#!/bin/bash

# Chatbot SaaS v2.1 - Security Setup Script
# Run this script to set up secure environment variables

echo "🔒 Chatbot SaaS v2.1 - Security Setup"
echo "======================================"

# Check if .env file exists
if [ -f ".env" ]; then
    echo "⚠️  .env file already exists. Backing up to .env.backup"
    cp .env .env.backup
fi

# Generate secure random values
echo "🔑 Generating secure credentials..."

# Database password
DB_PASSWORD=$(openssl rand -base64 32 | tr -d "=+/" | cut -c1-25)

# RabbitMQ credentials  
RABBITMQ_USER="chatbot_user_$(openssl rand -hex 4)"
RABBITMQ_PASSWORD=$(openssl rand -base64 32 | tr -d "=+/" | cut -c1-25)

# MinIO credentials
MINIO_ACCESS_KEY="chatbot_$(openssl rand -hex 6)"
MINIO_SECRET_KEY=$(openssl rand -base64 32 | tr -d "=+/" | cut -c1-25)

# JWT Secret (64 characters minimum)
JWT_SECRET=$(openssl rand -base64 48 | tr -d "=+/")

# Create .env file
cat > .env << EOF
# Chatbot SaaS v2.1 - Environment Variables
# Generated on $(date)

# ========================
# DATABASE CONFIGURATION
# ========================
DATABASE_URL=jdbc:postgresql://localhost:5432/traloitudong_db
DATABASE_USERNAME=traloitudong_user
DATABASE_PASSWORD=${DB_PASSWORD}

# ========================
# RABBITMQ CONFIGURATION
# ========================
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=${RABBITMQ_USER}
RABBITMQ_PASSWORD=${RABBITMQ_PASSWORD}
RABBITMQ_VHOST=/

# ========================
# REDIS CONFIGURATION
# ========================
REDIS_HOST=localhost
REDIS_PORT=6379

# ========================
# JWT CONFIGURATION
# ========================
JWT_SECRET=${JWT_SECRET}
JWT_EXPIRATION=86400000

# ========================
# MINIO CONFIGURATION
# ========================
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=${MINIO_ACCESS_KEY}
MINIO_SECRET_KEY=${MINIO_SECRET_KEY}
MINIO_BUCKET=chatbot-files

# ========================
# SERVER CONFIGURATION
# ========================
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=development

EOF

# Set secure permissions
chmod 600 .env

echo "✅ Environment variables generated successfully!"
echo ""
echo "📝 Generated credentials:"
echo "   Database Password: ${DB_PASSWORD}"
echo "   RabbitMQ User: ${RABBITMQ_USER}"
echo "   RabbitMQ Password: ${RABBITMQ_PASSWORD}"
echo "   MinIO Access Key: ${MINIO_ACCESS_KEY}"
echo "   MinIO Secret Key: ${MINIO_SECRET_KEY}"
echo "   JWT Secret: ${JWT_SECRET:0:20}..."
echo ""
echo "🔐 Security reminders:"
echo "   1. Save these credentials securely"
echo "   2. Update your database/services with new passwords"
echo "   3. Never commit .env file to version control"
echo "   4. Update CORS domains in SecurityConfig.java for production"
echo ""
echo "🚀 Next steps:"
echo "   1. Update your database password"
echo "   2. Update RabbitMQ credentials"
echo "   3. Update MinIO credentials"
echo "   4. Restart your application"
echo "   5. Test all integrations"
echo ""
echo "✨ Setup complete!"
