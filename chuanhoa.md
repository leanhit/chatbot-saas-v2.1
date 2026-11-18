# Backend datetime, error handling và đồng bộ Model–DTO–Migration

**Ngày rà soát:** 2026-11-18
**Phạm vi:** Chuẩn hóa datetime và error handling xuyên suốt Java model/entity, database migration, DTO/mapper, API/controller, frontend consumer và các tích hợp liên quan.

> Phạm vi rà soát lần này: quét tĩnh toàn bộ `backend/src/main/java` (885 file Java, trong đó 85 controller), migration SQL trong `backend/src/main/resources/db` (cả active và legacy) và các test/contract test liên quan; đọc thủ công các vị trí đại diện để xác nhận vấn đề. Working tree hiện có nhiều thay đổi chưa commit; kết luận áp dụng cho code trong working tree, không mặc nhiên phản ánh `HEAD` hay bản đang chạy. Đây không phải line-by-line audit từng class, chưa đối soát dữ liệu đang chạy trong DB, và chưa truy vết mọi field tới frontend/webhook/consumer. Số lượng dưới đây là pattern hits/files, không đồng nghĩa số bug.

## 1. Tóm tắt

- **Datetime:** working tree đã bổ sung/chuyển một số luồng sang `Instant`, và 11 cấu hình datasource đặt `hibernate.jdbc.time_zone=UTC`. Tuy nhiên `LocalDateTime` vẫn xuất hiện rộng, serializer chung coi mọi giá trị là UTC, nhiều migration có timestamp không timezone, và một số DTO trả thêm chuỗi hiển thị; semantics từng field chưa được kiểm chứng toàn bộ.
- **Xử lý lỗi:** đã có `@RestControllerAdvice`, `BaseException`, `ErrorCode` và mapping HTTP status; `SecurityConfig`/`JwtFilter` có dùng shared `ErrorResponse`. Nhưng nhiều controller/filter vẫn tự bắt lỗi, tự chọn status và ghi JSON riêng. Có một lỗi luồng trong `RateLimitingFilter`: nếu downstream filter/controller ném exception, code có thể gọi filter chain lần hai.
- **Model–Migration–DTO:** working tree đã bổ sung response DTO cho một số domain và **đã tách request DTO cho toàn bộ controller từng nhận JPA entity làm `@RequestBody`** (xem mục 2.3); analytics/report và knowledge-base vẫn cần rà soát response entity, validation và field exposure.
- **Mức độ hoàn thành:** đã quét pattern trên toàn bộ source/migration liên quan và xác nhận một số vấn đề cụ thể bên dưới. Chưa hoàn tất inventory mọi field DB–entity–DTO–consumer hoặc kiểm thử/migration trên DB thật.
- **Khuyến nghị:** chốt contract và kiểm thử trước, sau đó chuẩn hóa dần theo domain. Không thay đổi hàng loạt API đang được consumer sử dụng nếu chưa có kế hoạch tương thích.

## 2. Hiện trạng datetime

### 2.1 Đã có nền tảng chuẩn hóa

- [`JacksonConfig`](backend/src/main/java/com/chatbot/config/JacksonConfig.java) đăng ký `JavaTimeModule`, tắt `WRITE_DATES_AS_TIMESTAMPS` và cài serializer/deserializer riêng cho `LocalDateTime`. Serializer diễn giải giá trị là UTC wall-clock rồi ghi ISO-8601 có `Z`; deserializer nhận cả chuỗi có `Z` và dạng local không có offset.
- [`DateUtils`](backend/src/main/java/com/chatbot/shared/utils/DateUtils.java) hiện dùng `UTC_ZONE` rõ ràng trong các chuyển đổi `Date`/`Instant`/`LocalDateTime`, có helper `nowUtc`, `nowUtcLocalDateTime`, `parseInstantUtc` và format UTC.
- [`ApiResponse`](backend/src/main/java/com/chatbot/shared/dto/ApiResponse.java) tạo timestamp dạng chuỗi ISO UTC có `Z`. [`ErrorResponse`](backend/src/main/java/com/chatbot/shared/dto/ErrorResponse.java) giữ field `LocalDateTime` và khai báo format UTC; khi dùng mapper chung, field này cũng đi qua serializer UTC.
- Một số DTO, ví dụ [`TenantResponse`](backend/src/main/java/com/chatbot/core/tenant/dto/TenantResponse.java) và [`LicenseResponse`](backend/src/main/java/com/chatbot/core/license/dto/LicenseResponse.java), dùng `Instant` cùng `@JsonFormat`.

### 2.2 Các điểm chưa thống nhất

