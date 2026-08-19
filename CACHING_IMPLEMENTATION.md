# Caching & Cache Invalidation Implementation Summary

## Overview
Implemented comprehensive Redis caching strategy for rarely-changing data (Tenant Profiles, Package Constraints, User Roles/Permissions) with proper cache invalidation to prevent stale cache issues.

## Changes Made

### 1. Redis Cache Configuration (RedisCacheConfig.java)
Created centralized Redis cache configuration with custom TTLs:
- **Default TTL**: 30 minutes
- **Tenant data**: 1 hour (rarely changes)
- **Tenant profiles**: 30 minutes (changes infrequently)
- **User data**: 15 minutes (changes occasionally)
- **User roles**: 30 minutes (changes rarely)
- **Tenant roles/membership**: 15 minutes (changes occasionally)
- **Package constraints**: 1 hour (rarely changes)
- **API responses**: 5 minutes (short cache)

### 2. TenantProfileService Caching
**Read Operations:**
- `getProfile(Long tenantId)` - Cached with key `tenantId`
- `getProfilesByTenantIds(List<Long> tenantIds)` - Batch cached with hash key

**Write Operations (Cache Eviction):**
- `upsertProfile()` - Evicts single and batch cache
- `updateLogo()` - Evicts single and batch cache

### 3. TenantPermissionValidator Caching
**Read Operations:**
- `isAdmin(String userEmail)` - Cached with key `admin:{email}`
- `isOwner(Long tenantId, String userEmail)` - Cached with key `{tenantId}:{email}:owner`
- `isActiveMember(Long tenantId, String userEmail)` - Cached with key `{tenantId}:{email}:active`

### 4. UserService Caching
**Read Operations:**
- `getUser(Long userId)` - Cached with key `userId`
- `getAllUsers()` - Cached with key `allUsers`

**Write Operations (Cache Eviction):**
- `updateUserStatus()` - Evicts user cache
- `updateProfile()` - Evicts user cache
- `updateAvatar()` - Evicts user cache
- `updateBasicInfo()` - Evicts user cache
- `updateProfessionalInfo()` - Evicts user cache

### 5. TenantMemberService Caching
**Write Operations (Cache Eviction):**
- `updateRole()` - Evicts role caches for target user
- `transferOwnership()` - Evicts all tenant role caches
- `removeMember()` - Evicts role caches for removed user

### 6. Existing Caching (Already Implemented)
**TenantService:**
- `getTenant(Long tenantId)` - Cached
- `getTenantIdByKey(String tenantKey)` - Cached
- Status transitions (suspend/activate/deactivate/delete) - Cache evicted
- Updates (basic info, contact info) - Cache evicted

**PackageService:**
- Uses `CachedPackageService` for all read operations
- Write operations clear all package caches

## Cache Names and TTLs

| Cache Name | TTL | Purpose |
|------------|-----|---------|
| `tenants` | 1 hour | Tenant entity data |
| `tenant-key-to-id` | 1 hour | Tenant key to ID mapping |
| `tenant-profiles` | 30 minutes | Individual tenant profiles |
| `tenant-profiles-batch` | 30 minutes | Batch tenant profile queries |
| `users` | 15 minutes | User entity data |
| `user-roles` | 30 minutes | User system roles (admin checks) |
| `tenant-roles` | 15 minutes | Tenant membership roles |
| `packages` | 1 hour | Package constraints |
| `packages-active` | 1 hour | Active packages |
| `packages-all` | 1 hour | All packages (admin) |
| `apiResponses` | 5 minutes | API response caching |

## Cache Invalidation Strategy

### Automatic Invalidation
- **@CacheEvict**: Automatically removes cache entries when data changes
- **@Caching**: Allows multiple cache evictions in a single operation
- **Conditional eviction**: Only evicts when conditions are met

### Key Patterns
- **Single entity**: `#id` or `#userId`
- **Composite keys**: `{tenantId}:{email}:{roleType}`
- **Batch operations**: `#tenantIds.hashCode()`
- **All entries**: `allEntries = true`

### Preventing Stale Cache
1. **Role Changes**: When admin updates user roles, permission caches are evicted
2. **Package Updates**: When admin updates packages, package caches are cleared
3. **Profile Updates**: When tenant updates profile, profile caches are evicted
4. **Membership Changes**: When member roles change, role caches are evicted

## Usage Examples

### Reading Cached Data
```java
// Automatically cached
Tenant tenant = tenantService.getTenant(tenantId);

// Permission check with caching
boolean isAdmin = permissionValidator.isAdmin(userEmail);
```

### Writing with Cache Invalidation
```java
// Automatically evicts cache
tenantService.updateBasicInfo(tenantKey, request);

// Evicts multiple caches
tenantMemberService.updateRole(tenantId, userId, newRole);
```

