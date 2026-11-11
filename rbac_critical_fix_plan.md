# Kế hoạch fix RBAC — 3 vấn đề CRITICAL

Nguồn: RBAC backend audit (2026-11-09). Bối cảnh: `SecurityConfig.anyRequest().authenticated()` đảm bảo mọi endpoint cần login, nhưng các endpoint sau **không check role/tenant ownership** → user thường (kể cả AGENT) có thể thực hiện hành động admin hoặc cross-tenant.

---

## Vấn đề 1 — IDOR cross-tenant qua `tenantKey` path param

### Hiện trạng

- `TenantProfileController` `@/api/tenant/{tenantKey}/profile|logo` — resolve `tenantKey` → `tenant.getId()` rồi gọi service, **không so sánh với `TenantContext.getTenantId()`**, không check role.
- `TenantProfessionalController` `@/api/v1/tenant/professional/{tenantKey}` — `GET/PUT/DELETE`, cùng lỗi.

### Hậu quả

Bất kỳ user đăng nhập nào cũng đọc/sửa/xóa profile + professional info của tenant khác chỉ bằng `tenantKey` (dễ đoán: subdomain, slug...)

### Fix

Thêm private helper vào cả 2 controller:

```java
private Tenant resolveAndAuthorize(String tenantKey) {
    Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
            .orElseThrow(() -> new TenantNotFoundException("Tenant not found with key: " + tenantKey));

    Long contextTenantId = TenantContext.getTenantId();
    if (contextTenantId == null || !contextTenantId.equals(tenant.getId())) {
        throw new InsufficientPermissionException(
            ErrorCode.TENANT_ACCESS_DENIED, "Tenant key does not match current tenant");
    }
    // mutations (PUT/DELETE/logo) additionally require owner/editor:
    // permissionValidator.isOwnerOrEditor(tenant.getId(), permissionValidator.getCurrentUserEmail())
    return tenant;
}
```

- `GET`: yêu cầu tenantKey == TenantContext tenant (read vẫn giới hạn trong tenant mình; cân nhắc cho phép public profile sau — hiện tại chưa có nhu cầu).
- `PUT`/`DELETE`/`logo`: thêm `isOwnerOrEditor` check.
- Inject `TenantPermissionValidator` vào constructor (đã có sẵn bean).

### Files

- `backend/src/main/java/com/chatbot/core/tenant/profile/controller/TenantProfileController.java`
- `backend/src/main/java/com/chatbot/core/tenant/professional/controller/TenantProfessionalController.java`

### Test

- Unit test: mock `TenantContext.getTenantId()` = tenant A, gọi với `tenantKey` của tenant B → expect `InsufficientPermissionException`/404.
- Case tenantKey đúng + role AGENT gọi PUT → expect denied; OWNER → OK.

---

## Vấn đề 2 — Test/dev controllers lộ ra production

### Hiện trạng

- `GrpcTestController` `@/api/tenant/grpc-test/**` — `POST /suspend/{tenantKey}`, `/activate/{tenantKey}`: **user thường suspend/activate được tenant bất kỳ**. Không có guard nào.
- `PublicTestController` `@/api/public/health`, `/grpc-status`.
- `MetricsTestController` `@/api/metrics/test`, `/summary`.

### Fix

Bọc cả 3 class bằng `@Profile("!prod")` (hoặc profile dev/staging cụ thể đang dùng — kiểm tra `application.properties` `spring.profiles.active` default = `development`):

```java
@Profile({"dev", "development", "local", "test"})
@RestController
@RequestMapping("/api/tenant/grpc-test")
public class GrpcTestController { ... }
```

- Chọn profile list khớp với môi trường local (`development` là default trong `application.properties`).
- Nếu production dùng profile `production`/`prod` → `!prod` đủ; an toàn hơn là whitelist profile dev.
- Kiểm tra không có service/frontend nào gọi các endpoint này trong production (grep frontend + docs).

### Files

- `backend/src/main/java/com/chatbot/core/tenant/controller/GrpcTestController.java`
- `backend/src/main/java/com/chatbot/core/tenant/controller/PublicTestController.java`
- `backend/src/main/java/com/chatbot/core/metrics/test/MetricsTestController.java`

### Test

- Verify app start với profile `production` → 3 controller không được load (context không chứa bean).
- Verify profile `development` → vẫn hoạt động.

---

## Vấn đề 3 — `UserController` thiếu role check

### Hiện trạng

`@/api/users` — `SecurityConfig` chỉ yêu cầu `authenticated()`:

- `PUT /{id}/status` — **user thường deactivate/activate được bất kỳ user nào, kể cả admin/owner**.
- `GET /` (search users, filter role) — lộ danh sách user toàn hệ thống.
- `GET /{id}`, `GET /{id}/basic` — xem thông tin user bất kỳ.
- Các endpoint `/me`, `/v1/user-info/me` — self-service, OK.

### Fix

Áp dụng pattern đã dùng trong codebase (`TenantPermissionValidator` + `@PreAuthorize`):

- `PUT /{id}/status`: yêu cầu `isAdmin(currentUser)` HOẶC `isTenantAdmin/isOwner` của tenant chứa user đích. Tối thiểu: chỉ `SystemRole.ADMIN`.
- `GET /` (search), `GET /{id}`, `GET /{id}/basic`: yêu cầu admin/owner của tenant, hoặc giới hạn kết quả trong tenant hiện tại (`TenantContext`).
- Implement qua `TenantPermissionValidator` inject vào controller (giống `TenantInvitationService`), throw `InsufficientPermissionException`.
- Self-service endpoints (`/me*`) giữ nguyên.

### Files

- `backend/src/main/java/com/chatbot/core/user/controller/UserController.java`
- (có thể) `backend/src/main/java/com/chatbot/core/user/service/UserService.java` nếu check đặt ở service layer cho nhất quán với `TenantInvitationService`.

### Test

- Unit test: user role AGENT gọi `PUT /{id}/status` → 403; ADMIN → 200.
- `GET /api/users` với AGENT → 403 hoặc chỉ thấy user cùng tenant.

---

## Thứ tự thực hiện & verify

1. Fix Vấn đề 2 trước (nhỏ nhất — chỉ thêm annotation, rủi ro thấp).
2. Fix Vấn đề 1 (IDOR — impact cross-tenant cao nhất).
3. Fix Vấn đề 3 (UserController — impact cao nhưng cần quyết định policy: admin-only hay tenant-scoped).
4. `./gradlew compileJava compileTestJava` + chạy test liên quan.
5. Commit riêng từng vấn đề (3 commits) để dễ review/revert.

## Lưu ý

- Không động vào 3 file migration SQL đang uncommitted ở local.
- Sau 3 vấn đề này, tiếp tục với nhóm HIGH (config controllers: escalation/SLA/routing/training/facebook/category...) theo cùng pattern `isOwnerOrEditor` — sẽ lập plan riêng khi được duyệt.
