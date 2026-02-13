# ========================================
# CHATBOT SaaS v2.1 - gRPC Port Configuration
# ========================================

## 📋 Port Allocation Overview

### gRPC Server Ports
| Hub Name | Port | Description | Status |
|----------|------|-------------|--------|
| Identity Hub | 50051 | User authentication, JWT validation, user management | ✅ Active |
| App Hub | 50052 | Application registry, app management, subscriptions | ✅ Active |
| Tenant Hub | 50053 | Tenant management, multi-tenancy support | ✅ Active |
| User Hub | 50054 | User profiles, user preferences, user sessions | ✅ Active |

### HTTP/Web Ports
| Service | Port | Description | Status |
|---------|------|-------------|--------|
| Main Application | 8080 | Spring Boot web server, REST APIs | ✅ Active |
| Botpress | 3001 | Botpress admin interface | ✅ Active |
| Rasa | 5005 | Rasa NLU server | ✅ Active |
| Odoo | 3005 | Odoo ERP system | ✅ Active |
| MinIO | 9000 | Object storage service | ✅ Active |

### Database Ports
| Service | Port | Description | Status |
|---------|------|-------------|--------|
| PostgreSQL | 5432 | Main database | ✅ Active |
| Redis | 6379 | Cache and session storage | ✅ Active |

## 🏗️ Hub Communication Matrix

### Inter-Hub gRPC Connections
```
Identity Hub (50051) ←→ Tenant Hub (50053)
Identity Hub (50051) ←→ App Hub (50052)  
Identity Hub (50051) ←→ User Hub (50054)

Tenant Hub (50053) ←→ App Hub (50052)
Tenant Hub (50053) ←→ User Hub (50054)

App Hub (50052) ←→ User Hub (50054)
```

### Client Connection Patterns
```
Frontend → HTTP:8080 → Main App
Main App → gRPC:50051 → Identity Hub (Auth)
Main App → gRPC:50053 → Tenant Hub (Tenant Info)
Main App → gRPC:50052 → App Hub (App Registry)
Main App → gRPC:50054 → User Hub (User Profiles)
```

## 🔧 Configuration Files Reference

### Properties Files
- `application-identity.properties` - Identity Hub config (Port: 50051)
- `application-tenant.properties` - Tenant Hub config (Port: 50053)
- `application-app.properties` - App Hub config (Port: 50052)
- `application-user.properties` - User Hub config (Port: 50054)

### Server Configuration Classes
- `IdentityGrpcServerConfig.java` - Identity gRPC server
- `GrpcServerConfig.java` - Tenant gRPC server  
- `AppServiceGrpcImpl.java` - App gRPC server
- `UserGrpcServerConfig.java` - User gRPC server

### Client Configuration Classes
- `IdentityGrpcClient.java` - Connects to Identity Hub (50051)
- `TenantGrpcClient.java` - Connects to Tenant Hub (50053)
- `AppGrpcClient.java` - Connects to App Hub (50052)
- `UserGrpcClient.java` - Connects to User Hub (50054)

## 🚀 Port Assignment Rules

### Port Range Allocation
- **50051-50059**: gRPC Services (Internal hub communication)
- **8080**: Main HTTP API
- **3000-3099**: External services (Botpress, Odoo, etc.)
- **5000-5099**: AI/ML services (Rasa, etc.)
- **9000-9099**: Storage services (MinIO, etc.)
- **5432**: PostgreSQL database
- **6379**: Redis cache

### Naming Convention
```
{ServiceType}-{HubName}-{Port}
Examples:
- gRPC-Identity-50051
- gRPC-App-50052
- gRPC-Tenant-50053
- gRPC-User-50054
- HTTP-Main-8080
```

## 📝 Environment Variables

### Production Port Overrides
```bash
# Identity Hub
IDENTITY_GRPC_PORT=50051

# App Hub  
APP_GRPC_PORT=50052

# Tenant Hub
TENANT_GRPC_PORT=50053

# User Hub
USER_GRPC_PORT=50054

# Main Application
SERVER_PORT=8080
```

### Docker Compose Example
```yaml
services:
  identity-hub:
    ports:
      - "50051:50051"
    environment:
      - SPRING_GRPC_SERVER_PORT=50051
      
  app-hub:
    ports:
      - "50052:50052"
    environment:
      - SPRING_GRPC_SERVER_PORT=50052
      
  tenant-hub:
    ports:
      - "50053:50053"
    environment:
      - SPRING_GRPC_SERVER_PORT=50053
      
  user-hub:
    ports:
      - "50054:50054"
    environment:
      - USER_GRPC_SERVER_PORT=50054
```

## 🔍 Troubleshooting

### Common Port Issues
1. **Port Already in Use**: Check with `netstat -tulpn | grep :5005X`
2. **Connection Refused**: Verify target hub is running on correct port
3. **Timeout**: Check firewall settings and network connectivity

### Debug Commands
```bash
# Check all gRPC ports
netstat -tulpn | grep :5005

# Test gRPC connectivity
grpcurl -plaintext localhost:50051 list
grpcurl -plaintext localhost:50052 list
grpcurl -plaintext localhost:50053 list
grpcurl -plaintext localhost:50054 list

# Check process using ports
lsof -i :50051
lsof -i :50052
lsof -i :50053
lsof -i :50054
```

## 📅 Version History

### v2.1 (Current)
- Standardized all gRPC ports to 50051-50054 range
- Fixed client connection issues
- Added comprehensive port documentation

### Previous Versions
- Inconsistent port assignments
- Hardcoded port values
- Missing connection configurations

---

**Last Updated**: 2026-02-13  
**Maintainer**: Chatbot SaaS Team  
**Version**: 2.1.0