1. **Kiểu biểu diễn thời điểm khác nhau giữa các API**
   - Tìm kiếm toàn source có 1.191 hit `LocalDateTime` trong 262 Java file, 170 hit `Instant` trong 40 file và 102 hit `@JsonFormat` trong 45 file. Đây là hit tìm kiếm (gồm import/method/field), không phải số field đã inventory.
   - Nhiều DTO dùng `LocalDateTime`, ví dụ [`SubscriptionResponse`](backend/src/main/java/com/chatbot/core/app/subscription/dto/SubscriptionResponse.java) và [`MerchantPaymentResponse`](backend/src/main/java/com/chatbot/core/payment/merchant/dto/MerchantPaymentResponse.java). Một số không có `@JsonFormat` ở field và phụ thuộc serializer toàn cục; vì vậy cần kiểm thử qua mapper thực tế và xác định semantics của dữ liệu trước khi coi output là UTC.
   - DTO khác dùng `Instant`, có/không có annotation tùy nơi. Các kiểu này mang ngữ nghĩa khác nhau: `Instant` là thời điểm tuyệt đối; `LocalDateTime` không mang offset/timezone.
   - DTO payment như [`PaymentStatusResponse`](backend/src/main/java/com/chatbot/core/payment/transaction/dto/PaymentStatusResponse.java) và [`DepositResponse`](backend/src/main/java/com/chatbot/core/payment/transaction/dto/DepositResponse.java) giữ timestamp gốc dạng `LocalDateTime` nhưng đồng thời tạo các field bổ sung dạng `dd/MM/yyyy HH:mm:ss`; controller gọi các helper định dạng này trước khi trả response. Các field này là dữ liệu hiển thị, không nên được dùng thay timestamp máy đọc được; cần xác nhận consumer rồi loại bỏ hoặc deprecate có kế hoạch.

2. **Quy ước `Z` dựa trên giả định UTC của `LocalDateTime`**
   - Serializer chung và DTO như [`PennyBotResponse`](backend/src/main/java/com/chatbot/core/penny/dto/PennyBotResponse.java) ghi `LocalDateTime` với hậu tố `Z`; [`ErrorResponse`](backend/src/main/java/com/chatbot/shared/dto/ErrorResponse.java) cũng khai báo format này.
   - Code hiện đã chủ động diễn giải `LocalDateTime` là UTC wall-clock khi serialize, nhưng kiểu `LocalDateTime` tự nó không mang timezone. Vì vậy `Z` chỉ đúng nếu mọi giá trị đi qua contract đó thực sự được tạo/lưu theo UTC; serializer không thể xác minh semantics của dữ liệu cũ hoặc dữ liệu đến từ integration.

3. **UTC rõ ràng hơn, nhưng chưa có `Clock` injectable và cần xác minh timezone DB**
   - Các conversion và helper được kiểm tra trong [`DateUtils`](backend/src/main/java/com/chatbot/shared/utils/DateUtils.java) dùng `UTC_ZONE`; không còn thấy `ZoneId.systemDefault()` hoặc `LocalDateTime.now()` không chỉ định zone trong Java backend.
   - `nowUtc()` hiện gọi `Clock.systemUTC()` trực tiếp và `nowUtcLocalDateTime()` gọi `LocalDateTime.now(UTC_ZONE)`; chưa có `Clock` bean để inject, thay thế trong test hoặc điều khiển thời gian thống nhất.
   - 11 cấu hình datasource có `hibernate.jdbc.time_zone=UTC`, đây là điểm đã chuẩn hóa cho JDBC/Hibernate.
   - Các active baseline có 259 khai báo `TIMESTAMP WITHOUT TIME ZONE`; toàn bộ cây migration còn nhiều khai báo timestamp trong legacy và 12 khai báo `TIMESTAMP WITH TIME ZONE`/`TIMESTAMPTZ` ở 4 file. Đây là kết quả tìm kiếm tĩnh, không phải đối soát từng cột. Cần xác minh timezone session/database và quy tắc cast của `DEFAULT CURRENT_TIMESTAMP` sang cột không timezone.

4. **Format timestamp trong envelope không đồng nhất**
   - [`ApiResponse`](backend/src/main/java/com/chatbot/shared/dto/ApiResponse.java) hiện tạo `timestamp` dạng `String` ISO UTC; [`ErrorResponse`](backend/src/main/java/com/chatbot/shared/dto/ErrorResponse.java) giữ kiểu `LocalDateTime`; DTO khác dùng `Instant` hoặc thêm field chuỗi hiển thị.
   - Hướng timezone đã thống nhất tốt hơn, nhưng kiểu JSON và precision vẫn phụ thuộc kiểu field, annotation và mapper thực tế. Cần contract test bằng Spring-managed `ObjectMapper` cho các response đại diện, không chỉ test từng helper.

5. **Cấu hình ObjectMapper bị lặp hoặc bị bỏ qua**
   - Ngoài bean trong [`JacksonConfig`](backend/src/main/java/com/chatbot/config/JacksonConfig.java), một số vị trí vẫn tự tạo `ObjectMapper`, ví dụ [`JsonMapConverter`](backend/src/main/java/com/chatbot/core/routing/converter/JsonMapConverter.java), [`JsonUtils`](backend/src/main/java/com/chatbot/shared/utils/JsonUtils.java), [`LocationService`](backend/src/main/java/com/chatbot/shared/location/service/LocationService.java) và [`DataExportService`](backend/src/main/java/com/chatbot/core/tenant/service/DataExportService.java).
   - Một số mapper phục vụ JSON nội bộ có thể là chủ ý; cần inventory cách sử dụng, đặc biệt nơi serialize/deserialize DTO hoặc dữ liệu API, để xác định mapper nào phải dùng chung module và UTC contract.

