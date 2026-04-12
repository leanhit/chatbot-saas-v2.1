#!/bin/bash

echo "=== Production Secrets Generator ==="
echo "Generating secure passwords and secrets for production deployment"
echo ""

# Generate strong passwords (256-bit)
echo "Generating database passwords..."
POSTGRES_PASSWORD=$(openssl rand -base64 32)
IDENTITY_DB_PASSWORD=$(openssl rand -base64 32)
USER_DB_PASSWORD=$(openssl rand -base64 32)
TENANT_DB_PASSWORD=$(openssl rand -base64 32)
APP_DB_PASSWORD=$(openssl rand -base64 32)
BILLING_DB_PASSWORD=$(openssl rand -base64 32)
WALLET_DB_PASSWORD=$(openssl rand -base64 32)
CONFIG_DB_PASSWORD=$(openssl rand -base64 32)
MESSAGE_DB_PASSWORD=$(openssl rand -base64 32)

# Generate JWT secret (64+ characters)
echo "Generating JWT secret..."
JWT_SECRET=$(openssl rand -base64 64 | tr -d "\n")

# Generate service passwords
echo "Generating service passwords..."
MINIO_ROOT_PASSWORD=$(openssl rand -base64 32)
RABBITMQ_PASSWORD=$(openssl rand -base64 32)
REDIS_PASSWORD=$(openssl rand -base64 32)

echo ""
echo "=== Generated Secrets ==="
echo ""
echo "# Database Configuration"
echo "POSTGRES_PASSWORD=${POSTGRES_PASSWORD}"
echo "IDENTITY_DB_PASSWORD=${IDENTITY_DB_PASSWORD}"
echo "USER_DB_PASSWORD=${USER_DB_PASSWORD}"
echo "TENANT_DB_PASSWORD=${TENANT_DB_PASSWORD}"
echo "APP_DB_PASSWORD=${APP_DB_PASSWORD}"
echo "BILLING_DB_PASSWORD=${BILLING_DB_PASSWORD}"
echo "WALLET_DB_PASSWORD=${WALLET_DB_PASSWORD}"
echo "CONFIG_DB_PASSWORD=${CONFIG_DB_PASSWORD}"
echo "MESSAGE_DB_PASSWORD=${MESSAGE_DB_PASSWORD}"
echo ""
echo "# Security Configuration"
echo "JWT_SECRET=${JWT_SECRET}"
echo ""
echo "# Service Configuration"
echo "MINIO_ROOT_PASSWORD=${MINIO_ROOT_PASSWORD}"
echo "RABBITMQ_PASSWORD=${RABBITMQ_PASSWORD}"
echo "REDIS_PASSWORD=${REDIS_PASSWORD}"
echo ""
echo "=== Instructions ==="
echo "1. Copy these values into your .env.production file"
echo "2. Replace 'yourdomain.com' with your actual domain"
echo "3. Store these secrets securely (password manager, vault, etc.)"
echo "4. Never commit .env.production to version control"
echo ""
echo "=== Security Reminder ==="
echo "These are production secrets. Keep them secure and change them regularly."
