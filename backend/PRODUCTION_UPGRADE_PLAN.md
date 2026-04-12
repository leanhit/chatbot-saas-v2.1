# # Production Upgrade Plan - Chatbot SaaS v2.1

## # Tình Hiên Hiên Tai: 85% Sãn Sàng Production

## # Phân Tích Các Thành Phân Con Thiêu

### # Phase 1: Critical Infrastructure (3-5 ngày)

## # 1.1 Production Environment Configuration

### # File Cân Tao: `.env.production`
```bash
# ========================================
# DATABASE CONFIGURATION
# ========================================
POSTGRES_PASSWORD=your_strong_256bit_password_here
IDENTITY_DB_PASSWORD=your_identity_db_password_here
USER_DB_PASSWORD=your_user_db_password_here
TENANT_DB_PASSWORD=your_tenant_db_password_here
APP_DB_PASSWORD=your_app_db_password_here
BILLING_DB_PASSWORD=your_billing_db_password_here
WALLET_DB_PASSWORD=your_wallet_db_password_here
CONFIG_DB_PASSWORD=your_config_db_password_here
MESSAGE_DB_PASSWORD=your_message_db_password_here

# ========================================
# SECURITY CONFIGURATION
# ========================================
JWT_SECRET=your_64_character_random_jwt_secret_key_here_minimum_length
MINIO_ROOT_PASSWORD=your_minio_256bit_password_here
RABBITMQ_PASSWORD=your_rabbitmq_256bit_password_here

# ========================================
# PRODUCTION DOMAINS
# ========================================
FRONTEND_URL=https://yourdomain.com
BACKEND_URL=https://api.yourdomain.com
ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com

# ========================================
# SSL/TLS CONFIGURATION
# ========================================
SSL_CERT_PATH=/etc/ssl/certs/yourdomain.crt
SSL_KEY_PATH=/etc/ssl/private/yourdomain.key

# ========================================
# PRODUCTION SETTINGS
# ========================================
SPRING_PROFILES_ACTIVE=production
LOG_LEVEL=WARN
DEBUG=false
```

### # Tasks:
1. [ ] Tao `.env.production` file
2. [ ] Generate strong passwords (256-bit)
3. [ ] Generate JWT secret (64+ characters)
4. [ ] Configure production domains
5. [ ] Setup SSL certificate paths

## # 1.2 Nginx Reverse Proxy Configuration

### # File Cân Tao: `nginx.conf`
```nginx
upstream backend {
    server localhost:8080;
}

server {
    listen 443 ssl http2;
    server_name yourdomain.com api.yourdomain.com;

    ssl_certificate /etc/ssl/certs/yourdomain.crt;
    ssl_certificate_key /etc/ssl/private/yourdomain.key;
    
    # SSL Configuration
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;

    # Security Headers
    add_header Strict-Transport-Security "max-age=63072000" always;
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";

    # Backend API
    location /api/ {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Frontend Static Files
    location / {
        root /var/www/html;
        try_files $uri $uri/ /index.html;
    }
}

# HTTP to HTTPS Redirect
server {
    listen 80;
    server_name yourdomain.com api.yourdomain.com;
    return 301 https://$server_name$request_uri;
}
```

### # Tasks:
1. [ ] Cai dat Nginx
2. [ ] Cau hinh upstream backend
3. [ ] Setup SSL certificates
4. [ ] Configure security headers
5. [ ] Setup HTTP to HTTPS redirect

## # 1.3 SSL/TLS Setup

