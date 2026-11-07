. Dưới đây là kế hoạch hoàn thiện nâng cấp app theo code và improvement_plan.md hiện có. Tôi đề xuất tiếp tục Phase 5–6 song song có kiểm soát, nhưng vẫn giữ Phase 4 ở trạng thái blocked cho đến khi các luồng routing được kiểm chứng.

1. Chuẩn hóa cách gọi phase trước
improvement_plan.md đang dùng hai cách đánh số: phần tóm tắt gọi routing là Phase 4, còn lộ trình backend chi tiết gọi routing là Phase 3. Để tránh nhầm, kế hoạch dưới đây dùng tên nhóm tính năng:

Nền tảng và đo lường
Năng lực agent
Hiển thị khách hàng và trải nghiệm
Automation và routing
AI và analytics
Scale, bảo mật và tích hợp
2. Kế hoạch theo thứ tự ưu tiên
Giai đoạn 0 — Chốt trạng thái và tiêu chí nghiệm thu
Việc cần làm

Chọn một roadmap đánh số duy nhất và cập nhật các tài liệu Phase 4 cho thống nhất.
Phân loại từng hạng mục thành Done, In progress, Blocked, Not started.
Không đánh dấu test @Disabled hoặc SKIPPED là đã pass.
Tạo CI riêng chạy build, unit tests và integration tests; workflow deploy hiện được kiểm tra tập trung vào build/deploy.
Điều kiện hoàn tất

Mỗi hạng mục có đường dẫn code, test tương ứng và trạng thái test thực tế.
Báo cáo lỗi ghi nguyên nhân gốc từ log, không chỉ ghi ApplicationContext failure threshold.
Giai đoạn 1 — Củng cố nền tảng đa tenant và bảo mật
Ưu tiên phần này trước các tính năng AI tự động, vì dữ liệu khách hàng, agent và routing đều phải được cách ly đúng tenant.

Việc cần làm

Rà soát các API/service nhận tenantId nhưng truy vấn không giới hạn tenant.
Sửa các truy vấn training đang dùng findAll() dù có agentId/tenantId; gán tenant đúng khi lưu dữ liệu skill.
Rà soát truy vấn theo ID trong routing, escalation, supervisor, customer: kiểm tra tenant ở cả read/update/delete.
Bổ sung kiểm tra phân quyền phía backend; không chỉ dựa vào route guard frontend.
Xác định dữ liệu nào cần audit và ghi nhận các thao tác nhạy cảm.
Điều kiện hoàn tất

Có test xác nhận tenant A không đọc, sửa hoặc xóa dữ liệu tenant B.
API nhạy cảm có kiểm tra quyền phía server.
Không còn truy vấn toàn cục findAll() trong luồng cần lọc agent hoặc tenant.
Giai đoạn 2 — Hoàn thiện Phase 4: routing, escalation, supervisor
Không cần ngừng các công việc Phase 5–6, nhưng các thành phần tự động hóa phụ thuộc vào routing phải chờ qua cổng kiểm thử này.

Việc cần làm

Sửa lỗi và chốt contract

Rà soát điều kiện >=/<= trong IntelligentRoutingService: nhánh >/< hiện đứng trước, cần bảo đảm toán tử hai ký tự được xử lý đúng.
Xác nhận nghiệp vụ fallback của skill routing: hiện có thể chọn agent không có skill khi không tìm thấy agent phù hợp.
So khớp endpoint, DTO và payload giữa controller, service và frontend API.
Xác minh conversation được gán phải thuộc tenant hiện tại.
Tách test theo lớp

Unit tests: Mockito, chạy thường xuyên, không cần Spring context.
Repository integration: dùng @DataJpaTest với cấu hình DB tối thiểu; ưu tiên PostgreSQL trong CI để kiểm tra migration và hành vi PostgreSQL-specific.
Controller: @WebMvcTest với service mock và cấu hình web/security tối thiểu.
Smoke tests: chỉ bật khi các dependency cần thiết đã có cấu hình test rõ ràng.
Gỡ @Disabled sau khi test thực sự chạy được; không dùng annotation này làm cách đánh dấu hoàn thành.
Tổ chức môi trường test

Tránh load toàn bộ ChatbotApplication cho test repository/controller.
Không cần khởi tạo Redis/Kafka/RabbitMQ/gRPC nếu test không dùng chúng.
Nếu máy developer không có Docker, chạy PostgreSQL integration trong CI hoặc môi trường test có DB; H2 chỉ phù hợp với các mapping/query tương thích, không thay thế đầy đủ test PostgreSQL migration.
Điều kiện hoàn tất

Unit tests pass.
Repository tests chạy thật trên DB test.
Có test tenant isolation thực thi, không bị disable.
Có ít nhất một smoke flow đã chạy qua routing, escalation và supervisor.
Lỗi test được phân tích từ exception đầu tiên trong log.
Giai đoạn 3 — Hoàn thiện năng lực agent
Việc cần làm

Hoàn thiện truy vấn theo agent/tenant cho training, onboarding, skill và certification.
Triển khai đầy đủ các quy tắc trong kế hoạch:
hoàn thành training cập nhật skill score;
certification được cấp theo điều kiện/threshold;
onboarding cần hoàn tất trước khi agent được chuyển sang trạng thái online.
So khớp frontend API với backend contract; kiểm tra trạng thái tải, lỗi và quyền thao tác.
Điều kiện hoàn tất

