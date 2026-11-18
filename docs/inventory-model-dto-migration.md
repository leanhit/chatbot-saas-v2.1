# Inventory Model–Migration–DTO–Consumer (Giai đoạn 0)

**Ngày:** 2026-11-17
**Nguồn:** quét tĩnh `backend/src/main/resources/db/migration/**`, `backend/src/main/java/**`. Chưa đối chiếu dữ liệu thực tế trên DB.

## 1. Flyway hubs

8 location trong `MultiFlywayConfig`: `identity`, `user`, `tenant`, `app`, `config`, `message`, `shared`, `spokes`. Tất cả `baselineOnMigrate`, `outOfOrder=true`, `validateOnMigrate=false`, `ignoreMigrationPatterns(*:missing|pending|ignored)`.

- **Payment không có hub riêng**: các bảng payment (`simple_payments`, `merchant_payment_sessions`, `webhooks`, `invoices`, `discounts`, `packages`, `package_upgrade_audits`) nằm trong location `shared` (V5–V19). `PaymentHubConfig` tồn tại — cần xác nhận nó trỏ cùng datasource `shared` hay DB riêng không có Flyway.
- `FacebookDatabaseConfig`, `MinioDatabaseConfig` tồn tại; spokes/minio migrations nằm trong `spokes`/`shared`.

## 2. Kiểu cột datetime trong migration

~240 cột ngày/giờ. Phân bố:

| Kiểu SQL | Số lượng | Ghi chú |
|---|---|---|
| `TIMESTAMP` (without tz) | ~230 | Mặc định toàn hệ thống: `created_at`, `updated_at`, `expires_at`, `sent_time`, v.v. |
| `TIMESTAMP WITH TIME ZONE` | 6 | `app.device_bindings` (4 cột, V5); `message.penny_analytics_events.timestamp`, `created_at` (V19) |
| `DATE` | 2 | `analytics_daily_summary.summary_date`; partition bounds của `messages` (nội bộ, không phải API) |

Ngoại lệ đặt tên cột: `tenant.tenants` dùng `createdat`/`updatedat` (không underscore); `identity.refresh_tokens` dùng `expiry_date`/`created_date` (có underscore, khác convention `expires_at`/`created_at`).

## 3. Kiểu Java trong entity/model

325 field ngày/giờ trong 143 file:

- **`LocalDateTime`**: ~290 field — áp đảo mọi domain (tenant, message, routing, payment, penny, spokes, user, shared).
- **`Instant`**: ~30 field, tập trung ở:
  - `identity`: `RefreshToken.expiryDate`, `createdDate`
  - `app/license`: `License`, `DeviceBinding` (khớp `TIMESTAMPTZ` ở device_bindings; cần kiểm `licenses` table)
  - `message/penny`: `PennyConversationContext`, `AnalyticsEvent`, `ConversationContext`, `ImportExportChangelog`
  - DTO tenant: `TenantResponse`, `TenantDetailResponse`, `TenantBasicInfoRequest` dùng `Instant` trong khi entity `Tenant` dùng `LocalDateTime` → có lớp convert ở mapper.
- **`LocalDate`**: `AnalyticsDailySummary.summaryDate` (khớp `DATE`).

## 4. Mismatch Java ↔ SQL cần xác minh

| Entity/field | Java | Cột SQL | Vấn đề |
|---|---|---|---|
| `RefreshToken.expiryDate/createdDate` | `Instant` | `TIMESTAMP` (no tz) | Hibernate map `Instant` thường yêu cầu timestamptz; cần kiểm `@Column`/converter |
| `License`, `DeviceBinding` | `Instant` | `TIMESTAMPTZ` (device_bindings) | Nhất quán; kiểm bảng `licenses` |
| `AnalyticsEvent` | `Instant` | `TIMESTAMPTZ` | Nhất quán |
| `Tenant` entity | `LocalDateTime` | `TIMESTAMP` | Nhất quán kiểu, nhưng DTO trả `Instant` → ngữ nghĩa UTC được "thêm" ở biên API |
| Mọi `LocalDateTime` còn lại | `LocalDateTime` | `TIMESTAMP` | Nhất quán kiểu; câu hỏi là giá trị lưu là UTC hay giờ server — **chưa xác định**, là rủi ro dữ liệu lớn nhất |

