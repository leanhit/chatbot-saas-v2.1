/chatbot-saas-v2.1 là ứng dụng SaaS chatbot đa tenant gồm backend Spring Boot và dashboard Vue. Sản phẩm tích hợp quản lý tenant/người dùng, hội thoại và bot, Penny AI, Facebook cùng các dịch vụ vận hành như thanh toán, routing và supervisor.

Đánh giá dưới đây dựa trên việc rà soát rộng cấu trúc repo, tài liệu, cấu hình build/deploy và các module tiêu biểu. Đây không phải xác nhận đã đọc từng dòng của mọi file hoặc đã chạy build/test.

Stack
Ngôn ngữ: Java 21; JavaScript/Vue
Framework/runtime: Spring Boot 3.4, Vue 3
Thư viện/hạ tầng chính: Spring Data JPA, Spring Security/JWT, PostgreSQL/pgvector, Redis, RabbitMQ/Kafka, gRPC, Pinia
How it's organized
Text
backend/
  src/main/java/com/chatbot/core/
    identity, tenant, membership/    xác thực, tenant, quyền thành viên
    message/                         hội thoại, message, xử lý message
    customer/, training/             hồ sơ khách hàng và năng lực agent
    routing/, analytics/, ai/        routing, báo cáo, AI/sentiment
    penny/                           bot, middleware, RAG, provider LLM
    payment/, metrics/               thanh toán và quan sát hệ thống
  src/main/java/com/chatbot/spokes/  Facebook, MinIO và tích hợp ngoài
  src/main/resources/                cấu hình, migrations, SQL
  src/test/                          unit/integration tests; Testcontainers

frontend/
  src/api/                           API clients
  src/stores/, src/store/            trạng thái ứng dụng — hiện có hai thư mục
  src/router/                        route và navigation guards
  src/views/                         giao diện tenant, Penny, customer, routing, training

app-setup/                           PostgreSQL, Redis, RabbitMQ, Kafka, MinIO
app-deploy/                          image app, Nginx, cấu hình chạy
docs/                                runbook, RBAC, hợp đồng API/ops
.github/workflows/                   backend tests và deploy
Đánh giá và phương án cải tiến
Nhận định chung: repo có phạm vi tính năng lớn và nhiều nền tảng cần thiết, nhưng rủi ro hiện tại nằm ở an toàn cấu hình, tính nhất quán của phân quyền/tenant, ranh giới giữa module và độ tin cậy của CI, chứ không phải thiếu thêm tính năng. Nên ưu tiên làm ứng dụng dễ kiểm chứng và an toàn trước khi tách microservices hoặc tiếp tục mở rộng feature.