6. **Datetime pattern có hằng số sai nhưng hiện chưa được dùng** — **ĐÃ FIX**
   - `DateUtils.ISO_DATETIME_WITH_NANOS` từng khai báo 10 ký tự `S`; Java `DateTimeFormatter` chỉ chấp nhận tối đa 9 chữ số phần giây. Đã sửa thành 9 chữ `S` (`yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX`).

### 2.3 Đồng bộ persistence–entity–DTO

- Tìm kiếm trên toàn bộ migration cho thấy active baseline chủ yếu dùng `TIMESTAMP WITHOUT TIME ZONE`; kiểu này có thể khớp với `LocalDateTime` về mặt JDBC, nhưng chỉ đúng về nghiệp vụ khi xác nhận quy ước UTC/local, defaults và timezone database.
- Ví dụ đã truy vết một phần: [`AppSubscription`](backend/src/main/java/com/chatbot/core/app/subscription/model/AppSubscription.java) và [`SubscriptionResponse`](backend/src/main/java/com/chatbot/core/app/subscription/dto/SubscriptionResponse.java) dùng `LocalDateTime`; cột tương ứng trong [`app V1 baseline`](backend/src/main/resources/db/migration/app/V1__baseline.sql) là `timestamp without time zone`. Hibernate timezone đã đặt UTC, nhưng semantics dữ liệu cũ/default vẫn cần xác nhận.
- **Request boundary entity — ĐÃ XỬ LÝ (request DTO):** toàn bộ controller từng nhận JPA entity làm `@RequestBody` đã được tách sang request DTO riêng trong package `dto` của từng domain, kèm mapping whitelist field (không mass-assign `id`/`tenantId`/audit fields). Các DTO đã thêm: training (`TrainingMaterialRequest`, `TrainingCategoryRequest`, `AgentTrainingProgressRequest`, `AgentSkillRequest`, `AgentOnboardingTaskRequest`, `CertificationLevelRequest`); analytics/report (`FunnelStageRequest`, `CustomReportRequest`, `ReportScheduleRequest`); message store (`EscalationTierRequest`, `RoutingRuleRequest`, `SLAConfigurationRequest`); payment (`WebhookRequest`, `MerchantApiKeyRequest`, `PackageRequest`); penny/tenant (`KnowledgeArticleRequest`, `EscalationTicketRequest`, `TenantWebhookRequest`). `TakeoverController` nhận `TakeoverMessage` — đây là transport DTO có sẵn validation, không phải JPA entity nên giữ nguyên.
- **Còn lại ở response boundary:** analytics/report vẫn trả trực tiếp một số `FunnelStage`, `CustomReport`, `ReportSchedule`; `KnowledgeBaseController` trả `KnowledgeArticle`; một số controller khác trả entity trong response. Cần tách response DTO, whitelist field và thêm validation; không tính các response DTO mới là đã xử lý response boundary.
- Migration hiện cũng đang được tái cấu trúc: các migration theo phiên bản cũ được chuyển vào `db/migration-legacy`, còn tám location active có `V1__baseline.sql`. [`MultiFlywayConfig`](backend/src/main/java/com/chatbot/config/MultiFlywayConfig.java) đặt `baselineOnMigrate(true)`, baseline version `0`, tắt `validateOnMigrate` và bỏ qua một số trạng thái missing/pending/ignored; biến cấu hình `validateOnMigrate` được inject trong class và **đã được wire vào cả 8 Flyway bean** (trước đó bị hardcode `false`). Phải kiểm thử riêng database mới, database có lịch sử Flyway cũ và database đã có schema nhưng chưa có history; các baseline có `CREATE TABLE` thông thường (không `IF NOT EXISTS` cho phần lớn bảng), nên không được giả định việc baseline hóa an toàn cho mọi trạng thái hiện hữu. Đối chiếu từng baseline với kết quả áp dụng toàn bộ migration legacy trước khi rollout.
- Cần lập inventory field-level cho tất cả hub/table; chưa đủ căn cứ để kết luận mọi cột không timezone hoặc mọi `LocalDateTime` hiện tại đều sai.

## 3. Hiện trạng xử lý lỗi

### 3.1 Đã có cơ chế trung tâm