## 5. JSON/API datetime

- `@JsonFormat`: 97 chỗ, nhưng chỉ 2 pattern:
  - `STANDARD_JSON_FORMAT` = `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'` (58 chỗ)
  - `API_DATETIME_FORMAT` = cùng pattern trên (38 chỗ) + 1 chỗ không timezone
  - → literal `Z` gắn trên cả `LocalDateTime` lẫn `Instant`; hai hằng số trùng pattern.
- DTO không annotation → `LocalDateTime` serialize mặc định `yyyy-MM-ddTHH:mm:ss[.nnn]` (không offset) → **2 format JSON khác nhau** trong cùng API.
- `ApiResponse.timestamp`: `String` = `LocalDateTime.now().toString()` → format thứ 3, không timezone.
- `ErrorResponse.timestamp`: `LocalDateTime` + pattern `'Z'` literal.
- Payment DTO (`PaymentStatusResponse`, `DepositResponse`): thêm field `*Formatted` = `dd/MM/yyyy HH:mm:ss` song song timestamp thô.
- `JwtFilter`: `timestamp` = epoch millis (number), shape `{error,message,status,timestamp}`.

## 6. ObjectMapper

- `JacksonConfig` định nghĩa bean `ObjectMapper` thủ công (ghi đè auto-config của Boot): register `JavaTimeModule`, disable `WRITE_DATES_AS_TIMESTAMPS`, `setDateFormat(SimpleDateFormat)` — **SimpleDateFormat không tác dụng với java.time**; có thể làm mất các customizer khác của Boot.
- 12 chỗ `new ObjectMapper()`: `JwtFilter`, `MessageService`, `JsonMapConverter`, `IntelligentEscalationService`, `DataExportService`, `CertificationService`, `LocationService`, `JsonUtils`, `FacebookKafkaProducer`, `CacheConfig`, `UnifiedRedisConfig`, `JacksonConfig`.

## 7. Error handling inventory

- Trung tâm: `GlobalExceptionHandler` (`@RestControllerAdvice`, `LOWEST_PRECEDENCE`) → `ErrorResponse` schema `{status,code,message,description,errors,details,path,timestamp,correlationId,requestId,userId,tenantId}`; `ErrorCode` + `ErrorCodeMapper` → HTTP status.
- **41 controller** chứa `catch (Exception`; **~24 controller** trả `Map.of("error", ...)` hoặc map shape riêng.
- Error DTO cục bộ: `KnowledgeDocumentController` (record `ErrorResponse` field `error`); `payment/transaction/exception/ErrorResponse` — **không thấy nơi import, nghi dead code**.
- `JwtFilter` tự ghi JSON lỗi (shape khác shared DTO, timestamp epoch millis).
- `SecurityConfig` **không có** `exceptionHandling`/`AuthenticationEntryPoint`/`AccessDeniedHandler` → lỗi 401/403 từ Spring Security dùng format mặc định, không theo schema chung.
- Ngoại lệ protocol: `FacebookWebhookController` trả plain text.
- Nhiều controller trả **trực tiếp JPA entity**: `EscalationTicket`, `KnowledgeArticle`, `Invoice`, `MerchantPaymentSession`, `MerchantApiKey`, `Webhook`, `TenantWebhook`, `RoutingRule`, `SLAConfiguration`, `EscalationTier`, `TrainingMaterial`, `TenantApiKey`, `AgentSkill`, `AgentCertification`, v.v. → entity lộ ra API, mang theo `@JsonFormat` và quan hệ lazy.

## 8. Rủi ro chính & đề xuất pilot

1. **Rủi ro dữ liệu lớn nhất:** toàn bộ `TIMESTAMP` không tz — chưa biết giá trị là UTC hay giờ local server. Phải xác minh bằng dữ liệu thật trước khi đổi kiểu.
2. **Rủi ro contract:** 2+ format datetime JSON cùng tồn tại; `Z` literal trên `LocalDateTime`.
3. **Pilot đề xuất:** `identity` hub (`refresh_tokens` — 2 cột, `Instant`↔`TIMESTAMP` mismatch, blast radius nhỏ, consumer là auth flow nội bộ) hoặc `tenant` (DTO đã dùng `Instant`, entity `LocalDateTime` — có sẵn mapper để chuẩn hóa). Quyết định sau khi xác minh timezone dữ liệu thật.

