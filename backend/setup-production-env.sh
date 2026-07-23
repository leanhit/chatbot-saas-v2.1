#!/bin/bash

echo "=== Setup Production Environment for Chatbot SaaS v2.1 ==="
echo ""

# Generate random passwords
JWT_SECRET=$(openssl rand -base64 32)
POSTGRES_PASSWORD=$(openssl rand -base64 32)
MINIO_ROOT_PASSWORD=$(openssl rand -base64 32)
RABBITMQ_PASSWORD=$(openssl rand -base64 32)
REDIS_PASSWORD=$(openssl rand -base64 32)

# Create .env.production file
cat > .env.production <<EOF
# ========================================
# DATABASE CONFIGURATION
# ========================================
POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
IDENTITY_DB_PASSWORD=${POSTGRES_PASSWORD}
USER_DB_PASSWORD=${POSTGRES_PASSWORD}
TENANT_DB_PASSWORD=${POSTGRES_PASSWORD}
APP_DB_PASSWORD=${POSTGRES_PASSWORD}
BILLING_DB_PASSWORD=${POSTGRES_PASSWORD}
WALLET_DB_PASSWORD=${POSTGRES_PASSWORD}
CONFIG_DB_PASSWORD=${POSTGRES_PASSWORD}
MESSAGE_DB_PASSWORD=${POSTGRES_PASSWORD}

POSTGRES_DB=traloitudong_db
POSTGRES_USER=traloitudong_user

# ========================================
# SECURITY CONFIGURATION
# ========================================
JWT_SECRET=${JWT_SECRET}
MINIO_ROOT_PASSWORD=${MINIO_ROOT_PASSWORD}
RABBITMQ_PASSWORD=${RABBITMQ_PASSWORD}

# ========================================
# PRODUCTION DOMAINS
# ========================================
FRONTEND_URL=https://yourdomain.com
BACKEND_URL=https://api.yourdomain.com
ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com

# ========================================
# SSL/TLS CONFIGURATION
# ========================================
SSL_CERT_PATH=/etc/letsencrypt/live/cwsv.truyenthongviet.vn/fullchain.pem
SSL_KEY_PATH=/etc/letsencrypt/live/cwsv.truyenthongviet.vn/privkey.pem

# ========================================
# PRODUCTION SETTINGS
# ========================================
SPRING_PROFILES_ACTIVE=production
LOG_LEVEL=WARN
DEBUG=false

# ========================================
# REDIS CONFIGURATION
# ========================================
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=${REDIS_PASSWORD}

# ========================================
# MINIO CONFIGURATION
# ========================================
MINIO_ROOT_USER=minioadmin
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=${MINIO_ROOT_PASSWORD}

# ========================================
# RABBITMQ CONFIGURATION
# ========================================
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=admin
RABBITMQ_PASSWORD=${RABBITMQ_PASSWORD}

# ========================================
# MONITORING
# ========================================
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus
MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS=when-authorized
MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED=true
EOF

echo "✓ .env.production created with random passwords"
echo ""
echo "IMPORTANT: Please update the following values in .env.production:"
echo "  - FRONTEND_URL (replace with your actual domain)"
echo "  - BACKEND_URL (replace with your actual domain)"
echo "  - ALLOWED_ORIGINS (replace with your actual domain)"
echo ""
echo "Generated secrets (save these securely):"
echo "  JWT_SECRET: ${JWT_SECRET}"
echo "  POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}"
echo "  MINIO_ROOT_PASSWORD: ${MINIO_ROOT_PASSWORD}"
echo "  RABBITMQ_PASSWORD: ${RABBITMQ_PASSWORD}"
echo "  REDIS_PASSWORD: ${REDIS_PASSWORD}"