### # Tasks:
1. [ ] Install Certbot (Let's Encrypt)
2. [ ] Generate SSL certificates
3. [ ] Setup auto-renewal
4. [ ] Test SSL configuration
5. [ ] Verify SSL security rating

### # Commands:
```bash
# Install Certbot
sudo apt update
sudo apt install certbot python3-certbot-nginx

# Generate SSL Certificate
sudo certbot --nginx -d yourdomain.com -d api.yourdomain.com

# Test Auto-renewal
sudo certbot renew --dry-run
```

### # Phase 2: Monitoring & Security (2-3 ngày)

## # 2.1 Production Monitoring Stack

### # File Cân Tao: `docker-compose.monitoring.yml`
```yaml
version: '3.8'
services:
  prometheus:
    image: prom/prometheus:latest
    container_name: prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'

  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin123
    volumes:
      - grafana_data:/var/lib/grafana
      - ./monitoring/grafana/dashboards:/etc/grafana/provisioning/dashboards
      - ./monitoring/grafana/datasources:/etc/grafana/provisioning/datasources

  node-exporter:
    image: prom/node-exporter:latest
    container_name: node-exporter
    ports:
      - "9100:9100"
    volumes:
      - /proc:/host/proc:ro
      - /sys:/host/sys:ro
      - /:/rootfs:ro

volumes:
  prometheus_data:
  grafana_data:
```

### # File Cân Tao: `monitoring/prometheus.yml`
```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'node-exporter'
    static_configs:
      - targets: ['localhost:9100']

  - job_name: 'spring-boot'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
```

### # Tasks:
1. [ ] Setup Prometheus monitoring
2. [ ] Setup Grafana dashboards
3. [ ] Configure Node Exporter
4. [ ] Setup Spring Boot Actuator
5. [ ] Create custom metrics

## # 2.2 Security Hardening

### # Firewall Configuration
```bash
# UFW Firewall Setup
sudo ufw enable
sudo ufw default deny incoming
sudo ufw default allow outgoing

# Allow SSH
sudo ufw allow ssh

# Allow HTTP/HTTPS
sudo ufw allow 80
sudo ufw allow 443

# Allow Backend (internal only)
sudo ufw allow from 127.0.0.1 to any port 8080

# Allow Database (internal only)
sudo ufw allow from 127.0.0.1 to any port 5432

# Status
sudo ufw status verbose
```

### # Security Headers Enhancement
```java
// File: SecurityConfig.java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .headers(headers -> headers
            .contentSecurityPolicy(csp -> csp
                .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'")
            )
            .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
            .xssProtection(HeadersConfigurer.XXssConfig::disable)
            .httpStrictTransportSecurity(hsts -> hsts
                .maxAgeInSeconds(31536000)
                .includeSubdomains(true)
            )
        );
    return http.build();
}
```

### # Tasks:
1. [ ] Configure UFW firewall
2. [ ] Setup security headers
3. [ ] Implement rate limiting
4. [ ] Setup fail2ban
5. [ ] Configure intrusion detection

## # 2.3 Centralized Logging

### # File Cân Tao: `docker-compose.logging.yml`
```yaml
version: '3.8'
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.8.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data

  logstash:
    image: docker.elastic.co/logstash/logstash:8.8.0
    ports:
      - "5044:5044"
    volumes:
      - ./logging/logstash.conf:/usr/share/logstash/pipeline/logstash.conf
    depends_on:
      - elasticsearch

  kibana:
    image: docker.elastic.co/kibana/kibana:8.8.0
    ports:
      - "5601:5601"
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    depends_on:
      - elasticsearch

volumes:
  elasticsearch_data:
```

### # Tasks:
1. [ ] Setup ELK stack
2. [ ] Configure log shipping
3. [ ] Setup Kibana dashboards
4. [ ] Configure log retention
5. [ ] Setup alerting

### # Phase 3: Deployment & Scaling (2-3 ngày)

## # 3.1 Production Docker Images

### # File Cân Tao: `Dockerfile.production`
```dockerfile
FROM openjdk:21-jdk-slim

# Install dependencies
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create app user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy JAR file
COPY build/libs/chatbot-saas-*.jar app.jar

# Change ownership
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Start application
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=production", "app.jar"]
```

### # File Cân Tao: `docker-compose.production.yml`
```yaml
version: '3.8'
services:
  backend:
    build:
      context: .
      dockerfile: Dockerfile.production
    container_name: chatbot_saas_backend
    restart: always
    env_file:
      - .env.production
    ports:
      - "8080:8080"
    volumes:
      - app_logs:/app/logs
    depends_on:
      - db
      - redis
      - minio
    networks:
      - internal
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  app_logs:

networks:
  internal:
    driver: bridge
```

### # Tasks:
1. [ ] Create production Dockerfile
2. [ ] Optimize image size
3. [ ] Setup multi-stage build
4. [ ] Configure health checks
5. [ ] Setup resource limits

## # 3.2 Kubernetes Deployment

### # File Cân Tao: `k8s/namespace.yaml`
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: chatbot-saas
```

### # File Cân Tao: `k8s/backend-deployment.yaml`
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend
  namespace: chatbot-saas
spec:
  replicas: 3
  selector:
    matchLabels:
      app: backend
  template:
    metadata:
      labels:
        app: backend
    spec:
      containers:
      - name: backend
        image: chatbot-saas/backend:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        envFrom:
        - secretRef:
            name: backend-secrets
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
```

### # File Cân Tao: `k8s/backend-service.yaml`
```yaml
apiVersion: v1
kind: Service
metadata:
  name: backend-service
  namespace: chatbot-saas
spec:
  selector:
    app: backend
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: ClusterIP
```

### # Tasks:
1. [ ] Create Kubernetes manifests
2. [ ] Setup ConfigMaps and Secrets
3. [ ] Configure Horizontal Pod Autoscaler
4. [ ] Setup Ingress controller
5. [ ] Configure network policies

## # 3.3 CI/CD Pipeline

### # File Cân Tao: `.github/workflows/production.yml`
```yaml
name: Production Deploy

on:
  push:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
    - name: Run tests
      run: ./gradlew test

  build:
    needs: test
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Build Docker image
      run: |
        docker build -f Dockerfile.production -t chatbot-saas/backend:${{ github.sha }} .
        docker tag chatbot-saas/backend:${{ github.sha }} chatbot-saas/backend:latest
    - name: Push to registry
      run: |
        echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
        docker push chatbot-saas/backend:${{ github.sha }}
        docker push chatbot-saas/backend:latest

  deploy:
    needs: build
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Deploy to Kubernetes
      run: |
        kubectl set image deployment/backend backend=chatbot-saas/backend:${{ github.sha }} -n chatbot-saas
        kubectl rollout status deployment/backend -n chatbot-saas
```

### # Tasks:
1. [ ] Setup GitHub Actions
2. [ ] Configure build pipeline
3. [ ] Setup deployment pipeline
4. [ ] Configure secrets management
5. [ ] Setup rollback strategy

### # Phase 4: Performance & Optimization (1-2 ngày)

## # 4.1 Database Optimization

### # PostgreSQL Configuration
```sql
-- Production PostgreSQL Configuration
-- postgresql.conf

# Memory Configuration
shared_buffers = 256MB
effective_cache_size = 1GB
work_mem = 4MB
maintenance_work_mem = 64MB

# Connection Configuration
max_connections = 200
shared_preload_libraries = 'pg_stat_statements'

# Performance Monitoring
track_activity_query_size = 2048
pg_stat_statements.track = all
```

### # Database Indexes
```sql
-- Critical Indexes for Performance
CREATE INDEX CONCURRENTLY idx_users_email ON users(email);
CREATE INDEX CONCURRENTLY idx_tenants_tenant_id ON tenants(tenant_id);
CREATE INDEX CONCURRENTLY idx_messages_created_at ON messages(created_at);
CREATE INDEX CONCURRENTLY idx_packages_active ON packages(is_active) WHERE is_active = true;
```

### # Tasks:
1. [ ] Optimize PostgreSQL configuration
2. [ ] Create performance indexes
3. [ ] Setup connection pooling
4. [ ] Configure database monitoring
5. [ ] Setup backup strategy

## # 4.2 Caching Strategy

### # Redis Configuration
```yaml
# Redis Production Configuration
redis:
  image: redis:7-alpine
  command: redis-server --maxmemory 512mb --maxmemory-policy allkeys-lru
  volumes:
    - redis_data:/data
  restart: always
```

### # Spring Boot Cache Configuration
```java
// CacheConfig.java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .disableCachingNullValues();
            
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}
```

### # Tasks:
1. [ ] Configure Redis caching
2. [ ] Implement cache warming
3. [ ] Setup cache invalidation
4. [ ] Monitor cache performance
5. [ ] Optimize cache keys

## # 4.3 CDN Configuration

### # CloudFlare Setup
```bash
# DNS Configuration
A     yourdomain.com    SERVER_IP
A     api.yourdomain.com SERVER_IP
CNAME www.yourdomain.com yourdomain.com

# Page Rules
yourdomain.com/*        - Cache Level: Everything
api.yourdomain.com/*    - Cache Level: Bypass
```

### # Static Asset Optimization
```nginx
# Nginx Static Asset Configuration
location ~* \.(jpg|jpeg|png|gif|ico|css|js)$ {
    expires 1y;
    add_header Cache-Control "public, immutable";
    gzip_static on;
}
```

### # Tasks:
1. [ ] Setup CDN provider
2. [ ] Configure DNS records
3. [ ] Optimize static assets
4. [ ] Setup cache headers
5. [ ] Monitor CDN performance

### # Phase 5: Backup & Recovery (1 ngày)

## # 5.1 Database Backup Strategy

### # Backup Script
```bash
#!/bin/bash
# backup-databases.sh

BACKUP_DIR="/backups"
DATE=$(date +%Y%m%d_%H%M%S)

# Backup all databases
databases=("traloitudong_db" "botpress_db" "odoo_db")

for db in "${databases[@]}"; do
    docker exec chatbot_saas_postgres pg_dump -U traloitudong_user $db > $BACKUP_DIR/${db}_${DATE}.sql
    gzip $BACKUP_DIR/${db}_${DATE}.sql
done

# Remove old backups (7 days)
find $BACKUP_DIR -name "*.sql.gz" -mtime +7 -delete
```

### # Cron Job
```bash
# Daily backup at 2 AM
0 2 * * * /path/to/backup-databases.sh
```

### # Tasks:
1. [ ] Setup automated backups
2. [ ] Configure backup retention
3. [ ] Test backup restoration
4. [ ] Setup offsite backup
5. [ ] Document recovery procedures

## # 5.2 Disaster Recovery Plan

### # Recovery Procedures
```bash
# Database Recovery
docker exec -i chatbot_saas_postgres psql -U traloitudong_user -d traloitudong_db < backup.sql

# Application Recovery
docker compose -f docker-compose.production.yml up -d

# Data Verification
curl -f http://localhost:8080/actuator/health
```

### # Tasks:
1. [ ] Document recovery procedures
2. [ ] Test disaster recovery
3. [ ] Setup monitoring alerts
4. [ ] Create runbook
5. [ ] Train team on recovery

## # Timeline & Resources

### # Timeline Summary:
- **Phase 1**: 3-5 ngày (Critical Infrastructure)
- **Phase 2**: 2-3 ngày (Monitoring & Security)
- **Phase 3**: 2-3 ngày (Deployment & Scaling)
- **Phase 4**: 1-2 ngày (Performance & Optimization)
- **Phase 5**: 1 ngày (Backup & Recovery)

### # Total: 9-14 ngày

### # Required Resources:
- **Server**: Minimum 4GB RAM, 2 CPU cores
- **Storage**: 50GB SSD minimum
- **SSL Certificate**: Let's Encrypt (free) or paid
- **Monitoring**: Prometheus + Grafana (open source)
- **CDN**: CloudFlare (free tier available)

### # Team Requirements:
- **DevOps Engineer**: 1 person
- **Backend Developer**: 1 person (part-time)
- **Security Specialist**: 1 person (consultant)

## # Success Criteria

### # Production Ready Checklist:
- [ ] All environment variables configured
- [ ] SSL/TLS certificates installed
- [ ] Nginx reverse proxy configured
- [ ] Monitoring stack operational
- [ ] Security hardening complete
- [ ] Backup strategy implemented
- [ ] CI/CD pipeline functional
- [ ] Performance optimization complete
- [ ] Documentation complete
- [ ] Team training complete

## # Risk Mitigation

### # High Risks:
1. **Downtime during deployment** - Use blue-green deployment
2. **Data loss during migration** - Implement proper backup strategy
3. **Security vulnerabilities** - Conduct security audit
4. **Performance degradation** - Load testing before deployment

### # Mitigation Strategies:
1. **Rollback plan** - Keep previous version ready
2. **Health checks** - Monitor application health
3. **Gradual rollout** - Use feature flags
4. **24/7 monitoring** - Alert on critical issues

## # Conclusion

Production upgrade requires systematic approach across multiple areas. Following this plan will ensure secure, scalable, and maintainable production deployment. Estimated timeline of 9-14 days is realistic for a team of 2-3 engineers with proper planning and execution.

**Next Steps:**
1. Review and approve this plan
2. Assign responsibilities
3. Set up development environment for testing
4. Begin Phase 1 implementation