P0 — Bảo mật cấu hình và phát hành
Trong application.properties có giá trị mặc định nhạy cảm cho database/JWT/integration; repo cũng có file RSA private key. Nên coi các khóa/mật khẩu đã commit là đã lộ: xoay vòng chúng, chuyển sang GitHub Secrets/secret manager và tránh fallback về credential mặc định trong production. application.properties
SecurityConfig thêm nhiều origin pattern, gồm wildcard subdomain, đồng thời cho phép credentials. Cần xác định allowlist domain chính xác cho từng môi trường. Frontend gửi Accept-Language, nhưng danh sách CORS allowed headers được kiểm tra chưa liệt kê header đó; cần đối chiếu thêm X-API-Key cho các luồng sử dụng API key. SecurityConfig Axios plugin
Workflow test và deploy là hai workflow riêng. Dockerfile build backend bằng bootJar -x test, còn workflow test hiện chỉ chạy backend. Nên thêm frontend build, kiểm tra bảo mật/dependency và đặt deploy sau các job kiểm thử bắt buộc. Thay npm ci || npm install bằng cài đặt tái lập được để build không âm thầm bỏ qua lỗi lockfile. test workflow Dockerfile deploy workflow
P1 — Tenant isolation và phân quyền
Chốt một nguyên tắc: tenant ID lấy từ security context đã xác thực; mọi service/repository operation trên bot, customer, conversation, training, payment và report đều giới hạn tenant.
Audit matrix ghi nhận vẫn còn khoảng trống; TenantPermissionValidator.isTenantAdmin() hiện kiểm tra role OWNER, dù tên phương thức nói “tenant admin”. Cần thống nhất tên và semantics, đồng thời xác định cache role được invalidation thế nào khi membership thay đổi. RBAC matrix TenantPermissionValidator
Frontend guard chủ yếu kiểm tra đăng nhập/tenant; role check chỉ áp dụng ở một số route. Giữ frontend guard cho trải nghiệm, nhưng phải coi backend là lớp phân quyền thực sự và thêm test “tenant A không đọc/sửa/xóa dữ liệu tenant B”.
P1 — Kiến trúc và vận hành
Backend đang có nhiều hub/database, Kafka, RabbitMQ, Redis, gRPC và spokes trong cùng ứng dụng Spring. Điều đó tạo năng lực nhưng cũng làm cấu hình, transaction, test và vận hành phức tạp. Tài liệu kiến trúc đã ghi nhận spokes còn phụ thuộc chéo và chia sẻ EntityManager/database; nên tiếp tục theo hướng modular monolith với ranh giới DTO/event rõ, chưa cần vội tách thành nhiều service. Architecture improvements HubDatabaseConfig
Xác định nguồn schema duy nhất cho từng DB. Hiện có Flyway migrations, SQL init scripts và nhiều datasource; cần CI xác minh migrations từ DB rỗng và từ phiên bản đang chạy, tránh schema drift.
Hợp nhất frontend store/ và stores/ theo lộ trình; thống nhất API client, error handling và kiểu dữ liệu. router/index.js rất lớn và route metadata chưa có role đầy đủ; nên tách route theo feature và dùng kiểm tra quyền tập trung.
Chỉnh tài liệu cho sát code: frontend README vẫn mô tả Windzo template; ops runbook nói tất cả controller đều có @PreAuthorize và mô tả mã hóa API key AES, nhưng cần đối chiếu với triển khai thực tế trước khi dùng làm cam kết vận hành. Frontend README Ops runbook
P1 — Penny AI và xử lý hội thoại
Nghiệm thu riêng luồng Penny: xác thực quyền bot/tenant, context persistence, LLM timeout/fallback, RAG upload/reprocess và khả năng phục hồi khi Redis/provider lỗi.
Không coi sanitizer regex là cơ chế bảo vệ SQL hoặc prompt injection; dùng parameterized queries, giới hạn/kiểm soát dữ liệu gửi đến LLM, và tách nội dung tài liệu khỏi system instructions.
Kiểm tra health endpoint bằng trạng thái dependency thực, không chỉ báo healthy tĩnh; bảo đảm analytics batch xử lý đồng thời an toàn và không mất event khi flush lỗi.
Với routing/escalation/customer/training vừa hoàn thiện, bổ sung regression tests từ service tới repository/controller; test pass cần đi kèm assertion về side effect thực, không chỉ xác nhận object trả về.
P2 — Hiệu năng và độ ổn định
Đo trước khi tối ưu: connection pool tổng giữa các datasource, truy vấn conversation/message lớn, latency của report và vector search.
Thêm pagination/limits bắt buộc cho endpoint đọc danh sách; rà soát các luồng có tải toàn bộ entity rồi lọc trong Java.
Chuẩn hóa retry/idempotency cho webhook và job; theo dõi DLQ, lỗi provider, queue lag, migration và tenant quota qua dashboard/cảnh báo.
Giữ triển khai dạng một app container hiện tại nếu quy mô phù hợp; chỉ tách worker hoặc spokes sau khi có số liệu cho thấy một thành phần cần scale hoặc deploy độc lập.
Thứ tự thực hiện đề xuất
Bảo mật ngay: xoay secret đã commit, sửa CORS, rà soát public/API-key endpoints.
Chặn lỗi đa tenant: kiểm tra toàn bộ API/service nhạy cảm và test cross-tenant cho từng domain.
Làm CI thành cổng phát hành: backend tests + frontend build + migrations + security/dependency checks; deploy chỉ chạy sau khi pass.
Ổn định core: chuẩn hóa migration/datasource, Penny context/RAG, retry/idempotency và health thật.
Giảm chi phí bảo trì: chuẩn hóa frontend stores/API/routes, loại bỏ code/config trùng và cập nhật docs.
Tối ưu có số liệu: pool, query, cache, vector index, tách worker nếu cần.
How it fits together
Request đi qua Vue router và Axios interceptor, gửi JWT cùng tenant header tới Spring Security; các service gọi domain/repository tương ứng trên các datasource của hub. Penny xử lý message qua PennyMiddlewareEngine — context, intent, provider, analytics — còn RAG dùng parsing/chunking/embedding và pgvector; Facebook, MinIO và các spokes kết nối hệ thống ngoài. Ưu tiên cải tiến là đặt các ranh giới tenant/quyền và kiểm thử ở những đường nối này trước khi mở rộng thêm kiến trúc phân tán.

How to run it
Theo README và scripts trong repo, các đường chạy chính là:

bash
# Backend (Java 21; cần các dịch vụ/cấu hình ứng dụng)
cd backend
./gradlew bootRun