- [`GlobalExceptionHandler`](backend/src/main/java/com/chatbot/shared/exceptions/GlobalExceptionHandler.java) là `@RestControllerAdvice`; xử lý custom `BaseException`, validation, access denied, lỗi parse request, database integrity, runtime và một số lỗi framework.
- [`ErrorCode`](backend/src/main/java/com/chatbot/shared/exceptions/ErrorCode.java) tập trung khai báo mã lỗi; [`ErrorCodeMapper`](backend/src/main/java/com/chatbot/shared/exceptions/ErrorCodeMapper.java) ánh xạ mã sang HTTP status.
- [`ErrorResponse`](backend/src/main/java/com/chatbot/shared/dto/ErrorResponse.java) cung cấp schema lỗi dùng chung; [`ApiResponse`](backend/src/main/java/com/chatbot/shared/dto/ApiResponse.java) là schema response tổng quát/success.
- Security đã có handler riêng cho lỗi phát sinh trước MVC: [`SecurityConfig`](backend/src/main/java/com/chatbot/config/SecurityConfig.java) cấu hình `AuthenticationEntryPoint` và `AccessDeniedHandler` dùng shared `ErrorResponse`/`ObjectMapper`. [`JwtFilter`](backend/src/main/java/com/chatbot/core/identity/security/JwtFilter.java) cũng inject mapper chung và dùng shared `ErrorResponse` khi trả lỗi trong filter.

### 3.2 Các điểm chưa thống nhất

1. **Controller tự bắt lỗi và tự tạo response**
   - Quét toàn bộ `*Controller.java` ghi nhận 181 lần `catch (Exception|RuntimeException|Throwable)` trong 43 controller. Con số này gồm cả catch lồng nhau, cleanup, test/protocol và không tự động đồng nghĩa 181 lỗi; từng nhánh trả HTTP cần được phân loại.
   - [`MessageController`](backend/src/main/java/com/chatbot/core/message/store/controller/MessageController.java) tự map `IllegalStateException` thành 403 và `RuntimeException` thành 404, không đi qua mapping chung.
   - [`SimplePaymentController`](backend/src/main/java/com/chatbot/core/payment/transaction/controller/SimplePaymentController.java) có nhiều nhánh catch `Exception`; tùy endpoint có thể trả 400 kèm `Map`, chuỗi, body rỗng hoặc response khác. Một số nhánh đưa `e.getMessage()` thẳng ra client.
   - Cùng mẫu tự bắt lỗi/đổi mọi lỗi thành 400 và trả `e.getMessage()` còn thấy ở tenant/package/message usage/license, Facebook token và MinIO avatar controllers; cần rà từng endpoint, không đổi cơ học vì một số catch là protocol/test/cleanup có chủ ý.
   - Nhiều controller dùng `ApiResponse.error(...)` thay shared `ErrorResponse`, hoặc trả Map/plain string/body rỗng. `ApiResponse.error` có schema `success/message/data`, khác error schema `status/code/message/path/timestamp`; hai loại hiện cùng xuất hiện trong nhiều domain.
   - [`KnowledgeDocumentController`](backend/src/main/java/com/chatbot/core/penny/controller/KnowledgeDocumentController.java) có record `ErrorResponse` cục bộ với field `error`, độc lập với shared DTO; các nhánh catch còn đưa thông điệp exception vào response.
   - `ErrorResponse.fromMessage(...)` **đã fix**: gán `code` mặc định `"ERROR"` thay vì `null`, đáp ứng contract tối thiểu.

2. **HTTP status có thể phụ thuộc cách endpoint bắt lỗi**
   - Shared handler map `IllegalStateException` thành 409 và `RuntimeException` thành 500.
   - Nhưng `MessageController` map mọi `RuntimeException` thành 404; payment controller có endpoint map nhiều loại lỗi thành 400. Cùng exception nghiệp vụ vì thế có thể ra status khác tùy controller.
   - Nên ném exception nghiệp vụ có `ErrorCode` rõ ràng thay vì catch tổng quát rồi tự suy đoán status.

3. **Một số servlet filter chưa dùng shared error contract**
   - [`JwtFilter`](backend/src/main/java/com/chatbot/core/identity/security/JwtFilter.java), `AuthenticationEntryPoint` và `AccessDeniedHandler` dùng shared `ErrorResponse`; nhánh `JwtFilter.sendErrorResponse` **đã bổ sung `path`** từ request URI.
   - **ĐÃ CHUẨN HÓA:** [`TenantApiKeyAuthFilter`](backend/src/main/java/com/chatbot/core/tenant/guard/TenantApiKeyAuthFilter.java), [`MerchantAuthFilter`](backend/src/main/java/com/chatbot/core/payment/merchant/filter/MerchantAuthFilter.java), [`TenantRateLimitFilter`](backend/src/main/java/com/chatbot/core/tenant/guard/TenantRateLimitFilter.java) và [`RateLimitingFilter`](backend/src/main/java/com/chatbot/shared/security/RateLimitingFilter.java) đều đã chuyển sang shared `ErrorResponse` + `ObjectMapper`, kèm `path` từ request URI; rate-limit dùng `ErrorResponse.fromRateLimit` (code `RATE_LIMIT_EXCEEDED`).
   - **ĐÃ FIX double filter:** `RateLimitingFilter` trước đây gọi `filterChain.doFilter()` trong `try` bao trùm Redis và gọi tiếp trong `catch` — nếu downstream ném exception, filter xử lý cùng request lần hai. Hiện chỉ bọc Redis ops trong `try`, gọi filter chain đúng một lần.
   - Cần contract test HTTP cho 401/403/429 từ security/filter chain, gồm body/path/status và kiểm thử filter không gọi downstream lặp.

