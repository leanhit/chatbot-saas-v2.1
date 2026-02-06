# Minimal Local Setup

This setup runs the backend with minimal dependencies and no authentication.

## Prerequisites
- Docker and Docker Compose
- Java 21
- Gradle

## Quick Start

### Windows
```bash
start-minimal.bat
```

### Linux/Mac
```bash
chmod +x start-minimal.sh
./start-minimal.sh
```

## Manual Steps

### 1. Start Services
```bash
docker-compose up -d
```

### 2. Wait for Services (30 seconds)
Wait for PostgreSQL, Redis, and MinIO to fully start.

### 3. Start Backend
```bash
cd backend
./gradlew bootRun --args="--spring.profiles.active=minimal"
```

## Services
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379  
- **MinIO**: localhost:9000 (Console: localhost:9001)
- **Backend**: localhost:8080

## MinIO Access
- URL: http://localhost:9000
- Console: http://localhost:9001
- Username: minioadmin
- Password: minioadmin

## Disabled Features
- JWT Authentication
- VNPay Integration
- Facebook Integration
- Email SMTP
- Odoo Integration
- Botpress Integration

## Database
- Database: traloitudong_db
- Username: traloitudong_user
- Password: traloitudong_Admin_2025

## Stop Services
```bash
docker-compose down
```

## Clean Up
```bash
docker-compose down -v
```
