# SimplePayment Module

Hệ thống thanh toán đơn giản qua chuyển khoản ngân hàng với QR Code cho Chatbot SaaS Platform.

## Tổng quan

SimplePayment module cung cấp giải pháp thanh toán qua chuyển khoản ngân hàng với các tính năng:
- Tạo yêu cầu thanh toán với QR Code
- Tự động kiểm tra trạng thái thanh toán từ ngân hàng
- Hỗ trợ mua gói dịch vụ (package upgrade)
- Hỗ trợ mã giảm giá (discount code)
- Audit logging đầy đủ cho tất cả operations
- Metrics và monitoring tích hợp
- Retry logic với exponential backoff
- Rate limiting cho endpoints công khai
- Webhook signature verification

## Cấu trúc Module

```
com.chatbot.core.simplepayment/
├── config/                      # Configuration classes
│   ├── SimplePaymentConfig.java
│   ├── RateLimitConfig.java
│   ├── RateLimiterInterceptor.java
│   ├── WebMvcConfig.java
│   ├── WebhookSecurityFilter.java
│   └── RetryConfig.java
├── controller/                  # REST Controllers
│   ├── SimplePaymentController.java
│   └── PublicSimplePaymentController.java
├── service/                     # Business logic
│   ├── SimplePaymentService.java
│   ├── BankApiService.java
│   ├── QRCodeService.java
│   ├── PaymentAuditService.java
│   ├── PaymentValidationService.java
│   ├── RetryablePaymentService.java
│   └── ...
├── model/                       # Entity models
│   ├── SimplePayment.java
│   ├── PaymentAuditLog.java
│   └── PaymentStatus.java
├── repository/                  # Data access
│   ├── SimplePaymentRepository.java
│   └── PaymentAuditLogRepository.java
├── dto/                         # Data transfer objects
│   ├── DepositRequest.java
│   ├── DepositResponse.java
│   └── PaymentStatusResponse.java
├── exception/                   # Exception handling
│   ├── PaymentException.java
│   ├── PaymentNotFoundException.java
│   ├── InvalidPaymentAmountException.java
│   └── GlobalExceptionHandler.java
├── validation/                  # Input validation
│   ├── PaymentValidationService.java
│   ├── ValidAmount.java
│   ├── ValidCurrency.java
│   └── ValidReferenceCode.java
├── metrics/                     # Monitoring & metrics
│   ├── PaymentMetricsService.java
│   ├── PaymentMetricsAspect.java
│   └── PaymentMetricsScheduler.java
├── health/                      # Health checks
│   ├── SimplePaymentHealthIndicator.java
│   └── PaymentMetricsEndpoint.java
├── aspect/                      # AOP aspects
│   └── PaymentAuditAspect.java
├── annotation/                  # Custom annotations
│   └── AuditPayment.java
└── scheduler/                   # Scheduled tasks
    └── PaymentRetryScheduler.java
```

## API Endpoints

### Authenticated Endpoints

#### Tạo yêu cầu nạp tiền
```http
POST /api/simple-payment/deposit
Content-Type: application/json
Authorization: Bearer {token}

{
  "amount": 100000,
  "currency": "VND",
  "description": "Nạp tiền vào tài khoản",
  "targetPackageId": "package_001",
  "discountCode": "SAVE10"
}
```

#### Kiểm tra trạng thái thanh toán
```http
GET /api/simple-payment/status/{referenceCode}
Authorization: Bearer {token}
```

#### Lấy lịch sử thanh toán
```http
GET /api/simple-payment/history
Authorization: Bearer {token}
```

#### Hủy thanh toán
```http
POST /api/simple-payment/cancel/{referenceCode}
Authorization: Bearer {token}

{
  "reason": "User requested cancellation"
}
```

#### Refund thanh toán (Admin)
```http
POST /api/simple-payment/admin/refund/{referenceCode}
Authorization: Bearer {admin_token}

{
  "reason": "Admin refund"
}
```

#### Retry thanh toán thất bại
```http
POST /api/simple-payment/retry/{referenceCode}
Authorization: Bearer {token}
```

### Public Endpoints (No Authentication)