Có test cho từng quy tắc nghiệp vụ và quyền truy cập theo tenant.
Tạo, đọc, cập nhật tiến độ training hoạt động qua API thật, không chỉ qua giao diện.
Giai đoạn 4 — Hoàn thiện trải nghiệm khách hàng
Việc cần làm

Hoàn thiện profile detail, history/timeline, notes, feedback và liên kết feedback với conversation.
Sửa luồng frontend customer hiện còn dang dở: viewCustomer chỉ log; luồng edit cần giữ đúng ID; kiểm tra lại thao tác delete để không gọi nhầm cập nhật thống kê.
Xác nhận customer segmentation/VIP có dữ liệu và business rule thật, không chỉ là phần giao diện.
Đảm bảo mọi truy vấn customer và feedback đều giới hạn tenant.
Điều kiện hoàn tất

CRUD và customer history chạy end-to-end.
Feedback gắn đúng customer/conversation và có test.
Tenant khác không truy cập được customer profile hoặc feedback.
Giai đoạn 5 — Xây AI và analytics theo hướng an toàn
Có thể bắt đầu trước khi toàn bộ Phase 4 hoàn tất, nhưng nên giới hạn tính năng AI ở mức hỗ trợ, chưa tự động điều khiển routing.

Thứ tự triển khai

Chốt event/data model cho conversation, feedback và routing.
Chuẩn hóa analytics lưu trong DB theo tenant; xác định retention và cách tổng hợp.
Triển khai AI provider abstraction, cấu hình provider và xử lý timeout/lỗi.
Làm AI suggestion có confidence, ghi nhận accept/reject và phản hồi của agent.
Làm sentiment scoring; ban đầu chỉ hiển thị/cảnh báo, chưa tự động escalate.
Chỉ sau khi Phase 4 qua kiểm thử mới cho phép sentiment hoặc AI kích hoạt hành động tự động.
Điều kiện hoàn tất

Analytics truy vấn theo tenant và khoảng thời gian.
AI failure không làm hỏng luồng hội thoại.
Có kiểm soát dữ liệu nhạy cảm, logging phù hợp và test cho các trường hợp provider lỗi.
Không tự động thay đổi assignment/escalation trước khi routing được xác minh.
Giai đoạn 6 — Scale, compliance và tích hợp
Một số việc bảo mật/compliance không nên để đến cuối nếu app đã xử lý dữ liệu khách hàng.

Ưu tiên

Sớm: audit log, phân quyền backend, quản lý secrets, tenant quota/rate limit.
Tiếp theo: retention và quy trình xóa/xuất dữ liệu, API key management, webhook có xác thực chữ ký.
Sau đó: retry/idempotency, dead-letter queue, monitoring và cảnh báo.
2FA/GDPR cần được ưu tiên theo yêu cầu pháp lý và thị trường triển khai; không mặc định là “nice-to-have”.
Điều kiện hoàn tất

Có kiểm thử quyền truy cập, audit và tenant quota.
Webhook có xác thực, retry và chống xử lý trùng.
Có hướng dẫn vận hành, cảnh báo và quy trình xử lý sự cố.
3. Thứ tự thực hiện đề xuất
Có thể triển khai theo ba luồng:

Luồng A — Nền tảng và an toàn dữ liệu: tenant isolation, RBAC, audit; bắt đầu ngay.
Luồng B — Hoàn thiện sản phẩm: training và customer experience; sửa các lỗi truy vấn/luồng đã phát hiện.
Luồng C — Phase 5: analytics và AI suggestion ở chế độ hỗ trợ; không tự động điều khiển routing.
Trong khi đó, Phase 4 được xử lý như một cổng bắt buộc trước khi bật automation, không nhất thiết là lý do phải dừng mọi phát triển.

4. Cổng phát hành
Có thể tiếp tục phát triển: unit tests pass và feature mới có test phù hợp.
Có thể đưa lên staging: tenant isolation và repository/API integration cần thiết đã chạy pass trên môi trường test.
Có thể bật automation/AI actions: routing, escalation, supervisor smoke tests và tenant isolation đã được xác minh.
Có thể xem xét production: thêm security/compliance review, migration test trên PostgreSQL, monitoring và rollback plan.
Tóm lại: làm Phase 5–6 trước là được, nhưng hãy bắt đầu bằng tenant/security nền tảng; triển khai AI theo kiểu hỗ trợ, chưa tự động hóa hành động. Đồng thời tiếp tục gỡ blocker của Phase 4 và không đánh dấu Phase 4 hoàn tất cho đến khi các test integration quan trọng thực sự chạy pass.

====================================

❌ CHƯA hoàn thành
Giai đoạn	Còn thiếu
0 — Chốt trạng thái	✅ Đã chuẩn hóa roadmap numbering (canonical Giai đoạn 0–6 trong improvement_plan.md); còn thiếu CI riêng cho unit+integration tests
1 — Tenant/security foundation	✅ Đã rà soát & fix findAll() (training sạch, payment analytics/merchant keys/file metadata đã tenant-scope); còn thiếu test tenant isolation thực thi
2 — Phase 4 routing	Unit tests ✅ nhưng repository tests trên DB thật ❌, tenant isolation tests ❌, smoke flow ❌ — vẫn blocked như plan yêu cầu
3 — Năng lực agent	✅ Hoàn thành: training→skill score, certification threshold (skills+training+stats, hỗ trợ max:), onboarding→online gate (API + WebSocket), tenant-scoped, unit tests pass
4 — Customer experience	Profile detail, history/timeline, notes, feedback linking, sửa frontend (viewCustomer chỉ log, edit ID, delete), segmentation/VIP — chưa làm