4. **Một số endpoint có contract lỗi đặc thù**
   - [`FacebookWebhookController`](backend/src/main/java/com/chatbot/spokes/facebook/webhook/controller/FacebookWebhookController.java) trả plain text cho webhook. Có thể giữ nếu cần đúng giao thức bên ngoài, nhưng cần ghi rõ là ngoại lệ có chủ đích.
   - [`payment/transaction/exception/ErrorResponse`](backend/src/main/java/com/chatbot/core/payment/transaction/exception/ErrorResponse.java) **đã xóa** — là kiểu lỗi trùng lặp, không có call site trong `src/main/java`.
   - gRPC, WebSocket, SSE và webhook có protocol/transport riêng; không nhất thiết ép dùng HTTP `ErrorResponse`, nhưng cần schema/status tương ứng được ghi rõ và có contract test.

5. **Metadata của error response chưa được lấy từ nguồn tin cậy thống nhất**
   - Shared handler hiện bổ sung correlation/request/user/tenant IDs từ request headers. Cần rà soát cách các header này được tạo/kiểm chứng và ưu tiên correlation/request ID do server middleware sinh hoặc xác nhận.

## 4. Mục tiêu đề xuất

### 4.1 Datetime contract

- `Instant` cho timestamp sự kiện và thời điểm tuyệt đối; serialize nhất quán ISO-8601 UTC, ví dụ `2026-11-17T09:00:00Z`.
- `LocalDate` cho ngày lịch không có giờ.
- Chỉ dùng `LocalDateTime` khi nghiệp vụ thực sự là giờ địa phương không gắn offset; tài liệu hóa timezone nghiệp vụ và cách chuyển đổi ở biên API/DB.
- Không dùng chuỗi ngày đã format theo locale làm trường timestamp API. Nếu cần cho UI, frontend định dạng theo locale từ timestamp máy đọc được.
- Không gắn `Z` literal cho giá trị không được chuyển/định nghĩa là UTC.
- Dùng chung Spring-managed `ObjectMapper`/`JavaTimeModule`; không tự tạo mapper cho serialization ứng dụng nếu không có lý do rõ ràng.
- Dùng `Clock`/UTC ở nơi tạo thời gian để giảm phụ thuộc timezone máy chủ và giúp test xác định.

### 4.2 Error contract

- Một schema lỗi API chung, dựa trên shared `ErrorResponse` hoặc phiên bản schema được chốt, có tối thiểu: `code`, `message`, `timestamp`, `path`, và `details`/`errors` khi phù hợp.
- Exception nghiệp vụ mở rộng `BaseException` và chọn `ErrorCode`; central handler là nơi quyết định HTTP status/response.
- Không catch `Exception` chỉ để trả body lỗi chung ở controller. Nếu cần cleanup hoặc chuyển đổi, rethrow exception phù hợp và giữ nguyên cause.
- Validation, auth, access denied và lỗi parse HTTP đều phải trả schema tương thích.
- Chỉ giữ response đặc thù cho giao thức bên ngoài (ví dụ webhook) khi có yêu cầu protocol; ghi rõ và có contract test.
- Không để raw exception/database detail lọt ra client; log đầy đủ phía server, phản hồi client dùng message an toàn.

### 4.3 Quy ước đồng bộ Model–Migration–DTO

Mỗi trường ngày/giờ được chuẩn hóa theo một mapping có thể truy vết xuyên các lớp:

| Ngữ nghĩa | Java model/entity | PostgreSQL | DTO/API |
|---|---|---|---|
| Ngày lịch, không có giờ | `LocalDate` | `DATE` | `YYYY-MM-DD` |
| Thời điểm tuyệt đối | `Instant` | `TIMESTAMPTZ` | ISO-8601 UTC, ví dụ `2026-11-17T09:00:00Z` |
| Giờ địa phương theo nghiệp vụ | `LocalDateTime` (chỉ khi có lý do) | `TIMESTAMP WITHOUT TIME ZONE` | Không tự gắn `Z`; tài liệu hóa timezone nghiệp vụ và cách chuyển đổi |

Đây là quy ước mục tiêu, không phải chỉ thị đổi kiểu mọi cột hiện có. Trước khi chuyển `TIMESTAMP`/`TIMESTAMPTZ`, phải xác định dữ liệu đang biểu diễn UTC hay giờ local, cách driver/JPA đọc ghi, và timezone cần dùng khi diễn giải dữ liệu cũ.

Với mỗi endpoint/field, inventory phải ghi tối thiểu:

| Hạng mục | Nội dung cần ghi |
|---|---|
| Ngữ nghĩa | Ngày lịch, thời điểm tuyệt đối, hay giờ địa phương |
| Lưu trữ | Hub/database, bảng/cột, kiểu SQL, default, nullability |
| Java | Entity/model field và kiểu Java hiện tại/mục tiêu |
| API | DTO/mapper, JSON type/format, Error/Success envelope nếu liên quan |
| Consumers | Frontend/modal, SDK, webhook, gRPC/event, script hoặc integration |
| Migration | Cách chuyển dữ liệu, timezone diễn giải, rollout/rollback và phiên bản migration |
| Verification | Test mapper/serialization, repository/migration, API contract và consumer |