# Backend tests
./gradlew test

# Frontend
cd frontend
npm ci
npm run serve
npm run build

# Hạ tầng local
cd app-setup
docker compose up -d

# Build/chạy app đóng gói
cd app-deploy
./deploy.sh
Frontend dùng Vue CLI trên cổng 3003; backend mặc định cổng 8080. Backend yêu cầu Java 21 và cần PostgreSQL/Redis/RabbitMQ theo README; hạ tầng trong app-setup còn định nghĩa Kafka, MinIO và nhiều PostgreSQL hub. Chưa xác nhận các lệnh trên bằng cách chạy trực tiếp trong phiên review này.

Try asking
“Rà soát toàn bộ các endpoint thiếu tenant-scoped authorization.”
“Lập kế hoạch giảm các datasource và làm migration test nhất quán.”
“Đánh giá luồng Penny từ upload tài liệu đến câu trả lời RAG và viết test end-to-end.”
Approaching limit
You're approaching the limit of your included usage with the free plan.
Ask anything or type @ to add context


=====================================

Kế hoạch cải thiện chatbot-saas-v2.1
Dựa trên code_preview.md · Penny Bot P0/P1/P2 đã hoàn thành (12/11/2026, 874 tests ✅) Phạm vi: toàn bộ hệ thống ngoài Penny

Tổng quan thứ tự thực hiện

Tuần 1   │ P0-A: Credential & Secret rotation (NGAY HÔM NAY)
Tuần 1   │ P0-B: CORS hardening
Tuần 1-2 │ P0-C: CI/CD pipeline hardening
Tuần 2-3 │ P1-A: Tenant isolation toàn hệ thống (audit + test)
Tuần 3-4 │ P1-B: TenantPermissionValidator & RBAC
Tuần 4-5 │ P1-C: Frontend store/router consolidation
Tuần 5-6 │ P1-D: Datasource & Migration consolidation
Tuần 6+  │ P2:   Pagination, retry, monitoring
P0 — Bảo mật cấu hình & phát hành
CAUTION

Các vấn đề P0 ảnh hưởng trực tiếp đến bảo mật production. Thực hiện ngay, không chờ sprint.

P0-A: Credential & Secret đã commit vào repo
Vấn đề phát hiện trong application.properties:

Các credential đang hardcode làm default value (sẽ dùng nếu env var không được set):

Property	Giá trị mặc định nguy hiểm
app.datasource.*.password	chatbot_Admin_2025 (tất cả DB)
spring.security.jwt.secret	Hh52JKs3NlFlk7MpT6VYjIML9Zn7sgfhW67X7j3Xr8Y=
spring.security.jwt.rsa.private-key	Full RSA 2048-bit private key inline
spring.security.jwt.rsa.public-key	Full RSA public key inline
facebook.autoConnect.appSecret	51c0c2a53c21e6c09662ace9fa79a5ff
facebook.autoConnect.appId	1512336249760267
minio.access.key / minio.secret.key	minioadmin / minioadmin
spring.rabbitmq.username/password	admin / admin123
Công việc cần làm
1. Xoay vòng tất cả credential đã lộ:

DB password chatbot_Admin_2025 → tạo password mới cho production
JWT secret → generate lại bằng openssl rand -base64 32
RSA key pair → generate lại bằng openssl genrsa -out private.pem 2048
Facebook App Secret → regenerate trong Facebook Developer Console
MinIO admin credential → đổi ngay trên production server
RabbitMQ credential → đổi trên production server
2. Xóa default value khỏi application.properties:

properties

# TRƯỚC (nguy hiểm):
app.datasource.identity.password=${DB_PASSWORD:chatbot_Admin_2025}
# SAU (an toàn - fail nếu không set env var):
app.datasource.identity.password=${DB_PASSWORD}
3. RSA key — không để inline trong properties:

properties

# TRƯỚC (nguy hiểm - full key inline):
spring.security.jwt.rsa.private-key=${RSA_PRIVATE_KEY:-----BEGIN PRIVATE KEY-----MIIEv...}
# SAU — đọc từ file hoặc secret manager:
spring.security.jwt.rsa.private-key-path=${RSA_PRIVATE_KEY_PATH:/run/secrets/jwt_private.pem}
4. Thêm .gitignore rule:

gitignore

# Secrets
*.pem
*.key
application-production.properties
.env
.env.*
5. GitHub Actions — dùng Secrets:

yaml

env:
  DB_PASSWORD: ${{ secrets.DB_PASSWORD }}
  JWT_SECRET: ${{ secrets.JWT_SECRET }}
  RSA_PRIVATE_KEY: ${{ secrets.RSA_PRIVATE_KEY }}
  FACEBOOK_APP_SECRET: ${{ secrets.FACEBOOK_APP_SECRET }}