## 9. Phase 1 — đã thay đổi (2026-11-17)

- `ClockConfig`: bean `Clock.systemUTC()`.
- `DateUtils`: thêm helpers UTC (`UTC_ZONE`, `nowUtc`, `nowUtcLocalDateTime`, `toUtcLocalDateTime`, `toInstant`, `formatInstantUtc`, `formatUtcLocalDateTime`, `parseInstantUtc`, `parseUtcLocalDateTime`). Method cũ giữ nguyên.
- `JacksonConfig`: `setTimeZone(UTC)` — fix `java.util.Date` serialize khớp literal `Z`.
- `SecurityConfig`: `AuthenticationEntryPoint` + `AccessDeniedHandler` trả shared `ErrorResponse` (401/403).
- `ObjectMapper` consolidation: `JwtFilter`, `MessageService`, `FacebookKafkaProducer`, `IntelligentEscalationService`, `CertificationService` giờ inject shared mapper. `JsonMapConverter` thêm JavaTimeModule. Còn lại có chủ đích: `JsonUtils` (static util, đã config), `UnifiedRedisConfig`/`CacheConfig` (cần default typing cho cache), `LocationService`/`DataExportService` (method-local, đã register module).
- Tests: `DateUtilsUtcTest`, `ErrorResponseContractTest` — pass.
- `PaymentHubConfig` xác nhận dùng `sharedDataSource` + `sharedFlyway` → payment thuộc hub `shared`.

## 10. Còn lại trước pilot

- Xác minh giá trị timezone thực tế trong các cột `TIMESTAMP` (query DB mẫu).
- `ApiResponse.timestamp` (String, no zone), `JwtFilter` error shape (epoch millis), payment `*Formatted` (`dd/MM/yyyy`) — quyết định contract khi pilot.
- 41 controller `catch (Exception`, ~24 `Map.of("error")`, entity trả trực tiếp — dọn theo từng domain.

## 11. Xác minh môi trường (2026-11-17)

- Postgres mọi hub: `TimeZone = Etc/UTC` (kiểm tra tenant; cùng image/config).
- App container: `TZ=UTC`, `date` = UTC → JVM default timezone = UTC.
- Suy ra: `LocalDateTime.now()` đang ghi **UTC wall-clock** vào cột `TIMESTAMP` → semantics thực tế đã là UTC, nhưng **ngầm định** (phụ thuộc TZ môi trường). Không cần migrate dữ liệu; cần làm explicit.
- Môi trường dev hiện các bảng gần như trống → chưa verify được dữ liệu lịch sử; nếu prod từng chạy TZ khác cần audit riêng.
- `RefreshToken` (`Instant` ↔ `TIMESTAMP` no-tz): an toàn trong môi trường UTC hiện tại, nhưng nên chuẩn hóa trong pilot.

## 12. Pilot tenant — đã áp dụng (2026-11-17)

Quyết định contract: **giữ `TIMESTAMP` (no tz) = UTC wall-clock**, không migrate sang `TIMESTAMPTZ` (tránh rủi ro trên môi trường dùng chung; semantics đã là UTC). `Instant` chỉ ở biên API/DTO.

Thay đổi:
- `Tenant` entity + `TenantProfile`, `TenantProfessional`, `TenantApiKey`: `LocalDateTime.now()` → `DateUtils.nowUtcLocalDateTime()`.
- 12 file tenant (services, scheduler, grpc): `LocalDateTime.now()`/`ZoneId.systemDefault()` → `DateUtils.nowUtcLocalDateTime()`/`DateUtils.UTC_ZONE`.
- `TenantBasicInfoRequest`: parse/convert expiresAt theo UTC (trước dùng `systemDefault` — bug thật khi JVM TZ ≠ UTC).
- `TenantMapper.toInstant` → `DateUtils.toInstant` (UTC).
- DTO `LocalDateTime` thêm `@JsonFormat(API_DATETIME_FORMAT, UTC)`: `TenantPackageInfo`, `TenantPackageDetailResponse`, `TenantSearchResponse` → output `...Z` nhất quán với `TenantResponse`/`TenantDetailResponse` (Instant).
- `DateUtils.parseInstantUtc(String, pattern)` overload mới.
- Test: `TenantDatetimeContractTest` — pass.