#### Health Check
```http
GET /api/public/simple-payment/health
```

#### Lấy thông tin ngân hàng
```http
GET /api/public/simple-payment/bank-info
```

#### Tạo yêu cầu nạp tiền (Public)
```http
POST /api/public/simple-payment/deposit
Content-Type: application/json

{
  "amount": 100000,
  "currency": "VND",
  "description": "Nạp tiền",
  "targetPackageId": "package_001"
}
```

#### Kiểm tra trạng thái (Public)
```http
GET /api/public/simple-payment/status/{referenceCode}
```

### Admin Endpoints

#### Metrics Overview
```http
GET /api/simple-payment/metrics/overview
Authorization: Bearer {admin_token}
```

#### Metrics by Date Range
```http
GET /api/simple-payment/metrics/by-date-range?startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer {admin_token}
```

#### Detailed Health Status
```http
GET /api/simple-payment/metrics/health-detailed
Authorization: Bearer {admin_token}
```

## Configuration

### Environment-specific Configuration

Các file configuration cho từng môi trường:

- `application-simplepayment-dev.yml` - Development environment
- `application-simplepayment-staging.yml` - Staging environment
- `application-simplepayment-prod.yml` - Production environment

### Configuration Properties

```yaml
simplepayment:
  bank-api:
    provider: mock|vietqr
    api-url: ${BANK_API_URL}
    api-key: ${BANK_API_KEY}
    timeout: 30000
    retry-attempts: 5
    retry-delay: 2000
  
  payment:
    qr-expiry-hours: 24
    min-amount: 10000
    max-amount: 50000000
    allowed-currencies: VND,USD,EUR
  
  webhook:
    enabled: true
    signature-secret: ${WEBHOOK_SIGNATURE_SECRET}
    timeout: 10000
    retry-attempts: 5
    retry-delay: 5000
  
  rate-limit:
    enabled: true
    public-endpoints:
      requests-per-minute: 20
      burst-capacity: 40
    authenticated-endpoints:
      requests-per-minute: 80
      burst-capacity: 120
  
  monitoring:
    metrics-enabled: true
    audit-logging-enabled: true
    slow-query-threshold-ms: 300
  
  security:
    mask-sensitive-data: true
    log-payment-details: false
    require-https: true
```

## Payment Flow

1. **Tạo yêu cầu thanh toán**
   - Client gọi API tạo deposit
   - System validates request (amount, currency, etc.)
   - System tạo payment record với status PENDING
   - System generate QR code
   - System log audit event
   - System track metrics

2. **User chuyển khoản**
   - User scan QR code hoặc chuyển khoản thủ công
   - User nhập số tiền chính xác
   - Bank xử lý giao dịch

3. **Kiểm tra thanh toán**
   - Scheduled job kiểm tra pending payments mỗi 10 phút
   - System gọi Bank API để kiểm tra transaction
   - Nếu transaction found → complete payment
   - Nếu không found → tiếp tục kiểm tra

4. **Hoàn thành thanh toán**
   - System update payment status thành COMPLETED
   - System credit user balance hoặc upgrade package
   - System send email notification
   - System trigger webhook
   - System generate invoice
   - System log audit event
   - System track metrics

5. **Expiry**
   - Nếu payment không được hoàn thành trong 24h
   - System auto-expire payment
   - System send notification
   - System log audit event

## Security Features

### Rate Limiting
- Public endpoints: 20 requests/minute (burst 40)
- Authenticated endpoints: 80 requests/minute (burst 120)
- In-memory implementation (production nên dùng Redis)

### Webhook Signature Verification
- HMAC-SHA256 signature verification
- Timestamp validation (5 minutes window)
- Constant-time comparison để prevent timing attacks

### Input Validation
- Amount validation (min: 10,000 VND, max: 50,000,000 VND)
- Currency validation (VND, USD, EUR)
- Reference code format validation (PAY + 12 alphanumeric chars)
- Discount code format validation (6-20 alphanumeric chars)

### Audit Logging
- Tất cả payment operations được log
- Bao gồm: user_id, tenant_id, action, old_status, new_status, amount, ip_address, user_agent
- Async logging để không ảnh hưởng performance

