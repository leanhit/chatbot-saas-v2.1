# 🔐 JWT RS256 & Cloud Signing Implementation

## 📋 Overview

Backend đã được hoàn thiện để thỏa mãn đầy đủ requirements từ `req.md` với các tính năng sau:

### ✅ **Implemented Features**

#### **1. JWT Algorithm Support (100% Complete)**
- ✅ **HS256** - HMAC with SHA-256 (default)
- ✅ **RS256** - RSA Signature with SHA-256 
- ✅ Dynamic algorithm selection via configuration
- ✅ RSA key pair generation and management

#### **2. Cloud-Only License Signing (100% Complete)**
- ✅ License tokens signed with `signed_by: "cloud"` claim
- ✅ Client self-signing prevention
- ✅ Cloud service role validation for premium features
- ✅ License token verification endpoint

#### **3. Auth API (100% Complete)**
- ✅ `POST /api/auth/register` - User registration
- ✅ `POST /api/auth/login` - User authentication
- ✅ JWT returns: `sub`, `email`, `tenant_id`, `role`, `exp`
- ✅ Token refresh and revocation

#### **4. License API (100% Complete)**
- ✅ `GET /api/license/me` - Get user license
- ✅ Returns: `exp`, `features[]`, `modules[]`, `limits{}`
- ✅ JWT compatible fields for local app
- ✅ Feature/module/limit check endpoints

#### **5. Edge Case Handling (100% Complete)**
- ✅ User chưa mua: Returns **404** 
- ✅ License hết hạn: Returns **401**
- ✅ User bị khóa: Returns **401**

## 🔧 **Configuration**

### **Environment Variables**

```bash
# JWT Configuration
JWT_ALGORITHM=RS256              # hoặc HS256
JWT_SECRET=your-secret-key       # cho HS256
RSA_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----..."  # cho RS256
RSA_PUBLIC_KEY="-----BEGIN PUBLIC KEY-----..."    # cho RS256
```

### **Application Properties**

```properties
# Security Configuration
spring.security.jwt.algorithm=${JWT_ALGORITHM:HS256}
spring.security.jwt.secret=${JWT_SECRET:Hh52JKs3NlFlk7MpT6VYjIML9Zn7sgfhW67X7j3Xr8Y=}
spring.security.jwt.rsa.private-key=${RSA_PRIVATE_KEY:}
spring.security.jwt.rsa.public-key=${RSA_PUBLIC_KEY:}
```

## 🚀 **Setup Instructions**

### **1. Generate RSA Keys (for RS256)**

```bash
cd backend
./scripts/generate-rsa-keys.sh 2048
```

### **2. Configure Environment**

```bash
# Copy keys to environment
export RSA_PRIVATE_KEY="$(cat keys/private.pem)"
export RSA_PUBLIC_KEY="$(cat keys/public.pem)"
export JWT_ALGORITHM=RS256
```

### **3. Run Application**

```bash
./gradlew bootRun
```

## 🧪 **Testing**

### **Enhanced Test Suite**

```bash
# Run enhanced tests
node test-license-api-enhanced.js
```

### **Test Coverage**

- ✅ RS256 algorithm support
- ✅ Cloud-only license creation
- ✅ License signing verification
- ✅ Edge case handling (404/401)
- ✅ Auth & License API endpoints

## 🔒 **Security Features**

### **1. Algorithm Selection**
- Dynamic switching between HS256/RS256
- RSA key validation on startup
- Fallback to HS256 if RSA keys not provided

### **2. Cloud-Only Validation**
- `@PreAuthorize("hasRole('CLOUD_SERVICE')")` for premium features
- License token signature verification
- Client self-signing prevention

### **3. Token Verification**
```java
// Verify license was signed by cloud
boolean isValid = jwtService.verifyLicenseSignedByCloud(token);
```

## 📊 **Compliance Matrix**

| Requirement | Status | Implementation |
|-------------|--------|----------------|
| ✅ Sign JWT (HS256/RS256) | **Complete** | `JwtService.java` |
| ✅ Không cho client tự tạo license | **Complete** | `LicenseController.java` |
| ✅ Có thể revoke license | **Complete** | `DELETE /api/license/{id}` |
| ✅ Decode (Local) | **Complete** | JWT extraction methods |
| ✅ Edge cases (404/401) | **Complete** | `LicenseExceptionHandler.java` |
| ✅ Auth API | **Complete** | `AuthController.java` |
| ✅ License API | **Complete** | `LicenseController.java` |

## 🔄 **Migration Guide**

### **From HS256 to RS256**

1. **Generate RSA keys:**
   ```bash
   ./scripts/generate-rsa-keys.sh
   ```

2. **Update configuration:**
   ```bash
   JWT_ALGORITHM=RS256
   RSA_PRIVATE_KEY=...
   RSA_PUBLIC_KEY=...
   ```

3. **Restart application**

4. **Verify with tests:**
   ```bash
   node test-license-api-enhanced.js
   ```

## 🛠 **Key Files Modified**

- `JwtService.java` - RS256 support & cloud signing
- `LicenseController.java` - Cloud-only validation
- `application.properties` - Algorithm configuration
- `generate-rsa-keys.sh` - RSA key generation
- `test-license-api-enhanced.js` - Enhanced test suite

## 📝 **Usage Examples**

### **Create License (Cloud Service Only)**

```javascript
// Only works with CLOUD_SERVICE role
POST /api/license
Authorization: Bearer <cloud-service-token>
{
  "userId": 123,
  "features": ["premium-feature"],
  "modules": ["advanced-module"]
}
```

### **Verify License Token**

```javascript
GET /api/license/me
Authorization: Bearer <user-token>
X-License-Token: <license-jwt>
```

## 🎯 **Final Compliance: 100%** ✅

Backend đã thỏa mãn **đầy đủ** tất cả requirements từ `req.md`:

- ✅ **Cloud**: Sign JWT (HS256/RS256), Không cho client tự tạo license, Có thể revoke license
- ✅ **Local**: Decode, Không cần verify signature (MVP), Có thể verify sau
- ✅ **Edge Cases**: 404 cho user chưa mua, 401 cho license hết hạn/user bị khóa
- ✅ **Auth & License APIs**: Hoàn chỉnh với JWT fields yêu cầu
- ✅ **Security**: Cloud-only signing, RSA support, client prevention
