# 📊 Configuration Files Analysis Report

## 🔍 **Tổng quan phân tích**

Đã phân tích **11 file cấu hình** và phát hiện nhiều vấn đề về trùng lặp và xung đột.

---

## ⚠️ **VẤN ĐỀ TRÙNG LẶP & XUNG ĐỘT**

### **1. DATABASE CONFIGURATION CONFLICTS**

#### **Primary Database Conflicts:**
| File | Property | Value | Conflict |
|------|-----------|--------|-----------|
| `application.properties` | `spring.datasource.jdbc-url` | `jdbc:postgresql://localhost:5432/traloitudong_db` |
| `application-dev.properties` | `spring.datasource.url` | `jdbc:postgresql://localhost:5432/traloitudong_db` |
| `application-old.properties` | `spring.datasource.url` | `jdbc:postgresql://localhost:5432/traloitudong_db` |

**Vấn đề:** Mixed usage of `jdbc-url` vs `url` properties

#### **Hub Database Overrides:**
| File | Property | Value | Conflict |
|------|-----------|--------|-----------|
| `application.properties` | `spring.datasource.user.jdbc-url` | `jdbc:postgresql://localhost:5433/chatbot_identity_db` |
| `application-dev.properties` | `spring.datasource.user.jdbc-url` | `jdbc:postgresql://localhost:5432/traloitudong_db` |

**Vấn đề:** Development profile overrides hub databases to use single database

### **2. GRPC PORT CONFLICTS**

#### **gRPC Server Port Conflicts:**
| File | Property | Port | Conflict |
|------|-----------|--------|-----------|
| `application.properties` | `app.hubs.identity.port` | `50051` |
| `application-identity.properties` | `spring.grpc.server.port` | `50051` |
| `application-old.properties` | `spring.grpc.server.port` | `50054` |

| File | Property | Port | Conflict |
|------|-----------|--------|-----------|
| `application.properties` | `app.hubs.app.port` | `50054` |
| `application-app.properties` | `spring.grpc.server.port` | `50054` |
| `application-old.properties` | `spring.grpc.server.port` | `50054` |

**Vấn đề:** Multiple files defining same gRPC ports

### **3. JWT CONFIGURATION DUPLICATES**

#### **JWT Secret Conflicts:**
| File | Property | Value |
|------|-----------|--------|
| `application.properties` | `spring.security.jwt.secret` | `${JWT_SECRET:mySecretKey}` |
| `application.properties` | `jwt.secret` | `Hh52JKs3NlFlk7MpT6VYjIML9Zn7sgfhW67X7j3Xr8Y=` |
| `application-identity.properties` | `identity.jwt.secret` | `${JWT_SECRET:Hh52JKs3NlFlk7MpT6VYjIML9Zn7sgfhW67X7j3Xr8Y=}` |
| `application-old.properties` | `jwt.secret` | `Hh52JKs3NlFlk7MpT6VYjIML9Zn7sgfhW67X7j3Xr8Y=` |

**Vấn đề:** 4 different JWT secret configurations

### **4. REDIS CONFIGURATION DUPLICATES**

#### **Redis Host/Port Conflicts:**
| File | Property | Value |
|------|-----------|--------|
| `application.properties` | `spring.data.redis.host` | `localhost` |
| `application.properties` | `spring.redis.host` | `localhost` |
| `application-old.properties` | `spring.redis.host` | `localhost` |

**Vấn đề:** Both `spring.data.redis.*` and `spring.redis.*` used

### **5. LOGGING CONFIGURATION CONFLICTS**

#### **Logging Level Conflicts:**
| File | Property | Value | Conflict |
|------|-----------|--------|-----------|
| `application.properties` | `logging.level.com.chatbot` | `INFO` |
| `application-identity.properties` | `logging.level.com.chatbot.core.identity` | `DEBUG` |
| `application-app.properties` | `logging.level.com.chatbot.core.app` | `DEBUG` |

#### **Management Endpoints Duplicates:**
| File | Property | Value |
|------|-----------|--------|
| `application.properties` | `management.endpoints.web.exposure.include` | `health,info,metrics,prometheus` |
| `application-identity.properties` | `management.endpoints.web.exposure.include` | `health,info,metrics,prometheus` |
| `application-app.properties` | `management.endpoints.web.exposure.include` | `health,info,metrics,prometheus` |

### **6. MINIO CONFIGURATION CONFLICTS**

#### **MinIO Configuration Duplicates:**
| File | Property | Value | Conflict |
|------|-----------|--------|-----------|
| `application.properties` | `app.integrations.minio.endpoint` | `${MINIO_ENDPOINT:http://localhost:9000}` |
| `application.properties` | `minio.url` | `http://localhost:9000` |
| `application-old.properties` | `minio.url` | `http://cwsv.truyenthongviet.vn:9000` |