Các nguyên tắc thực hiện:

- DTO là ranh giới API; không trả trực tiếp JPA entity nếu endpoint không chủ ý công khai cấu trúc persistence.
- Mapper là nơi chuyển đổi model ↔ DTO; không tạo một cách diễn giải timezone khác nhau ở controller hoặc frontend.
- Schema lỗi chung cũng là API DTO: thời điểm trong error response tuân theo datetime contract; error code/status/body phải khớp giữa exception → handler/filter → HTTP response.
- Success envelope và error envelope có thể là hai schema khác nhau, nhưng từng schema phải được version/document/test nhất quán.
- Migrations theo đúng database/hub Flyway location (`app`, `config`, `identity`, `message`, `shared`, `spokes`, `tenant`, `user`); không sửa migration đã áp dụng ở môi trường dùng chung.
- Mỗi thay đổi là một lát cắt dọc theo domain: migration + entity/model + mapper/DTO + controller/error mapping + tests/consumer; tránh đổi tất cả model trước rồi để DTO hoặc DB lệch ở trạng thái trung gian.

## 5. Kế hoạch cập nhật

### Tiến độ đã thực hiện (cập nhật 2026-11-18)

- **Datetime:** sửa `DateUtils.ISO_DATETIME_WITH_NANOS` (10→9 chữ `S`); wire `validateOnMigrate` vào cả 8 Flyway bean trong `MultiFlywayConfig`.
- **Error contract:** `ErrorResponse.fromMessage` có `code` mặc định `"ERROR"`; `JwtFilter` bổ sung `path`; 4 filter (`TenantApiKeyAuthFilter`, `MerchantAuthFilter`, `TenantRateLimitFilter`, `RateLimitingFilter`) dùng shared `ErrorResponse` + `ObjectMapper`; fix double `filterChain.doFilter` trong `RateLimitingFilter`; xóa `payment/transaction/exception/ErrorResponse` trùng lặp.
- **Request DTO (f8):** toàn bộ controller nhận entity `@RequestBody` đã tách sang request DTO (training, analytics/report, message store, payment, penny/tenant) — chi tiết ở mục 2.3.
- **Còn lại:** response entity ở analytics/report/knowledge-base; `catch(Exception)→e.getMessage()` trong controllers (f9); contract test HTTP cho filter/security; inventory field-level DB–entity–DTO–consumer; kiểm thử migration/baseline trên DB thật.

### Giai đoạn 0 — Lập inventory và chốt contract xuyên lớp

1. Lập bảng inventory cho từng field: database/hub → migration/table/column → entity/model → mapper/DTO → JSON → consumer.
2. Với từng cột thời gian, xác nhận semantics, kiểu SQL, dữ liệu hiện có, DB/session/JVM timezone, cách JPA/JDBC đọc ghi và mọi default/backfill.
3. Inventory error theo endpoint: exception source, `ErrorCode`, HTTP status, response schema và vị trí phát sinh (MVC, security filter, webhook, async/gRPC).
4. Đánh dấu endpoint trả trực tiếp entity, DTO có custom date string, mapper tự tạo, controller tự bắt exception, error DTO cục bộ và response theo protocol.
5. Chốt datetime contract, error contract, version/compatibility, ngoại lệ có chủ ý và thứ tự triển khai với frontend/integrations.
6. Viết contract tests cho các API/DTO đại diện trước khi thay đổi schema hoặc output.

**Gate để bắt đầu migration:** không có field nào được đổi nếu chưa xác định rõ semantics và timezone dữ liệu cũ; mỗi API breaking change phải có consumer/rollout plan.

**Kết quả cần có:** inventory truy vết model–column–migration–DTO–consumer; mapping hiện tại → mục tiêu; danh sách rủi ro dữ liệu, breaking changes và ngoại lệ protocol.

### Giai đoạn 1 — Hoàn thiện nền tảng dùng chung và bộ test