6. Scan git history để xoá credential cũ:

bash

# Dùng git-filter-repo hoặc BFG Repo Cleaner
# Sau đó force-push và invalidate tất cả deploy keys
File cần sửa
[MODIFY] backend/src/main/resources/application.properties — xóa tất cả default credential
[MODIFY] .gitignore — thêm rules bảo mật
[MODIFY] .github/workflows/*.yml — thêm secrets injection
Tiêu chí nghiệm thu
grep -r "chatbot_Admin_2025\|minioadmin\|admin123" . → không tìm thấy gì
Build fail nếu DB_PASSWORD env var không được set
RSA private key không có trong bất kỳ file nào được commit
P0-B: CORS hardening
Phân tích 
SecurityConfig.java
 thực tế:

java

// Dòng 32 — default origins nếu ALLOWED_ORIGINS env var không set:
"http://localhost,http://localhost:8080,
 https://*.truyenthongviet.vn,https://truyenthongviet.vn,
 https://*.startai.vn,https://startai.vn,
 http://103.149.99.7,http://103.149.99.7:8080,https://103.149.99.7"
// Dòng 63-69 — HARDCODE thêm vào bất kể env var:
origins.add("http://localhost:*");          // wildcard port
origins.add("http://103.149.99.7:*");       // wildcard port trên IP production
origins.add("https://103.149.99.7:*");      // wildcard port
origins.add("https://*.truyenthongviet.vn"); // wildcard subdomain
origins.add("https://*.startai.vn");         // wildcard subdomain
// Dòng 82:
config.setAllowCredentials(true); // ← kết hợp với wildcard = nguy hiểm
Vấn đề bổ sung phát hiện:

IP production 103.149.99.7 hardcode trong source code
allowedHeaders thiếu Accept-Language và X-API-Key (nhưng /api/tenant-api/** dùng X-API-Key auth)
15+ endpoint .permitAll() trong SecurityFilterChain — cần review từng endpoint
Danh sách endpoint .permitAll() cần review:

Endpoint	Mức rủi ro
/actuator/** — toàn bộ actuator public	🔴 Cao
/swagger-ui/**, /v3/api-docs/**	🟡 Trung bình
/api/simple-payment/test/**, /simulate-payment	🟡 Có cần public?
/api/tenant-api/**	🟢 Dùng X-API-Key auth
/api/locations/**	🟢 OK nếu dữ liệu public
Công việc cần làm
1. Xóa hardcode wildcard — đọc từ config:

java

// TRƯỚC (nguy hiểm, dòng 63-69):
if (!origins.contains("http://localhost:*")) origins.add("http://localhost:*");
if (!origins.contains("http://103.149.99.7:*")) origins.add("http://103.149.99.7:*");
// ...
// SAU — chỉ dùng ALLOWED_ORIGINS env var, không hardcode:
// Xóa toàn bộ khối if(!origins.contains...) 
// Chỉ giữ: config.setAllowedOriginPatterns(origins);
2. Externalize vào application.properties:

properties

# DEV (application.properties):
cors.allowed-origins=http://localhost:3003,http://localhost:8080
# PROD (via env var ALLOWED_ORIGINS):
# ALLOWED_ORIGINS=https://app.startai.vn,https://admin.startai.vn
3. Restrict actuator:

java

// TRƯỚC:
.requestMatchers("/actuator/**").permitAll()
// SAU:
.requestMatchers("/actuator/health").permitAll()       // chỉ health public
.requestMatchers("/actuator/**").hasRole("ADMIN")      // metrics/prometheus cần auth
4. Thêm Accept-Language và X-API-Key vào allowedHeaders:

java

config.setAllowedHeaders(List.of(
    "Authorization", "Content-Type",
    "X-Tenant-Key", "Cache-Control",
    "Accept-Language",   // ← thêm
    "X-API-Key"          // ← thêm (cho tenant-api endpoints)
));
File cần sửa
[MODIFY] 
SecurityConfig.java
 — dòng 63-69 (xóa hardcode) + dòng 136 (restrict actuator) + dòng 75-80 (thêm headers)
[MODIFY] application.properties — dùng cors.allowed-origins property chuẩn
Tiêu chí nghiệm thu
Không còn http://103.149.99.7 hay wildcard * hardcode trong source code
/actuator/prometheus trả 403 khi không có auth
SecurityConfigTest kiểm tra CORS reject origin không nằm trong allowlist
P0-C: CI/CD Pipeline hardening
Phân tích workflow thực tế:

test.yml
 (40 dòng):

✅ Chạy ./gradlew test cho backend
✅ Trigger cả push và pull_request lên main/master
❌ Chỉ test backend, không có frontend build/test
❌ Không upload test report artifact
deploy.yml
 (80 dòng):

❌ Chạy độc lập, KHÔNG có needs: test → deploy chạy song song với test, không chờ pass
❌ Trigger cùng event push: main → deploy có thể chạy dù test fail
❌ Build Docker image trực tiếp (Dockerfile dùng -x test)
⚠️ Dùng docker compose pull || true và docker login ... || true — lỗi bị bỏ qua
Công việc cần làm
1. Merge 2 workflow thành pipeline có thứ tự bắt buộc:

yaml

# .github/workflows/ci-cd.yml (THAY THẾ cả test.yml và deploy.yml)
name: CI/CD Pipeline
on:
  push:
    branches: [main, master]
  pull_request:
    branches: [main, master]
jobs:
  backend-test:
    name: Backend Tests
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: backend
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: '21', cache: gradle }
      - run: chmod +x gradlew
      - name: Run tests
        run: ./gradlew test --no-daemon
        env:
          TESTCONTAINERS_RYUK_DISABLED: "false"
      - name: Upload test report
        if: always()  # Upload kể cả khi test fail
        uses: actions/upload-artifact@v4
        with:
          name: backend-test-results
          path: backend/build/reports/tests/
  frontend-build:
    name: Frontend Build
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: frontend
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: '20', cache: 'npm', cache-dependency-path: frontend/package-lock.json }
      - name: Install (strict — không fallback)
        run: npm ci
      - name: Build
        run: npm run build
  deploy:
    name: Build & Deploy
    needs: [backend-test, frontend-build]  # ← BẮT BUỘC cả 2 pass
    if: github.ref == 'refs/heads/main' && github.event_name == 'push'
    runs-on: ubuntu-latest
    env:
      SERVER_HOST: ${{ secrets.SERVER_HOST }}
      SERVER_USER: ${{ secrets.SERVER_USER }}
      SERVER_SSH_KEY: ${{ secrets.SERVER_SSH_KEY }}
      SERVER_PORT: ${{ secrets.SERVER_PORT }}
      SERVER_PATH: ${{ secrets.SERVER_PATH }}
    steps:
      - uses: actions/checkout@v4
      # ... (giữ nguyên các bước build Docker + SSH deploy từ deploy.yml cũ)
      # Nhưng BỎ các `|| true` che giấu lỗi:
      - name: Docker login (fail nếu lỗi)
        run: echo "${{ secrets.GITHUB_TOKEN }}" | docker login ghcr.io -u "${{ github.actor }}" --password-stdin
2. Kiểm tra Dockerfile — xóa -x test:

bash

grep -n 'gradlew' app-deploy/Dockerfile
# Nếu có: ./gradlew bootJar -x test → đổi thành: ./gradlew bootJar
File cần sửa
[NEW] .github/workflows/ci-cd.yml — pipeline mới thay thế cả 2
[DELETE] .github/workflows/test.yml — sau khi ci-cd.yml hoạt động
[DELETE] .github/workflows/deploy.yml — sau khi ci-cd.yml hoạt động
[MODIFY] app-deploy/Dockerfile — bỏ -x test nếu có
Tiêu chí nghiệm thu
git push main với test fail → deploy job ở trạng thái "skipped", không chạy
Pull request phải pass cả backend-test + frontend-build mới được merge
Test report được upload lên GitHub Actions artifacts sau mỗi run
P1 — Tenant Isolation & Phân quyền
IMPORTANT

Đây là điều kiện bắt buộc trước production. Tenant A không được phép truy cập dữ liệu Tenant B ở BẤT KỲ endpoint nào.

P1-A: Tenant isolation audit toàn hệ thống
Vấn đề: Chưa có kiểm tra có hệ thống xem tất cả service/repository có lọc theo tenant không. Penny đã fix, phần còn lại chưa rà soát đầy đủ.

Trạng thái hiện tại (13/11/2026):
✅ Đã hoàn thành audit và fix cho tất cả HIGH priority domains:
- Invoice: Thêm tenant-scoped finders, controller scoped
- SimplePayment: Thêm findByReferenceCodeAndTenantId, OrderLookupService scoped, thêm existsByReferenceCodeAndTenantId + analytics methods scoped
- simulate-payment: Xóa khỏi permitAll, restrict to SYSTEM_ADMIN
- MerchantAuthFilter: Thêm TenantContext.setTenantId() sau API key validation
- MerchantPaymentSession: Thêm findBySessionIdAndTenantId, deleteByMerchantIdAndTenantId, service scoped
- AppSubscription: Thêm tenant-scoped finders, service scoped
- AppGuard: Thêm findByAppIdAndTenantId, service scoped
- PackageUpgradeAudit: Thêm tenant-scoped finders
- EnvironmentConfig: Thêm tenant-scoped finders cho tất cả methods
- RuntimeConfig: OK — ConfigScope design (GLOBAL/TENANT/USER) với tenant-aware queries
- FacebookConnection: OK — unscoped methods legit cho webhook/scheduler
- Agent: OK — findByUserId deprecated với tenant-scoped alternative
- Config/Address: OK — global by design hoặc đã scoped

⏸️ Cross-tenant isolation tests: Pending (cần setup test infrastructure cho multi-datasource architecture)

Full audit report: docs/tenant-isolation-audit.md

Phạm vi cần audit
Domain	Service	Repository	Trạng thái
core/penny/	PennyBotService, DocumentProcessingService	PennyBotRepository, DocumentRepository	✅ Đã fix
core/message/	ConversationService, MessageService	ConversationRepository	✅ Đã scoped (90%+)
core/customer/	CustomerService	CustomerRepository	✅ Đã scoped hoàn toàn
core/routing/	RoutingService, EscalationService	RoutingRepository	✅ Hầu hết đã scoped
core/training/	TrainingService	TrainingRepository	❓ Cần kiểm tra
core/analytics/	AnalyticsService	AnalyticsRepository	❓ Cần kiểm tra
core/payment/	PaymentService	PaymentRepository	✅ Đã fix (Invoice, SimplePayment, Merchant, AppSubscription, AppGuard, PackageUpgradeAudit)
core/config/	EnvironmentConfigService, RuntimeConfigService	EnvironmentConfigRepository, RuntimeConfigRepository	✅ Đã fix (EnvironmentConfig) / OK (RuntimeConfig)
spokes/facebook/	FacebookService	FacebookConnectionRepository	✅ OK — unscoped methods legit cho webhook/scheduler
Công việc cần làm
1. Viết script audit tự động:

bash

# Tìm tất cả repository method không có tenant filter
grep -rn "findAll()\|findById(" backend/src/main/java/com/chatbot/core/ \
  | grep -v "penny\|test" \
  | grep -v "tenantId\|tenant_id"
2. Với mỗi domain, kiểm tra:

Repository: có findByIdAndTenantId() thay vì findById()?
Service: lấy tenantId từ TenantContext.getTenantId() hay từ request?
Controller: có @PreAuthorize kiểm tra tenant?
3. Thêm cross-tenant test cho từng domain:

java

@Test
void tenantA_cannotRead_tenantB_conversations() {
    // Setup: tạo conversation của tenant B
    // Act: request với TenantContext = tenant A
    // Assert: kết quả rỗng hoặc 403
}
4. Xem xét thêm @PostFilter hoặc Spring Data Specification cho các query phức tạp:

java

// Cách tiếp cận an toàn nhất:
interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    // KHÔNG dùng findAll() - luôn filter by tenant
    List<Conversation> findAllByTenantId(Long tenantId);
    Optional<Conversation> findByIdAndTenantId(UUID id, Long tenantId);
}
File cần sửa (sau audit)
[MODIFY] core/message/repository/*Repository.java — thêm tenantId filter
[MODIFY] core/customer/repository/*Repository.java — thêm tenantId filter
[MODIFY] core/routing/repository/*Repository.java — thêm tenantId filter
[NEW] core/*/TenantIsolationTest.java cho từng domain
Tiêu chí nghiệm thu
Không tồn tại findAll() không có tenant scope trong production code
Mỗi domain có ít nhất 1 cross-tenant isolation test
Test: Tenant A không đọc/sửa/xóa dữ liệu Tenant B ở tất cả endpoints
P1-B: TenantPermissionValidator & RBAC nhất quán
Vấn đề:

