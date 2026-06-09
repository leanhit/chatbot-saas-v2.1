# SimplePayment Deployment Guide

Hướng dẫn triển khai SimplePayment module cho Chatbot SaaS Platform.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Environment Setup](#environment-setup)
3. [Database Migration](#database-migration)
4. [Configuration](#configuration)
5. [Deployment Steps](#deployment-steps)
6. [Post-Deployment Verification](#post-deployment-verification)
7. [Monitoring Setup](#monitoring-setup)
8. [Troubleshooting](#troubleshooting)

## Prerequisites

### Software Requirements
- Java 21 or higher
- PostgreSQL 14 or higher
- Redis 6 or higher
- RabbitMQ 3.12 or higher
- Gradle 8.x

### Infrastructure Requirements
- Minimum 2GB RAM per application instance
- 10GB disk space for database
- SSL certificate for HTTPS (production)
- Load balancer for high availability

### External Services
- Bank API credentials (VietQR or other payment gateway)
- SMTP server for email notifications
- MinIO or S3 for invoice storage
- Prometheus/Grafana for monitoring

## Environment Setup

### 1. Environment Variables

Create a `.env` file or set environment variables:

```bash
# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/chatbot_db
DATABASE_USER=chatbot_user
DATABASE_PASSWORD=your_secure_password

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# RabbitMQ Configuration
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=admin
RABBITMQ_PASSWORD=your_rabbitmq_password

# JWT Configuration
JWT_SECRET=your_jwt_secret_key_min_32_chars
JWT_EXPIRATION=86400000

# Bank API Configuration
BANK_API_URL=https://api.vietqr.io
BANK_API_KEY=your_bank_api_key

# Webhook Configuration
WEBHOOK_SIGNATURE_SECRET=your_webhook_secret_min_32_chars

# Email Configuration
EMAIL_FROM=noreply@yourdomain.com
SMTP_HOST=smtp.yourdomain.com
SMTP_PORT=587
SMTP_USER=your_smtp_user
SMTP_PASSWORD=your_smtp_password

# MinIO Configuration
MINIO_ENDPOINT=https://minio.yourdomain.com
MINIO_ACCESS_KEY=your_minio_access_key
MINIO_SECRET_KEY=your_minio_secret_key
MINIO_BUCKET=chatbot-invoices

# Application Configuration
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080
```

### 2. Profile Selection

Set the appropriate Spring profile:

```bash
# Development
export SPRING_PROFILES_ACTIVE=development

# Staging
export SPRING_PROFILES_ACTIVE=staging

# Production
export SPRING_PROFILES_ACTIVE=production
```

## Database Migration

### 1. Run Flyway Migrations

```bash
cd backend
./gradlew flywayMigrate
```

Or manually run SQL scripts:

```bash
psql -U chatbot_user -d chatbot_db -f src/main/resources/db/migration/V5__add_payment_audit_logs.sql
```

### 2. Verify Migration

```sql
-- Check if tables exist
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('simple_payments', 'payment_audit_logs');

-- Check indexes
SELECT indexname, tablename FROM pg_indexes 
WHERE tablename IN ('simple_payments', 'payment_audit_logs');
```

### 3. Seed Initial Data (Optional)

```sql
-- Insert bank configuration
INSERT INTO system_config (config_key, config_value, config_type, description) VALUES
('payment.bank.name', 'Vietcombank', 'STRING', 'Default bank for payments'),
('payment.bank.account_number', '1234567890', 'STRING', 'Bank account number'),
('payment.bank.account_name', 'YOUR COMPANY NAME', 'STRING', 'Bank account name')
ON CONFLICT (config_key) DO NOTHING;
```

## Configuration

### 1. Update application.properties

Ensure the following line is present to import SimplePayment config:

```properties
spring.config.import=optional:classpath:application-penny.properties,optional:classpath:application-simplepayment-${spring.profiles.active:dev}.yml
```

### 2. Configure Rate Limiting (Production)

Update `application-simplepayment-prod.yml`:

```yaml
simplepayment:
  rate-limit:
    enabled: true
    public-endpoints:
      requests-per-minute: 20
      burst-capacity: 40
    authenticated-endpoints:
      requests-per-minute: 80
      burst-capacity: 120
```

### 3. Configure Webhook Security

Generate a secure webhook signature secret:

```bash
# Generate random 32-character secret
openssl rand -hex 32
```

Set in environment variable:

```bash
export WEBHOOK_SIGNATURE_SECRET=your_generated_secret
```

### 4. Configure Bank API

For production, replace mock BankApiService with real implementation:

```yaml
simplepayment:
  bank-api:
    provider: vietqr
    api-url: ${BANK_API_URL}
    api-key: ${BANK_API_KEY}
    timeout: 30000
    retry-attempts: 5
    retry-delay: 2000
```

## Deployment Steps

### 1. Build Application

```bash
cd backend
./gradlew clean build -x test
```

### 2. Run Database Migration

```bash
./gradlew flywayMigrate
```

### 3. Start Application

```bash
java -jar build/libs/chatbot-saas-v2.1-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=production \
  --server.port=8080
```

Or using systemd:

```ini
# /etc/systemd/system/chatbot-saas.service
[Unit]
Description=Chatbot SaaS Application
After=network.target postgresql.service redis.service rabbitmq.service

[Service]
Type=simple
User=chatbot
WorkingDirectory=/opt/chatbot-saas
ExecStart=/usr/bin/java -jar /opt/chatbot-saas/chatbot-saas.jar \
  --spring.profiles.active=production \
  --server.port=8080
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Enable and start service:

```bash
sudo systemctl daemon-reload
sudo systemctl enable chatbot-saas
sudo systemctl start chatbot-saas
```

### 4. Configure Nginx Reverse Proxy

```nginx
server {
    listen 80;
    server_name api.yourdomain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name api.yourdomain.com;

    ssl_certificate /etc/ssl/certs/yourdomain.crt;
    ssl_certificate_key /etc/ssl/private/yourdomain.key;
    ssl_protocols TLSv1.2 TLSv1.3;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Rate limiting headers
        proxy_set_header X-Client-IP $remote_addr;
    }

    location /actuator/health {
        proxy_pass http://localhost:8080/actuator/health;
        access_log off;
    }
}
```

### 5. Configure SSL Certificate

Using Let's Encrypt:

```bash
sudo certbot --nginx -d api.yourdomain.com
```

## Post-Deployment Verification

### 1. Health Check

```bash
curl https://api.yourdomain.com/actuator/health
```

Expected response:

```json
{
  "status": "UP"
}
```

### 2. SimplePayment Health Check

```bash
curl https://api.yourdomain.com/api/simple-payment/health
```

### 3. Test Payment Creation

```bash
curl -X POST https://api.yourdomain.com/api/simple-payment/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "amount": 100000,
    "currency": "VND",
    "description": "Test payment"
  }'
```

### 4. Verify Metrics

```bash
curl https://api.yourdomain.com/actuator/metrics
```

### 5. Check Scheduled Jobs

Verify that scheduled jobs are running by checking logs:

```bash
sudo journalctl -u chatbot-saas -f | grep "Checking pending payments"
```

### 6. Verify Database

```sql
-- Check if payments are being created
SELECT COUNT(*) FROM simple_payments WHERE created_at > NOW() - INTERVAL '1 hour';

-- Check audit logs
SELECT COUNT(*) FROM payment_audit_logs WHERE created_at > NOW() - INTERVAL '1 hour';
```

## Monitoring Setup

### 1. Prometheus Configuration

Add to `prometheus.yml`:

```yaml
scrape_configs:
  - job_name: 'chatbot-saas'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

### 2. Grafana Dashboard

Import the following metrics:

- Payment creation rate
- Payment completion rate
- Payment failure rate
- Average payment processing time
- Pending payments count
- Total revenue
- Bank API call duration
- Webhook delivery success rate

### 3. Alert Rules

Create alert rules in Prometheus:

```yaml
groups:
  - name: payment_alerts
    rules:
      - alert: HighPaymentFailureRate
        expr: rate(payment_failed_total[5m]) > 0.1
        for: 5m
        annotations:
          summary: "High payment failure rate detected"
      
      - alert: TooManyPendingPayments
        expr: payment_pending_count > 100
        for: 10m
        annotations:
          summary: "Too many pending payments"
      
      - alert: BankApiDown
        expr: rate(bank_api_call_duration_seconds_count[5m]) == 0
        for: 5m
        annotations:
          summary: "Bank API is not responding"
```

### 4. Log Aggregation

Configure ELK stack or Loki for log aggregation:

```yaml
# application-simplepayment-prod.yml
logging:
  level:
    com.chatbot.core.simplepayment: INFO
  file:
    name: /var/log/chatbot-saas/payment.log
    max-size: 100MB
    max-history: 30
```

## Troubleshooting

### Issue: Payments stuck in PENDING status

**Symptoms**: Payments remain in PENDING status indefinitely

**Possible Causes**:
1. Bank API is not responding
2. Scheduled jobs are not running
3. Payment expiry time is too short

**Solutions**:
1. Check Bank API connectivity:
   ```bash
   curl -X POST ${BANK_API_URL}/check -H "Authorization: Bearer ${BANK_API_KEY}"
   ```

2. Verify scheduled jobs:
   ```bash
   sudo journalctl -u chatbot-saas | grep "Checking pending payments"
   ```

3. Check payment expiry:
   ```sql
   SELECT reference_code, expires_at, status 
   FROM simple_payments 
   WHERE status = 'PENDING' 
   AND expires_at < NOW();
   ```

### Issue: Webhook delivery failed

**Symptoms**: Webhooks are not being delivered to clients

**Possible Causes**:
1. Webhook URL is incorrect or unreachable
2. Signature verification failed
3. Network connectivity issues

**Solutions**:
1. Test webhook URL:
   ```bash
   curl -X POST https://client-webhook-url.com \
     -H "Content-Type: application/json" \
     -d '{"test": "data"}'
   ```

2. Check webhook signature:
   ```bash
   # Verify signature secret is correct
   echo $WEBHOOK_SIGNATURE_SECRET
   ```

3. Review webhook logs:
   ```bash
   sudo journalctl -u chatbot-saas | grep "Webhook"
   ```

### Issue: Rate limiting not working

**Symptoms**: Rate limits are not being enforced

**Possible Causes**:
1. Rate limiting is disabled in config
2. Interceptor is not registered
3. IP extraction is failing

**Solutions**:
1. Check configuration:
   ```bash
   grep -A 10 "rate-limit" application-simplepayment-prod.yml
   ```

2. Verify interceptor registration:
   ```bash
   curl -I https://api.yourdomain.com/api/public/simple-payment/health
   # Check for rate limit headers
   ```

3. Review logs for rate limit violations:
   ```bash
   sudo journalctl -u chatbot-saas | grep "Rate limit exceeded"
   ```

### Issue: Metrics not showing in Prometheus

**Symptoms**: Metrics are not being scraped by Prometheus

**Possible Causes**:
1. Metrics endpoint is not exposed
2. Prometheus configuration is incorrect
3. Network firewall blocking connection

**Solutions**:
1. Verify metrics endpoint:
   ```bash
   curl https://api.yourdomain.com/actuator/prometheus
   ```

2. Check Prometheus configuration:
   ```bash
   sudo systemctl restart prometheus
   sudo journalctl -u prometheus -f
   ```

3. Test network connectivity:
   ```bash
   telnet prometheus-server 9090
   ```

### Issue: Database migration failed

**Symptoms**: Flyway migration fails

**Possible Causes**:
1. Database connection issues
2. Migration script has syntax errors
3. Database schema conflicts

**Solutions**:
1. Check database connection:
   ```bash
   psql -U chatbot_user -d chatbot_db -c "SELECT 1"
   ```

2. Review migration script:
   ```bash
   cat src/main/resources/db/migration/V5__add_payment_audit_logs.sql
   ```

3. Check Flyway history:
   ```sql
   SELECT * FROM flyway_schema_history;
   ```

4. Repair failed migration:
   ```bash
   ./gradlew flywayRepair
   ./gradlew flywayMigrate
   ```

## Security Hardening

### 1. Enable HTTPS

Ensure all endpoints use HTTPS:

```nginx
server {
    listen 443 ssl http2;
    ssl_certificate /etc/ssl/certs/yourdomain.crt;
    ssl_certificate_key /etc/ssl/private/yourdomain.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
}
```

### 2. Configure Firewall

```bash
# Allow only necessary ports
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 443/tcp   # HTTPS
sudo ufw allow 80/tcp    # HTTP (redirect to HTTPS)
sudo ufw enable
```

### 3. Enable Database SSL

Configure PostgreSQL to require SSL:

```bash
# postgresql.conf
ssl = on
ssl_cert_file = '/etc/ssl/certs/postgresql.crt'
ssl_key_file = '/etc/ssl/private/postgresql.key'
```

### 4. Rotate Secrets Regularly

Set up a schedule to rotate:
- JWT secrets
- Webhook signature secrets
- Database passwords
- API keys

### 5. Enable Audit Logging

Ensure audit logging is enabled:

```yaml
simplepayment:
  monitoring:
    audit-logging-enabled: true
```

## Backup and Recovery

### 1. Database Backup

```bash
# Daily backup
pg_dump -U chatbot_user -h localhost chatbot_db > backup_$(date +%Y%m%d).sql

# Compress backup
gzip backup_$(date +%Y%m%d).sql
```

### 2. Restore Database

```bash
gunzip backup_20240101.sql.gz
psql -U chatbot_user -h localhost chatbot_db < backup_20240101.sql
```

### 3. Invoice Backup

Backup invoice storage:

```bash
# Using MinIO client
mc cp /opt/chatbot-invoices/ backup-minio/$(date +%Y%m%d)/
```

## Performance Tuning

### 1. Database Connection Pool

Adjust HikariCP settings:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      connection-timeout: 20000
```

### 2. Redis Configuration

```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
```

### 3. JVM Settings

```bash
java -Xms2g -Xmx4g -XX:+UseG1GC -jar chatbot-saas.jar
```

## Rollback Procedure

If deployment fails:

1. Stop the application:
   ```bash
   sudo systemctl stop chatbot-saas
   ```

2. Restore previous version:
   ```bash
   cp /opt/chatbot-saas/chatbot-saas.jar.backup /opt/chatbot-saas/chatbot-saas.jar
   ```

3. Rollback database migration if needed:
   ```bash
   ./gradlew flywayUndo
   ```

4. Start the application:
   ```bash
   sudo systemctl start chatbot-saas
   ```

5. Verify health:
   ```bash
   curl https://api.yourdomain.com/actuator/health
   ```

## Support

For deployment issues, contact:
- DevOps team: devops@yourdomain.com
- Development team: dev@yourdomain.com
- Emergency: on-call@yourdomain.com