1. `JacksonConfig` đã đăng ký `JavaTimeModule` và custom serializer/deserializer cho `LocalDateTime`; tiếp tục kiểm tra cấu hình mapper thực tế và không hiểu `SimpleDateFormat` là cấu hình format cho `java.time`.
2. Quy ước UTC cho `LocalDateTime` đã được triển khai ở mapper chung. Cần xác nhận semantics dữ liệu từng domain trước khi áp dụng quy ước đó cho field cũ; xác định riêng contract cho `Instant` và `LocalDate`.
3. Các conversion trong `DateUtils` đã dùng UTC rõ ràng, không còn thấy `ZoneId.systemDefault()` trong Java backend, và 11 datasource config đặt `hibernate.jdbc.time_zone=UTC`; phần còn lại là cung cấp/inject `Clock` để kiểm soát thời gian trong test, đồng thời xác minh timezone session của DB/default.
4. `DateUtils` đã có helper UTC, nhưng format hiển thị vẫn được thêm vào một số DTO payment; tách các field hiển thị khỏi contract API sau khi xác nhận consumer và kế hoạch tương thích.
5. Shared `ErrorResponse`, `ErrorCode`/HTTP mapping và security `AuthenticationEntryPoint`/`AccessDeniedHandler` đã có. Cần chuẩn hóa các filter còn ghi JSON riêng, bổ sung kiểm thử status/body thực tế qua MVC/security chain, đồng thời xử lý các nhánh controller tự bắt lỗi.
6. Mapper tự tạo vẫn tồn tại; kiểm kê chúng và chuyển những mapper ảnh hưởng tới API/persistence contract sang cấu hình Spring-managed phù hợp.
7. Đã có test UTC, tenant datetime và error DTO. Mở rộng thành test dùng Spring-managed `ObjectMapper`, model–DTO mapping, migration/repository, error status/schema, security responses và đảm bảo rate-limit filter chỉ gọi downstream một lần trước khi migrate domain.
8. Các location Flyway active hiện dùng tám baseline V1 thay cho chuỗi migration cũ. So sánh từng baseline với schema sinh từ migrations trong `migration-legacy`, rồi kiểm thử upgrade trên từng trạng thái database được hỗ trợ; bước này chưa được xác minh trên DB.

**Trạng thái hiện tại:** helper/serializer UTC, shared error DTO và một số response DTO mới đã có; chưa đủ bằng chứng về bộ test end-to-end cho migration/baseline, model–DTO mapping, HTTP status/schema và consumer.

**Kết quả cần có:** bộ test dùng chung cho serialize/deserialize, model–DTO mapping, error schema/status và test helpers để migration từng domain.

### Giai đoạn 2 — Pilot theo một domain dọc

Chọn domain sau khi hoàn tất inventory; ưu tiên domain có timezone/schema/consumer đã hiểu rõ, blast radius nhỏ và có test/consumer đại diện. Không mặc định đổi domain chỉ dựa trên độ dễ sửa Java.

Trong pilot, cập nhật đủ một luồng từ persistence đến consumer:

1. Thêm **forward Flyway migration mới** vào đúng hub/database. Nếu đổi dữ liệu hiện hữu, ghi rõ biểu thức chuyển đổi, timezone nguồn, cách backfill/verify và phương án rollback.
2. Cập nhật entity/model để khớp kiểu cột và semantics mới; kiểm tra precision, nullability, default, index và query/filter/sort.
3. Cập nhật mapper/DTO và endpoint contract; response dùng shared error schema; nếu có thay đổi output, cập nhật consumer hoặc cung cấp compatibility/deprecation.
4. Chạy migration trên dữ liệu đại diện, kiểm tra trước/sau và thử các trường hợp ranh giới (timezone, DST nếu áp dụng, null, precision, invalid input).
5. Bổ sung regression/contract tests từ repository và mapper đến HTTP response, gồm lỗi validation/not-found/conflict/forbidden.
6. Chỉ mở rộng sang domain kế tiếp khi inventory và test xác nhận các lớp không lệch nhau, đồng thời không có consumer bị phá vỡ ngoài kế hoạch.

**Kết quả cần có:** một domain hoàn tất end-to-end, migration chạy được theo thứ tự deploy dự kiến, API tương thích và có bằng chứng test/đối soát dữ liệu.

### Giai đoạn 3 — Mở rộng theo domain và rollout tương thích

Triển khai theo mức độ phụ thuộc và kết quả inventory. Nhóm để rà soát gồm:

1. **Shared/common và API nền:** `ApiResponse`, shared `ErrorResponse`, `DateUtils`, Jackson, security error handling.
2. **Identity/tenant/membership:** profile, membership, subscription/package date, frontend tenant flows.
3. **Messaging/takeover/routing:** message/conversation times, websocket/API consumers, async/event payload.
4. **Payment/invoice/merchant:** payment status/deposit dates, status/error mapping và webhook contract.
5. **Penny/analytics/training/customer/Facebook/MinIO:** timestamps, document/report endpoints, event payloads và integrations.
6. **Các hub/database còn lại:** kiểm tra toàn bộ Flyway locations và gRPC/shared DTO.

Mỗi domain phải dùng cùng checklist model–migration–DTO–error–consumer. Thứ tự deploy cụ thể phụ thuộc khả năng tương thích của từng consumer, không chỉ thứ tự danh sách trên.

Với migration có rủi ro hoặc cần rolling deploy, dùng **expand → backfill/verify → switch reads/writes → contract**: thêm cấu trúc tương thích trước, triển khai code đọc/ghi tương thích, chuyển dữ liệu có kiểm soát, rồi mới loại bỏ cột/field legacy. Không sửa migration đã chạy ở môi trường chia sẻ. Nếu thay đổi JSON không tương thích, version API hoặc duy trì field legacy trong thời gian deprecation đã công bố.

### Giai đoạn 4 — Đóng legacy và ngăn lệch lớp tái diễn