## Monitoring & Metrics

### Micrometer Metrics
- Counters: payment.created.total, payment.completed.total, payment.failed.total, etc.
- Timers: payment.processing.duration, bank.api.call.duration, qr.code.generation.duration
- Gauges: payment.pending.count, payment.revenue.total

### Health Checks
- `/actuator/health` - Spring Boot Actuator health check
- `/api/simple-payment/metrics/health-detailed` - Detailed payment health status

### Audit Logs
- Table: `payment_audit_logs`
- Actions: PAYMENT_CREATED, PAYMENT_COMPLETED, PAYMENT_FAILED, PAYMENT_EXPIRED, etc.
- Queryable by reference_code, user_id, tenant_id, action, date range

## Retry Logic

### Exponential Backoff
- Initial delay: 1s
- Multiplier: 2
- Max delay: 30s
- Max attempts: 5

### Retry Scenarios
- Bank API calls
- Payment completion
- Webhook delivery

### Scheduled Retry Jobs
- Retry failed payments every 5 minutes
- Check stuck pending payments every 10 minutes

## Database Schema

### simple_payments table
```sql
CREATE TABLE simple_payments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    reference_code VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    bank_transaction_id VARCHAR(100),
    description TEXT,
    target_package_id VARCHAR(100),
    qr_content TEXT,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    completed_at TIMESTAMP,
    
    CONSTRAINT fk_simple_payments_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_simple_payments_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);
```

### payment_audit_logs table
```sql
CREATE TABLE payment_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    payment_reference_code VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_status VARCHAR(20),
    new_status VARCHAR(20),
    amount DECIMAL(15,2),
    description TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    request_id VARCHAR(100),
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_payment_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_payment_audit_logs_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);
```

## Error Handling

### Custom Exceptions
- `PaymentException` - Base exception
- `PaymentNotFoundException` - Payment not found
- `InvalidPaymentAmountException` - Invalid amount
- `PaymentExpiredException` - Payment expired
- `BankApiException` - Bank API error

### Error Response Format
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Amount must be at least 10,000 VND",
  "details": {
    "amount": "Amount must be at least 10,000 VND"
  },
  "timestamp": "2024-01-01T00:00:00",
  "status": 400
}
```

## Dependencies

```gradle
// Spring Retry
implementation 'org.springframework.retry:spring-retry:2.0.5'
implementation 'org.springframework:spring-aspects:6.1.5'

// Micrometer
implementation 'io.micrometer:micrometer-core:1.13.0'
implementation 'io.micrometer:micrometer-registry-prometheus:1.13.0'

// Flyway
implementation 'org.flywaydb:flyway-core:10.10.0'
implementation 'org.flywaydb:flyway-postgresql:10.10.0'
```

## Deployment Checklist

### Pre-deployment
- [ ] Chạy database migration (Flyway)
- [ ] Set environment variables cho production
- [ ] Configure Bank API credentials
- [ ] Configure webhook signature secret
- [ ] Set rate limits cho production
- [ ] Configure SMTP cho email notifications
- [ ] Configure Redis cho rate limiting (nếu dùng distributed)
- [ ] Configure Prometheus/Grafana cho metrics

### Post-deployment
- [ ] Verify health check endpoint
- [ ] Test payment flow end-to-end
- [ ] Verify metrics collection
- [ ] Test webhook delivery
- [ ] Verify audit logging
- [ ] Monitor error rates
- [ ] Check scheduled jobs execution

## Troubleshooting

### Payment stuck in PENDING status
- Check Bank API connectivity
- Verify scheduled jobs are running
- Check payment expiry time
- Review audit logs for errors

### Webhook delivery failed
- Verify webhook URL is accessible
- Check webhook signature
- Review retry logs
- Check network connectivity

### Metrics not showing
- Verify Micrometer is initialized
- Check Prometheus configuration
- Verify PaymentMetricsService.init() is called
- Review application logs

### Rate limiting not working
- Verify RateLimitConfig.enabled = true
- Check interceptor registration
- Verify IP extraction logic
- Review rate limit thresholds

## Support

For issues and questions, contact the development team or create an issue in the project repository.