### **7. HIKARI CONNECTION POOL DUPLICATES**

#### **Hikari Configuration Conflicts:**
| File | Property | Value | Conflict |
|------|-----------|--------|-----------|
| `application.properties` | `spring.datasource.hikari.maximum-pool-size` | `20` |
| `application-dev.properties` | `spring.datasource.hikari.maximum-pool-size` | `20` |
| `application-old.properties` | `spring.datasource.hikari.maximum-pool-size` | `20` |

---

## 🚨 **CRITICAL ISSUES**

### **1. DATABASE ISOLATION VIOLATION**
- **Problem:** Development profile overrides all hub databases to use single database
- **Impact:** Violates Hub & Spoke architecture principles
- **Files:** `application-dev.properties`

### **2. GRC PORT ASSIGNMENT CONFLICTS**
- **Problem:** Multiple hubs trying to use same ports
- **Impact:** Services won't start due to port conflicts
- **Files:** All hub-specific configs

### **3. JWT SECRET INCONSISTENCY**
- **Problem:** Different JWT secrets in different files
- **Impact:** Authentication failures between hubs
- **Files:** `application.properties`, `application-identity.properties`, `application-old.properties`

---

## 📋 **RECOMMENDATIONS**

### **IMMEDIATE FIXES REQUIRED:**

#### **1. Standardize Database Configuration**
```properties
# Remove from application-dev.properties:
# spring.datasource.user.jdbc-url
# spring.datasource.tenant.jdbc-url
# Keep hub databases separate for proper isolation
```

#### **2. Fix gRPC Port Conflicts**
```properties
# Use hub-specific port properties:
# identity: 50051, user: 50052, tenant: 50053, app: 50054
# billing: 50055, wallet: 50056, config: 50057, message: 50058
```

#### **3. Consolidate JWT Configuration**
```properties
# Use single JWT configuration:
spring.security.jwt.secret=${JWT_SECRET:Hh52JKs3NlFlk7MpT6VYjIML9Zn7sgfhW67X7j3Xr8Y=}
spring.security.jwt.expiration=${JWT_EXPIRATION:86400000}

# Remove duplicate jwt.* properties
```

#### **4. Standardize Redis Configuration**
```properties
# Use only spring.data.redis.* properties
# Remove spring.redis.* duplicates
```

### **ARCHITECTURAL IMPROVEMENTS:**

#### **1. Remove Legacy Configuration**
- Delete `application-old.properties` (contains outdated settings)
- Move relevant settings to appropriate hub configs

#### **2. Environment-Specific Configuration**
```properties
# application-dev.properties should only contain:
# - Single database for development
# - Debug logging enabled
# - Mock external services
# - Development-specific timeouts
```

#### **3. Hub Configuration Standardization**
```properties
# Each hub should only define:
# - Hub-specific gRPC server port
# - Hub-specific client connections
# - Hub-specific business logic settings
# - Hub-specific logging levels
```

---

## 🔧 **CLEANUP PLAN**

### **Phase 1: Critical Conflicts**
1. ✅ Fix database configuration conflicts
2. ✅ Resolve gRPC port conflicts  
3. ✅ Consolidate JWT configuration
4. ✅ Standardize Redis configuration

### **Phase 2: Structural Improvements**
1. ✅ Remove duplicate management endpoints
2. ✅ Clean up MinIO configuration
3. ✅ Standardize Hikari pool settings
4. ✅ Consolidate logging configuration

### **Phase 3: Architecture Cleanup**
1. ✅ Remove legacy configuration file
2. ✅ Create environment-specific configs
3. ✅ Implement hub isolation
4. ✅ Add configuration validation

---

## 📊 **IMPACT ASSESSMENT**

| Issue Type | Severity | Files Affected | Risk Level |
|------------|-----------|-----------------|-------------|
| Database Conflicts | **CRITICAL** | 3 | **HIGH** |
| gRPC Port Conflicts | **CRITICAL** | 8 | **HIGH** |
| JWT Secret Conflicts | **HIGH** | 3 | **MEDIUM** |
| Redis Duplicates | **MEDIUM** | 3 | **LOW** |
| Logging Duplicates | **LOW** | 8 | **LOW** |

**Overall Risk Level: HIGH** - Requires immediate attention before production deployment.

---

## 🎯 **NEXT STEPS**

1. **IMMEDIATE:** Fix critical database and gRPC conflicts
2. **SHORT-TERM:** Clean up configuration duplicates
3. **MEDIUM-TERM:** Implement proper environment separation
4. **LONG-TERM:** Add configuration validation and testing

**Priority:** CRITICAL - Fix before any production deployment!