- Xóa compatibility fields/cột legacy sau khi xác nhận tất cả consumer đã chuyển và dữ liệu đã đối soát.
- Duy trì contract/integration tests cho DB migration, entity–DTO mapping, HTTP schema/status và consumer quan trọng.
- Thêm kiểm tra/review rule cho mismatch giữa kiểu Java và SQL, `new ObjectMapper()`, date format API tùy tiện, `Z` literal trên `LocalDateTime`, controller catch `Exception`, và endpoint trả trực tiếp entity.
- Cập nhật tài liệu API/domain và hướng dẫn backend; ghi lại các ngoại lệ có chủ đích cùng owner/điều kiện loại bỏ.

## 6. Tiêu chí hoàn thành đề xuất

- Mỗi field đã migrate có mapping truy vết **SQL column ↔ entity/model ↔ DTO/JSON ↔ consumer**, và semantics/timezone không mơ hồ.
- Kiểu Java, kiểu cột, precision/nullability/default và mapping đều khớp; migration mới chạy được từ schema thực tế được hỗ trợ.
- Timestamp API không phụ thuộc timezone host; `Z` chỉ xuất hiện khi instant thực sự là UTC; UI tự định dạng để hiển thị.
- Không còn timestamp API dùng format hiển thị `dd/MM/yyyy` thay cho dữ liệu máy đọc được, trừ field legacy đang trong thời gian deprecation.
- Lỗi MVC/security đi theo shared error schema và status có ý nghĩa; ngoại lệ webhook/protocol được định nghĩa/test riêng.
- Không còn controller âm thầm đổi mọi exception thành 400/404; exception nghiệp vụ/error code/status được kiểm tra bằng tests.
- Có migration/contract/integration tests và đối soát dữ liệu; deploy/rollback/compatibility plan được ghi nhận cho từng domain.

## 7. Tham chiếu mã nguồn đã kiểm tra

- Jackson: [`JacksonConfig`](backend/src/main/java/com/chatbot/config/JacksonConfig.java), [`DateUtils`](backend/src/main/java/com/chatbot/shared/utils/DateUtils.java)
- Response/error chung: [`ApiResponse`](backend/src/main/java/com/chatbot/shared/dto/ApiResponse.java), [`ErrorResponse`](backend/src/main/java/com/chatbot/shared/dto/ErrorResponse.java), [`GlobalExceptionHandler`](backend/src/main/java/com/chatbot/shared/exceptions/GlobalExceptionHandler.java), [`ErrorCodeMapper`](backend/src/main/java/com/chatbot/shared/exceptions/ErrorCodeMapper.java)
- Security/filter/migration: [`SecurityConfig`](backend/src/main/java/com/chatbot/config/SecurityConfig.java), [`JwtFilter`](backend/src/main/java/com/chatbot/core/identity/security/JwtFilter.java), [`TenantApiKeyAuthFilter`](backend/src/main/java/com/chatbot/core/tenant/guard/TenantApiKeyAuthFilter.java), [`TenantRateLimitFilter`](backend/src/main/java/com/chatbot/core/tenant/guard/TenantRateLimitFilter.java), [`MerchantAuthFilter`](backend/src/main/java/com/chatbot/core/payment/merchant/filter/MerchantAuthFilter.java), [`RateLimitingFilter`](backend/src/main/java/com/chatbot/shared/security/RateLimitingFilter.java), [`MultiFlywayConfig`](backend/src/main/java/com/chatbot/config/MultiFlywayConfig.java)
- Ví dụ model–DTO–migration: [`AppSubscription`](backend/src/main/java/com/chatbot/core/app/subscription/model/AppSubscription.java), [`SubscriptionResponse`](backend/src/main/java/com/chatbot/core/app/subscription/dto/SubscriptionResponse.java), [`app V1 baseline`](backend/src/main/resources/db/migration/app/V1__baseline.sql)
- Ví dụ endpoint: [`MessageController`](backend/src/main/java/com/chatbot/core/message/store/controller/MessageController.java), [`SimplePaymentController`](backend/src/main/java/com/chatbot/core/payment/transaction/controller/SimplePaymentController.java), [`PaymentStatusResponse`](backend/src/main/java/com/chatbot/core/payment/transaction/dto/PaymentStatusResponse.java), [`KnowledgeDocumentController`](backend/src/main/java/com/chatbot/core/penny/controller/KnowledgeDocumentController.java), [`FacebookWebhookController`](backend/src/main/java/com/chatbot/spokes/facebook/webhook/controller/FacebookWebhookController.java), [`AnalyticsController`](backend/src/main/java/com/chatbot/core/analytics/controller/AnalyticsController.java), [`ReportController`](backend/src/main/java/com/chatbot/core/analytics/controller/ReportController.java)
- Tests nền tảng đã có: [`JacksonUtcContractTest`](backend/src/test/java/com/chatbot/config/JacksonUtcContractTest.java), [`DateUtilsUtcTest`](backend/src/test/java/com/chatbot/shared/utils/DateUtilsUtcTest.java), [`TenantDatetimeContractTest`](backend/src/test/java/com/chatbot/core/tenant/dto/TenantDatetimeContractTest.java), [`ErrorResponseContractTest`](backend/src/test/java/com/chatbot/shared/dto/ErrorResponseContractTest.java)
