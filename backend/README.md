# Chatbot SaaS v2.1 Backend

Multi-tenant Chatbot SaaS Platform with Hub & Spoke Architecture.

## 📁 Tài liệu Dự án (Project Documentation)

Tất cả tài liệu kỹ thuật và hướng dẫn vận hành hệ thống đã được tổ chức ngăn nắp trong thư mục [backend/docs](docs/):

*   **Kiến trúc & Cấu trúc:**
    *   [PROJECT_STRUCTURE.md](docs/PROJECT_STRUCTURE.md) - Cấu trúc chi tiết của toàn bộ mã nguồn Backend.
    *   [TENANT_GRPC_README.md](docs/TENANT_GRPC_README.md) - Hướng dẫn và thiết kế kết nối liên Hub qua gRPC.
*   **Vận hành & Hướng dẫn:**
    *   [README_SIMPLE_PAYMENT.md](docs/README_SIMPLE_PAYMENT.md) - Hướng dẫn cấu hình cổng thanh toán chuyển khoản tinh gọn (SimplePayment).
    *   [README_JWT_RS256.md](docs/README_JWT_RS256.md) - Hướng dẫn cấu hình chữ ký số bảo mật JWT với thuật toán RS256.
    *   [ssl-setup-instructions.md](docs/ssl-setup-instructions.md) - Quy trình cấu hình và cài đặt chứng chỉ bảo mật SSL.
*   **Kế hoạch & Checklist Triển khai (Production-Ready):**
    *   [PRODUCTION_DEPLOYMENT_CHECKLIST.md](docs/PRODUCTION_DEPLOYMENT_CHECKLIST.md) - Danh sách kiểm tra trước khi đưa hệ thống lên môi trường Production.
    *   [PRODUCTION_UPGRADE_PLAN.md](docs/PRODUCTION_UPGRADE_PLAN.md) - Kế hoạch chi tiết nâng cấp hạ tầng lên Production.
    *   [PRODUCTION_UPGRADE_GUIDE.md](docs/PRODUCTION_UPGRADE_GUIDE.md) - Cẩm nang chi tiết hướng dẫn các bước nâng cấp và bảo trì.
    *   [SECURITY_HARDENING.md](docs/SECURITY_HARDENING.md) - Các bước tăng cường bảo mật cho toàn bộ hệ thống.
    *   [COMPLETION_REPORT.md](docs/COMPLETION_REPORT.md) - Báo cáo tổng kết hoàn thiện hạ tầng Backend.

## 🚀 Hướng dẫn khởi chạy nhanh (Quick Start)

### Yêu cầu hệ thống
*   Java 21 (JDK 21) hoặc cao hơn.
*   PostgreSQL & Redis.
*   RabbitMQ.

### Chạy ứng dụng ở môi trường Phát triển (Development)
```bash
./gradlew bootRun
```
Ứng dụng sẽ tự động chạy trên cổng `8080` và tài liệu API Swagger sẽ có sẵn tại: `http://localhost:8080/swagger-ui/index.html`.

## 🤖 Penny Package - Intelligent Middleware

The Penny package provides intelligent routing, context management, and analytics for the chatbot system.

### Key Features (v1.5.0)
- **Unicode-aware Vietnamese Intent Analysis**: Case-insensitive patterns for accented and unaccented Vietnamese text
- **Cost-aware Provider Selection**: Estimate and compare costs across different AI providers
- **Enhanced Monitoring**: Spring Actuator metrics integration for request tracking and error monitoring
- **Admin Metrics Endpoints**: Comprehensive monitoring APIs for provider health and cost metrics
- **Async Processing**: CompletableFuture support for non-blocking intent analysis
- **Response Caching**: Caffeine-based caching for common query responses
- **Rate Limiting**: Bucket4j token bucket algorithm for API endpoint protection
- **Retry with Exponential Backoff**: Resilient error handling with automatic retries

### Configuration

Enable Penny features in `application.yml`:
```yaml
penny:
  enabled: true
  analytics:
    enabled: true
  provider:
    selection-strategy: hybrid
```

### API Endpoints

#### Admin Metrics
- `GET /api/penny/admin/metrics` - System-wide metrics
- `GET /api/penny/admin/metrics/providers` - Provider health status
- `GET /api/penny/admin/metrics/providers/costs` - Provider cost comparison

#### Monitoring Metrics
- `penny_requests_total` - Total requests processed
- `penny_errors_total` - Total processing errors

Access metrics via Actuator: `GET /actuator/prometheus`

### Testing

Run Penny-specific tests:
```bash
./gradlew test --tests "*penny*"
```

### Documentation

See [docs/api/penny-hub.md](docs/api/penny-hub.md) for complete API documentation.