Còn lại trong tenant: entity vẫn `LocalDateTime` (by design = UTC); `expiresAtDisplay`/`expiresAt.toString()` là display string — xem lại khi chuẩn hóa payment.

## 13. Rollout hàng loạt (2026-11-17)

- Toàn bộ `src/main`: `LocalDateTime.now()`→`DateUtils.nowUtcLocalDateTime()`, `Instant.now()`→`DateUtils.nowUtc()`, `ZoneId.systemDefault()`→`DateUtils.UTC_ZONE`, `LocalDate/ZonedDateTime/OffsetDateTime.now()`→`...(DateUtils.UTC_ZONE)` (~150 file). Fix hậu quả `java.time.*` fully-qualified.
- 11 hub `*Config`: thêm `hibernate.jdbc.time_zone=UTC` → `Instant`↔`TIMESTAMP` deterministic bất kể JVM TZ.
- `JacksonConfig`: custom (de)serializer cho `LocalDateTime` — serialize ra ISO-8601 `...Z` (UTC wall-clock), deserialize chấp nhận cả `Z`/offset lẫn plain local → mọi DTO `LocalDateTime` tự động theo contract, không cần `@JsonFormat` từng field. `@JsonFormat` cũ vẫn override, output tương đương.
- Test mới: `JacksonUtcContractTest` — pass. `compileTestJava` OK.

## 14. Còn lại (Giai đoạn 3–4)

- Error handling rollout: 41 controller `catch (Exception`, ~24 `Map.of("error")`, entity trả trực tiếp API, `JwtFilter` shape riêng, payment `*Formatted` `dd/MM/yyyy`, `ApiResponse.timestamp` String no-zone.
- `RefreshToken` `Instant`↔`TIMESTAMP`: đã an toàn nhờ `hibernate.jdbc.time_zone=UTC`; cân nhắc migration `TIMESTAMPTZ` ở phase sau nếu muốn explicit.
- Prevention: review rules (cấm `LocalDateTime.now()`/`systemDefault`/`new ObjectMapper()` trong code mới), mở rộng contract tests sang API đại diện.

## 15. Error handling rollout + test fixes (2026-11-17)

- `ApiResponse.timestamp`: String ISO-8601 `...Z` (trước `LocalDateTime.toString()` no-zone).
- `JwtFilter.sendErrorResponse`: shape riêng `{error,message,status,timestamp:epoch}` → shared `ErrorResponse` (UNAUTHORIZED).
- `Map.of("error", ...)` trong controller → `ErrorResponse.fromMessage(...)`: 4 file "Tenant context missing" (21 chỗ), + DLQ/KafkaDlq/IdentityInterHub/TenantInterHub/TenantPublicApi/DataExport/CustomerProfile/Agent/SimplePayment controllers. Return type một số method đổi `ResponseEntity<Map<..>>` → `ResponseEntity<?>`; `SimplePaymentController.debugContext` → `Object`. Service-internal error markers (KafkaDlqService, DLQManagementService, PennyConfigurationService) giữ nguyên có chủ đích.
- Test fixes: `@Spy ObjectMapper` trong 3 test class (InjectMocks giờ inject shared mapper); `build.gradle` test JVM `user.timezone=UTC` — fix 4 test fail do host TZ=Asia/Bangkok trong khi service đã chuẩn UTC.
- **Full suite: 900 tests, 0 failures.**

## 16. Còn lại

- 41 controller `catch (Exception` chưa dọn hết (nhiều catch là legit); entity trả trực tiếp API (~20 loại) cần DTO hoá theo domain; payment `*Formatted` `dd/MM/yyyy` là display-field exception có chủ đích; `RefreshToken` Instant↔TIMESTAMP an toàn nhờ `hibernate.jdbc.time_zone=UTC`.
- Prevention: review rules cấm `LocalDateTime.now()`/`ZoneId.systemDefault()`/`new ObjectMapper()`/`Map.of("error"` trong code mới; contract tests mở rộng.

## 17. Prevention (2026-11-17)