isTenantAdmin() kiểm tra role OWNER nhưng tên nói "admin" → semantics không nhất quán
Role cache chưa rõ invalidation khi membership thay đổi
Công việc cần làm
1. Làm rõ semantics isTenantAdmin():

java

// TRƯỚC (tên vs logic không khớp):
public boolean isTenantAdmin(Long tenantId) {
    return hasRole(tenantId, Role.OWNER); // Tại sao "admin" check "OWNER"?
}
// SAU — tách rõ 2 method:
public boolean isTenantOwner(Long tenantId) {
    return hasRole(tenantId, Role.OWNER);
}
public boolean isTenantAdmin(Long tenantId) {
    return hasRole(tenantId, Role.ADMIN) || hasRole(tenantId, Role.OWNER);
}
2. Cache invalidation khi membership thay đổi:

java

@CacheEvict(cacheNames = "tenantPermissions", key = "#userId + ':' + #tenantId")
public void removeMember(Long tenantId, String userId) { ... }
@CacheEvict(cacheNames = "tenantPermissions", allEntries = true)
public void updateMemberRole(Long tenantId, String userId, Role newRole) { ... }
3. Cập nhật RBAC matrix trong docs/ để khớp với code.

4. Thêm test:

java

@Test void ownerIsAlsoAdmin() { ... }
@Test void memberIsNotAdmin() { ... }
@Test void cacheInvalidatedOnRoleChange() { ... }
File cần sửa
[MODIFY] 
TenantPermissionValidator.java
[MODIFY] MembershipService.java — thêm @CacheEvict
[MODIFY] docs/rbac-matrix.md — sync với code
Tiêu chí nghiệm thu
isTenantAdmin() và isTenantOwner() có semantics rõ ràng, có test
Thay đổi role → cache bị xóa → request tiếp theo check fresh từ DB
P1-C: Frontend store/router consolidation
Phân tích thực tế:

src/store/ (3 file nhỏ — dễ migrate):


src/store/
  fullscreen.js   (295 bytes)  — toggle fullscreen state
  index.js        (88 bytes)   — Vuex store entry point (Vuex! không phải Pinia)
  sidebar.js      (754 bytes)  — sidebar collapse state
src/stores/ (Pinia — modern, đây là store chính):


src/stores/
  authStore.js          (14978 bytes)  — auth, JWT, user info
  paymentStore.js       (25140 bytes)  — payment state
  pennyBotStore.js      (8750 bytes)   — Penny Bot management
  pennyConnectionStore.js (6357 bytes) — Penny connections
  pennyRuleStore.js     (4485 bytes)   — Penny rules
  searchStore.js        (2303 bytes)   — search
  locationStore.js      (1079 bytes)   — location
  tenant/              — tenant stores
  notification/        — notification stores
NOTE

src/store/ dùng Vuex (cũ), src/stores/ dùng Pinia (mới). Hai hệ thống state khác nhau đang chạy song song. Việc consolidate cần đổi store/ sang Pinia.

Công việc cần làm
1. Migrate src/store/ → Pinia (3 file, nhanh):

javascript

// store/sidebar.js (Vuex) → stores/uiStore.js (Pinia)
import { defineStore } from 'pinia'
export const useUiStore = defineStore('ui', {
  state: () => ({
    sidebarCollapsed: false,
    isFullscreen: false,
  }),
  actions: {
    toggleSidebar() { this.sidebarCollapsed = !this.sidebarCollapsed },
    toggleFullscreen() { this.isFullscreen = !this.isFullscreen },
  }
})
2. Tìm và thay toàn bộ import từ store/:

bash

grep -rn "from.*['\"].*store/" frontend/src --include='*.vue' --include='*.js'
# Thay từng import sang Pinia equivalent
3. Xóa Vuex khỏi dependencies sau khi migrate xong:

bash

npm uninstall vuex
4. Thêm role guard vào router (cần đọc router/index.js để biết cấu trúc hiện tại):

javascript

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isAuthenticated) return '/login'
  if (to.meta.roles?.length && !auth.hasAnyRole(to.meta.roles)) return '/403'
})
File cần sửa
[NEW] frontend/src/stores/uiStore.js — merge fullscreen + sidebar từ Vuex
[DELETE] frontend/src/store/ — sau khi migrate
[MODIFY] frontend/src/main.js — bỏ Vuex store registration
[MODIFY] frontend/src/router/index.js — thêm role guard
[MODIFY] frontend/package.json — xóa vuex dependency
Tiêu chí nghiệm thu
grep -r "from.*store/index" frontend/src → không còn kết quả
npm ls vuex → package không tồn tại
Sidebar và fullscreen hoạt động bình thường sau migrate
P1-D: Datasource & Migration consolidation
Vấn đề:

Flyway chỉ được bật cho message database, các hub khác dùng hibernate.ddl-auto
Hai nguồn schema: Flyway migrations + SQL init scripts trong app-setup/
CI không test migrations từ DB rỗng
Công việc cần làm
1. Bật Flyway cho tất cả hub databases:

properties

# application.properties — thêm config cho từng hub
spring.flyway.locations=classpath:db/migration/message,\
                        classpath:db/migration/identity,\
                        classpath:db/migration/tenant,\
                        classpath:db/migration/user
2. Tạo migration folder cho từng hub:


backend/src/main/resources/db/migration/
  message/     ← đã có
  identity/    ← tạo mới
  tenant/      ← tạo mới  
  user/        ← tạo mới
  app/         ← tạo mới