### Manual Cache Management
```java
// Clear all package caches
packageService.clearCache();

// Warm up caches
packageService.warmupCache();

// Get cache statistics
String stats = packageService.getCacheStats();
```

## Testing Cache Invalidation

### Scenario 1: User Role Update
1. User calls `isAdmin(userEmail)` - cached result: `false`
2. Admin updates user to ADMIN role
3. Cache evicted via `updateUserStatus()`
4. Next `isAdmin(userEmail)` call returns `true` (fresh data)

### Scenario 2: Package Constraint Update
1. User checks package constraints - cached
2. Admin updates package (message limit, features)
3. Cache evicted via `updatePackage()` → `clearAllPackageCache()`
4. Next package check returns updated constraints

### Scenario 3: Tenant Profile Update
1. User views tenant profile - cached
2. Admin updates tenant profile (logo, description)
3. Cache evicted via `upsertProfile()` or `updateLogo()`
4. Next profile view shows updated data

### Scenario 4: Tenant Membership Role Change
1. Permission check `isOwner(tenantId, userEmail)` - cached: `false`
2. Owner transfers ownership to this user
3. Cache evicted via `transferOwnership()` (all tenant roles)
4. Next `isOwner()` check returns `true`

## Monitoring Cache Performance

### Redis CLI Commands
```bash
# Monitor cache operations
redis-cli monitor

# Check cache size
redis-cli DBSIZE

# Get cache keys
redis-cli KEYS "tenant-profiles:*"

# Get cache TTL
redis-cli TTL "tenant-profiles:123"
```

### Spring Boot Actuator
```bash
# Cache metrics
curl http://localhost:8080/actuator/caches
curl http://localhost:8080/actuator/metrics/cache.*
```

### Application Logs
```
2026-08-19 18:00:00.123 [traceId=xxx, spanId=yyy] INFO  TenantService - Cache MISS: getTenant(123)
2026-08-19 18:00:01.456 [traceId=xxx, spanId=yyy] INFO  TenantService - Cache HIT: getTenant(123)
```

## Benefits

1. **Reduced Database Load**: Frequently accessed data served from Redis
2. **Improved Response Time**: Cache hits return in milliseconds vs database queries
3. **Scalability**: Reduced database contention under high load
4. **Automatic Invalidation**: No stale data when updates occur
5. **Configurable TTLs**: Different data types have appropriate cache durations
6. **Batch Caching**: Efficient handling of bulk queries
7. **Role-Based Caching**: Permission checks cached for performance
8. **Package Constraints**: Business rules cached to avoid repeated lookups

## Troubleshooting

### Cache Not Working
1. Verify `@EnableCaching` is present in configuration
2. Check Redis connection is active
3. Ensure cache manager bean is properly configured
4. Verify method is public (caching requires public methods)

### Stale Cache Issues
1. Check if `@CacheEvict` is present on write operations
2. Verify cache keys match between read and write operations
3. Ensure transaction boundaries are correct
4. Check for cache key collisions

### High Memory Usage
1. Reduce TTL for frequently changing data
2. Monitor cache hit ratios
3. Consider cache partitioning by tenant
4. Implement cache size limits

### Cache Misses
1. Check Redis connectivity
2. Verify serialization is working (JSON)
3. Monitor cache key patterns
4. Check for cache eviction due to memory pressure

## Configuration

### application.properties
```properties
# Redis Cache Configuration
spring.cache.type=redis
spring.cache.redis.time-to-live=1800000
spring.cache.redis.cache-null-values=false
spring.cache.redis.use-key-prefix=true
```

### Redis Configuration
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    timeout: 6000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
```

## Files Modified

1. `backend/src/main/java/com/chatbot/configs/RedisCacheConfig.java` - Redis cache configuration (new)
2. `backend/src/main/java/com/chatbot/core/tenant/profile/service/TenantProfileService.java` - Added caching
3. `backend/src/main/java/com/chatbot/core/tenant/service/TenantPermissionValidator.java` - Added caching
4. `backend/src/main/java/com/chatbot/core/user/service/UserService.java` - Added cache evictions
5. `backend/src/main/java/com/chatbot/core/tenant/membership/service/TenantMemberService.java` - Added cache evictions

## Next Steps

1. Monitor cache hit/miss ratios in production
2. Tune TTL values based on actual data change patterns
3. Implement cache warming for critical data on startup
4. Set up alerts for cache performance degradation
5. Consider implementing cache partitioning for multi-tenant isolation
6. Add cache statistics to monitoring dashboards
7. Implement cache backup/restore for disaster recovery
8. Consider implementing cache versioning for schema changes

## Security Considerations

- Cache keys may contain sensitive information (emails, IDs)
- Ensure Redis is properly secured (authentication, TLS)
- Consider encrypting cached sensitive data
- Implement cache access control in multi-tenant environments
- Monitor for cache poisoning attacks
- Regular cache cleanup to prevent data leakage