- `docs/datetime-error-conventions.md`: quy ước bắt buộc cho code mới.
- CI `backend-test` thêm step **Convention guard**: fail nếu có `LocalDateTime.now()`/`Instant.now()`/`LocalDate.now()`/`ZonedDateTime.now()`/`OffsetDateTime.now()` không tham số, `ZoneId.systemDefault()`, `Map.of("error")` trong `*Controller.java`, hoặc `new ObjectMapper()` ngoài allowlist (7 site có chủ đích). Đã verify pass locally + YAML hợp lệ.
- Datetime contract giờ đồng nhất kể cả khi entity trả trực tiếp (global `LocalDateTime` serializer → `...Z`).

## 18. Backlog còn lại (không block)

- Entity trả trực tiếp API (~20 loại: EscalationTicket, KnowledgeArticle, Invoice, MerchantPaymentSession, Webhook, TenantWebhook, RoutingRule, SLAConfiguration, EscalationTier, TrainingMaterial, TenantApiKey, MerchantApiKey, AgentSkill, AgentCertification...) → DTO hoá theo domain, ưu tiên payment/message trước.
- 41 controller `catch (Exception` — review từng cái, nhiều là legit; chuẩn hoá dần khi đụng domain.
- `RefreshToken` `Instant`↔`TIMESTAMP`: an toàn nhờ `hibernate.jdbc.time_zone=UTC`; optional migration `TIMESTAMPTZ` nếu muốn explicit.

## 19. Entity→DTO hoá đợt 1 (2026-11-17)

DTO mới + controller cập nhật:
- `MerchantApiKeyResponse` — loại `apiSecret`/`webhookSecret`; secrets chỉ trả khi create (show-once). Fix rò rỉ secret trong list/update.
- `MerchantPaymentSessionResponse`, `InvoiceResponse`, `WebhookResponse` (loại `secret`), `TenantWebhookResponse` (loại `secret`), `TenantApiKeyResponse` (loại `keyHash`).
- `KnowledgeArticleResponse` — loại `embedding` (vector 1536 chiều) khỏi mọi response/search.
- `EscalationTicketResponse` (penny).
- `EscalationTierResponse`, `SLAConfigurationResponse`, `RoutingRuleResponse` (message/store).

Còn lại: training domain (AgentSkill, AgentCertification, CertificationLevel, AgentTrainingProgress, AgentOnboardingProgress/Task, TrainingMaterial, TrainingCategory), TenantWebhookDelivery, ConversationEvent, GrpcCallFailure — không có secret/PII nhạy cảm, ưu tiên thấp.

## 20. Entity→DTO đợt 2 — HOÀN TẤT (2026-11-17)

DTO mới: `TrainingCategoryResponse`, `TrainingMaterialResponse`, `AgentSkillResponse`, `AgentTrainingProgressResponse`, `CertificationLevelResponse`, `AgentCertificationResponse`, `AgentOnboardingTaskResponse`, `AgentOnboardingProgressResponse`, `TenantWebhookDeliveryResponse`, `ConversationEventResponse`, `GrpcCallFailureResponse`.

Controllers cập nhật: `TrainingController`, `CertificationController`, `SkillController`, `OnboardingController`, `TenantWebhookController` (deliveries), `ConversationController` (events), `GrpcFailureController`.

**Kết quả: không còn JPA entity nào được trả trực tiếp từ `*Controller.java`.** Scan `ResponseEntity<...>` toàn bộ controller chỉ còn DTO/plain class. Full suite 900 tests pass.

Còn lại duy nhất: dọn `catch (Exception` trong controllers (review theo domain, nhiều là legit).

## 21. Migration squash — HOÀN TẤT (2026-11-18)

- 87 migration files → **8 file `V1__baseline.sql`** (mỗi hub 1 file), sinh bằng `pg_dump --schema-only` từ DB đã migrate đúng.
- Bản cũ giữ tại `backend/src/main/resources/db/migration-legacy/` (rollback tham khảo, KHÔNG nằm trong Flyway locations).
- Fix bug: thêm `app/V9__create_training_tables.sql` trước khi dump → 8 bảng training đã có trong baseline app.
- Lưu ý kiến trúc: shared/spokes/minio/facebook/payment hubs cùng trỏ `chatbot_user_db` — baseline user/shared/spokes phân chia table ownership rõ ràng, không còn duplicate `category`/`file_metadata`.
- Môi trường có DB cũ: cần `flyway repair` hoặc wipe + migrate lại (baseline V1 không tương thích history cũ).