3. Đặt hibernate.ddl-auto=validate cho production:

properties

# application-production.properties
app.hibernate.ddl-auto=validate
4. Test migration CI — chạy từ DB rỗng:

yaml

- name: Test migration from scratch
  run: |
    ./gradlew test -Dspring.profiles.active=test-migrate
5. Đồng bộ SQL init scripts trong app-setup/ với Flyway migrations — chọn một nguồn duy nhất.

File cần sửa
[MODIFY] application.properties — extend flyway locations
[NEW] db/migration/identity/V1__init.sql etc.
[MODIFY] .github/workflows/ci.yml — thêm migration test
Tiêu chí nghiệm thu
CI chạy flyway migrate từ DB rỗng và pass
Production dùng hibernate.ddl-auto=validate (không tự tạo bảng)
Không có schema khác nhau giữa staging và production
P2 — Hiệu năng & Độ ổn định
NOTE

Thực hiện sau P0/P1. Đo metric trước, tối ưu sau — không premature optimization.

P2-A: Pagination bắt buộc
Vấn đề: Các endpoint danh sách không có giới hạn mặc định — một tenant có nhiều dữ liệu có thể gây OOM.

Công việc cần làm
1. Thêm Pageable với default size cho tất cả list endpoint:

java

// TRƯỚC:
@GetMapping("/conversations")
public List<Conversation> getAll() {
    return service.findAll(); // Nguy hiểm!
}
// SAU:
@GetMapping("/conversations")
public Page<ConversationDto> getAll(
    @PageableDefault(size = 50, max = 200) Pageable pageable) {
    return service.findAll(tenantId, pageable);
}
2. Config global max page size:

java

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
        resolver.setMaxPageSize(200);  // Hard limit
        resolver.setFallbackPageable(PageRequest.of(0, 50));
        resolvers.add(resolver);
    }
}
P2-B: Retry & Idempotency cho webhook
Vấn đề: Webhook delivery và async job chưa có idempotency key → retry gây duplicate processing.

Công việc cần làm
1. Thêm idempotency key cho webhook:

java

// Lưu `deliveryId` trước khi process, skip nếu đã processed
@Transactional
public void processWebhook(WebhookEvent event) {
    if (deliveryRepo.existsByDeliveryId(event.getDeliveryId())) {
        log.info("Duplicate webhook delivery {}, skipping", event.getDeliveryId());
        return;
    }
    deliveryRepo.save(new WebhookDelivery(event.getDeliveryId()));
    // ... process
}
2. Theo dõi DLQ: Tạo alert khi DLQ có message.

P2-C: Connection pool sizing
Vấn đề: Hiện tổng pool size = 50(user) + 20(tenant) + 30(message) + 5(identity) + 5(app) + 3(config) = 113 connections → nhiều hơn PostgreSQL default max (100).

Công việc cần làm
1. Tính lại pool size phù hợp:


PostgreSQL max_connections = 200 (đặt trong postgresql.conf)
Tổng pool = số replicas × (pool_per_datasource)
Công thức: pool_size = (core_count × 2) + disk_spindles
2. Cân nhắc PgBouncer (đã có hướng dẫn trong PgBouncer_Setup_Guide.md).

3. Monitor pool usage qua Prometheus/Grafana — đã enable management.metrics.enable.hikari=true.

Thứ tự thực hiện & Timeline
Tuần	Việc	Blocking?
1	P0-A: Rotate tất cả secrets	🔴 Ngay hôm nay
1	P0-B: CORS hardening	🔴 Ngay
1-2	P0-C: CI/CD pipeline	🟠 Trước sprint tiếp
2-3	P1-A: Tenant isolation audit	🟠 Trước production
3-4	P1-B: RBAC nhất quán	🟡 Quan trọng
4-5	P1-C: Frontend store/router	🟡 Quan trọng
5-6	P1-D: Migration consolidation	🟡 Quan trọng
6+	P2-A/B/C	🟢 Sau P0/P1
Open Questions
IMPORTANT

Cần trả lời trước khi thực hiện P1-A:

Domain nào ưu tiên audit trước? (message/routing/customer/training)
Target PostgreSQL max_connections? Để tính lại pool size.
Frontend framework: Vue CLI hay đang migrate sang Vite? (ảnh hưởng router setup)
Secret manager: Dùng GitHub Secrets thuần, hay tích hợp Vault/AWS Secrets Manager?
Penny Bot P0/P1/P2: ✅ DONE (12/11/2026, 874 tests) Kế hoạch này: chatbot-saas-v2.1 toàn hệ thống