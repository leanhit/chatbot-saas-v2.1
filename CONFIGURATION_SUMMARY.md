# Configuration Summary - Chatbot SaaS v2.1

## Database Architecture
- **Hub & Spoke Architecture** với 8 databases riêng biệt
- **Main Database**: `chatbot_identity_db` (port 5432)
- **Hub Databases**: user (5434), tenant (5435), app (5436), billing (5437), wallet (5438), config (5439), message (5440)

## Port Configuration
- **Backend API**: 8080
- **Identity Hub gRPC**: 50051
- **User Hub gRPC**: 50052
- **Tenant Hub gRPC**: 50053
- **App Hub gRPC**: 50054
- **Billing Hub gRPC**: 50055
- **Wallet Hub gRPC**: 50056
- **Config Hub gRPC**: 50057
- **Message Hub gRPC**: 50058
- **Botpress**: 3001
- **MinIO**: 9000 (API), 9090 (Console)
- **Odoo**: 3005

## Fixed Issues
1. ✅ Database credentials统一使用 `chatbot_user`/`chatbot_Admin_2025`
2. ✅ gRPC port mapping corrected for hub communication
3. ✅ Removed duplicate health check intervals in Penny config
4. ✅ Docker Compose files synchronized with hub architecture

## Environment Variables
- `SPRING_PROFILES_ACTIVE`: development (default)
- `JWT_SECRET`: Hh52JKs3NlFlk7MpT6VYjIML9Zn7sgfhW67X7j3Xr8Y=
- `JWT_EXPIRATION`: 86400000ms (24 hours)
- `SERVER_PORT`: 8080

## Integration Services
- **Botpress**: http://localhost:3001
- **MinIO**: http://localhost:9000
- **Odoo**: http://localhost:3005
- **Redis**: localhost:6379

## Security Configuration
- Password requirements: min 8 chars, uppercase, lowercase, numbers, special chars
- Account lockout: 5 attempts, 30 minutes lockout
- Rate limiting: 60 requests/minute (identity), 300 requests/minute (user)
