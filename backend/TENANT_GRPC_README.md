# Tenant gRPC Service - Hướng dẫn sử dụng

## Tổng quan

gRPC service cho tenant đã được implement thành công với các tính năng sau:

### ✅ Đã implement:
- `validateTenant` - Kiểm tra tính hợp lệ của tenant
- `checkTenantExists` - Kiểm tra tenant có tồn tại không
- `getTenant` - Lấy thông tin chi tiết tenant
- `listTenants` - Liệt kê tất cả tenants

### 🔄 Chưa implement (sẽ làm sau):
- `createTenant` - Tạo tenant mới
- `updateTenant` - Cập nhật tenant
- `deleteTenant` - Xóa tenant
- `searchTenants` - Tìm kiếm tenant
- `activateTenant` - Kích hoạt tenant
- `suspendTenant` - Tạm dừng tenant
- `getTenantStatus` - Lấy trạng thái tenant

## Cấu hình

- **Port**: 50052 (được cấu hình trong `application-tenant.properties`)
- **Proto file**: `src/main/resources/proto/tenant-service.proto`
- **Generated classes**: `src/main/java/com/chatbot/core/tenant/grpc/`

## Cách sử dụng

### 1. Khởi động server

gRPC server sẽ tự động khởi động khi ứng dụng Spring Boot start:

```bash
./gradlew bootRun
```

### 2. Test với REST API

Sử dụng các endpoint test sau:

#### Test kết nối
```bash
curl -X GET http://localhost:8080/api/tenant/grpc-test/test
```

#### Validate tenant
```bash
curl -X GET http://localhost:8080/api/tenant/grpc-test/validate/{tenantKey}
```

#### Kiểm tra tenant tồn tại
```bash
curl -X GET http://localhost:8080/api/tenant/grpc-test/exists/{tenantKey}
```

#### Lấy thông tin tenant
```bash
curl -X GET http://localhost:8080/api/tenant/grpc-test/get/{tenantKey}
```

### 3. Sử dụng gRPC client trực tiếp

```java
@Autowired
private TenantGrpcClient grpcClient;

// Validate tenant
ValidateTenantResponse response = grpcClient.validateTenant("tenant-key");

// Check tenant exists
CheckTenantExistsResponse exists = grpcClient.checkTenantExists("tenant-key");

// Get tenant details
TenantDetailResponse tenant = grpcClient.getTenant("tenant-key");
```

## Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   REST Client   │────│  GrpcTestController │────│  TenantGrpcClient │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                                        │
                                                        ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   gRPC Server   │────│TenantServiceGrpcImpl│────│ TenantRepository│
│   (Port 50052) │    └──────────────────┘    └─────────────────┘
└─────────────────┘
```

## Log và Debug

gRPC service log ở level INFO:
- `gRPC: Validating tenant với key: {tenantKey}`
- `gRPC: Kiểm tra tenant tồn tại với key: {tenantKey}`
- `gRPC: Lấy tenant với key: {tenantKey}`
- `gRPC: Liệt kê tenants - trang: {page}, kích thước: {size}`

## Error Handling

Tất cả errors được handle và trả về gRPC status codes:
- `NOT_FOUND` - Khi tenant không tồn tại
- `INTERNAL` - Khi có lỗi server
- `UNIMPLEMENTED` - Khi method chưa được implement

## Testing

1. **Unit Tests**: Sẽ được thêm sau
2. **Integration Tests**: Sử dụng REST endpoints
3. **gRPC Client Tests**: Sử dụng `TenantGrpcClient`

## Next Steps

1. ✅ Implement các methods cơ bản (đã hoàn thành)
2. 🔄 Implement các methods còn lại (create, update, delete, etc.)
3. 📝 Add unit tests
4. 📝 Add integration tests
5. 🔧 Add authentication & authorization
6. 📊 Add metrics and monitoring

## Files liên quan

- `src/main/java/com/chatbot/core/tenant/grpc/TenantServiceGrpcImpl.java` - Implementation chính
- `src/main/java/com/chatbot/core/tenant/grpc/TenantGrpcClient.java` - gRPC client
- `src/main/java/com/chatbot/core/tenant/controller/GrpcTestController.java` - REST test endpoints
- `src/main/java/com/chatbot/core/tenant/config/GrpcServerConfig.java` - Server configuration
- `src/main/resources/proto/tenant-service.proto` - Protocol buffer definition
