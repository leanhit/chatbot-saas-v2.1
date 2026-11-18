# Quy ước Datetime & Error Handling (bắt buộc cho code mới)

Tham chiếu: `docs/inventory-model-dto-migration.md`.

## Datetime

- **Cấm** `LocalDateTime.now()`, `Instant.now()`, `LocalDate.now()`, `ZonedDateTime.now()`, `OffsetDateTime.now()` không tham số → dùng `DateUtils.nowUtcLocalDateTime()` / `DateUtils.nowUtc()` / `LocalDate.now(DateUtils.UTC_ZONE)`.
- **Cấm** `ZoneId.systemDefault()` → dùng `DateUtils.UTC_ZONE`.
- Entity/`TIMESTAMP` column = **UTC wall-clock** (`LocalDateTime`); `TIMESTAMPTZ` = `Instant`; `DATE` = `LocalDate`.
- API/DTO: `Instant` hoặc `LocalDateTime` + `@JsonFormat(API_DATETIME_FORMAT, UTC)` → output `yyyy-MM-dd'T'HH:mm:ss[.SSS]'Z'`. `LocalDate` → `yyyy-MM-dd`.
- Inject `Clock` bean khi cần test time; không gọi `now()` trực tiếp trong logic nghiệp vụ mới nếu có thể inject.

## Jackson

- **Cấm** `new ObjectMapper()` ngoài allowlist: `JsonUtils`, `LocationService`, `UnifiedRedisConfig`, `CacheConfig`, `JacksonConfig`, `JsonMapConverter`, `DataExportService`. Inject shared `ObjectMapper` bean.
- `LocalDateTime` serialize tự động ra `...Z` (UTC) qua `JacksonConfig`; deserialize chấp nhận cả `Z` lẫn plain local.

## Error handling

- **Cấm** `Map.of("error", ...)` / error body tự chế trong `*Controller.java` → dùng `com.chatbot.shared.dto.ErrorResponse` (`fromMessage`, `fromUnauthorized`, `fromValidation`, ...).
- Exception nghiệp vụ → throw `BaseException`/`ErrorCode`; `GlobalExceptionHandler` lo phần còn lại.
- 401/403 đã chuẩn qua `AuthenticationEntryPoint`/`AccessDeniedHandler` trong `SecurityConfig`.
- Ngoại lệ có chủ đích: `FacebookWebhookController` (plain text protocol), payment `*Formatted` (`dd/MM/yyyy` display), service-internal error markers trong `KafkaDlqService`/`DLQManagementService`/`PennyConfigurationService`.

## Entity ↔ API

- Không trả JPA entity trực tiếp từ controller — tạo DTO + mapper.

## CI

- `.github/workflows/ci-cd.yml` job `backend-test` có step **Convention guard** fail build nếu vi phạm các rule trên.
- Test JVM chạy `user.timezone=UTC` (build.gradle) — khớp production.

## Khi cần ngoại lệ

Ghi rõ trong PR + cập nhật allowlist trong CI guard nếu là `new ObjectMapper()` site mới có lý do chính đáng.
