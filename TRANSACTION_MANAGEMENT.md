# Transaction Management Guide

## Overview

This document provides guidelines for transaction management in the chatbot-saas-v2.1 application. The system uses multiple transaction managers due to its multi-database architecture.

## Transaction Managers

### Current Transaction Managers

| Transaction Manager | Database | Primary | Purpose |
|---------------------|----------|---------|---------|
| `sharedTransactionManager` | traloitudong_db | ✅ Yes | Shared entities (address, infrastructure, simplepayment) |
| `userTransactionManager` | traloitudong_db | ❌ No | User entities (user, user_profile) |
| `identityTransactionManager` | identity_db | ❌ No | Identity entities (auth, refresh tokens) |
| `tenantTransactionManager` | tenant_db | ❌ No | Tenant entities (tenant, membership, profile) |
| `messageTransactionManager` | message_db | ❌ No | Message entities (conversations, messages) |
| `facebookTransactionManager` | facebook_db | ❌ No | Facebook connection entities |
| `minioTransactionManager` | minio_db | ❌ No | Minio file metadata |
| `configTransactionManager` | config_db | ❌ No | Configuration entities |
| `spokesTransactionManager` | spokes_db | ❌ No | Spokes entities |

### Database Configuration

All transaction managers use PostgreSQL with identical Hibernate settings:
- Dialect: `org.hibernate.dialect.PostgreSQLDialect`
- DDL Auto: Controlled by `app.hibernate.ddl-auto` (default: none)
- Show SQL: false
- Format SQL: false

## Usage Guidelines

### ✅ DO: Use @Transactional on Service Layer

```java
@Service
public class UserService {
    
    @Transactional(value = "userTransactionManager", rollbackFor = Exception.class)
    public UserResponse updateUser(Long userId, UserRequest request) {
        // Business logic here
        return userRepository.save(user);
    }
}
```

### ❌ DON'T: Use @Transactional on Repository Layer

```java
// ❌ WRONG - Transaction annotation on repository
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    @Modifying
    @Transactional(value = "userTransactionManager", rollbackFor = Exception.class)
    @Query(value = "INSERT INTO user_profiles (user_id, created_at, updated_at) VALUES (:userId, NOW(), NOW())", nativeQuery = true)
    void insertProfile(Long userId);
}
```

```java
// ✅ CORRECT - No transaction annotation on repository
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    @Modifying
    @Query(value = "INSERT INTO user_profiles (user_id, created_at, updated_at) VALUES (:userId, NOW(), NOW())", nativeQuery = true)
    void insertProfile(Long userId);
}
```

### ✅ DO: Specify Transaction Manager Explicitly

```java
@Service
public class AuthService {
    
    @Transactional(value = "userTransactionManager", rollbackFor = Exception.class)
    public UserResponse register(RegisterRequest request) {
        // User database operations
    }
    
    @Transactional(value = "identityTransactionManager", rollbackFor = Exception.class)
    public TokenRefreshResponse refreshToken(String refreshToken) {
        // Identity database operations
    }
}
```

### ✅ DO: Use Default Transaction Manager for Shared Entities

```java
@Service
public class AddressService extends BaseService<Address, UUID, AddressRepository> {
    
    // Uses sharedTransactionManager (primary) by default
    @Transactional
    public Address createAddress(AddressRequest request, String createdBy) {
        // Shared database operations
    }
}
```

### ❌ DON'T: Mix Transaction Managers in Single Method

```java
// ❌ WRONG - Multiple transaction managers in one method
@Transactional(value = "userTransactionManager")
public void complexOperation() {
    userRepository.save(user);
    tenantRepository.save(tenant); // Different transaction manager!
}
```

```java
// ✅ CORRECT - Separate methods with appropriate transaction managers
@Transactional(value = "userTransactionManager")
public void saveUser(User user) {
    userRepository.save(user);
}

@Transactional(value = "tenantTransactionManager")
public void saveTenant(Tenant tenant) {
    tenantRepository.save(tenant);
}
```

## Transaction Manager Selection

### By Module

| Module | Transaction Manager | Example Services |
|--------|---------------------|------------------|
| User | `userTransactionManager` | UserService, UserCleanupService |
| Identity | `identityTransactionManager` | AuthService, RefreshTokenService |
| Tenant | `tenantTransactionManager` | TenantService, MessageUsageService |
| Message | `messageTransactionManager` | ConversationService, MessageService |
| Facebook | `facebookTransactionManager` | FacebookConnectionService, FacebookWebhookService |
| Minio | `minioTransactionManager` | FileMetadataService, CategoryService |
| Config | `configTransactionManager` | ConfigService |
| Spokes | `spokesTransactionManager` | SpokesService |
| Shared | `sharedTransactionManager` (default) | AddressService, BaseService implementations |

### Annotation Syntax

Use consistent annotation syntax:

```java
// ✅ PREFERRED - Use 'value' parameter
@Transactional(value = "userTransactionManager", rollbackFor = Exception.class)

// ⚠️ ACCEPTABLE - Use 'transactionManager' parameter (less common)
@Transactional(transactionManager = "userTransactionManager", rollbackFor = Exception.class)

// ✅ CORRECT - For read-only operations
@Transactional(value = "userTransactionManager", readOnly = true, rollbackFor = Exception.class)

// ✅ CORRECT - For operations requiring new transaction
@Transactional(value = "userTransactionManager", propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
```

## Best Practices

### 1. Transaction Boundaries

- **Keep transactions short**: Only include necessary database operations
- **Avoid external calls in transactions**: Don't make HTTP calls, send emails, or call external APIs within transactions
- **Use appropriate isolation level**: Default is READ_COMMITTED, which is suitable for most cases

### 2. Rollback Behavior

Always specify `rollbackFor = Exception.class` to ensure rollback on all exceptions:

```java
@Transactional(value = "userTransactionManager", rollbackFor = Exception.class)
public void updateUser(Long userId, UserRequest request) {
    // This will rollback on any exception
}
```

### 3. Read-Only Transactions

Use `readOnly = true` for read operations to improve performance:

```java
@Transactional(value = "userTransactionManager", readOnly = true, rollbackFor = Exception.class)
public UserResponse getUser(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found"));
}
```

### 4. Cross-Database Operations

For operations spanning multiple databases, use programmatic transaction management or consider distributed transaction patterns:

```java
@Service
public class UserRegistrationService {
    
    private final UserService userService;
    private final TenantService tenantService;
    
    @Transactional(value = "userTransactionManager", rollbackFor = Exception.class)
    public void registerUserWithTenant(User user, Tenant tenant) {
        // Save user in user database
        userService.saveUser(user);
        
        // Call tenant service (separate transaction)
        tenantService.createTenant(tenant);
    }
}
```

## Common Issues and Solutions

### Issue 1: "No transaction manager found" Error

**Cause**: Using `@Transactional` without specifying transaction manager in a module that doesn't use the primary transaction manager.

**Solution**: Always specify the transaction manager explicitly:

```java
// ❌ WRONG
@Transactional
public void someMethod() { }

// ✅ CORRECT
@Transactional(value = "userTransactionManager", rollbackFor = Exception.class)
public void someMethod() { }
```

### Issue 2: Transaction Not Rolling Back

**Cause**: Not specifying `rollbackFor = Exception.class`.

**Solution**: Always include rollback behavior:

```java
@Transactional(value = "userTransactionManager", rollbackFor = Exception.class)
public void someMethod() { }
```

### Issue 3: Repository Layer Transactions

**Cause**: Adding `@Transactional` on repository methods.

**Solution**: Remove transaction annotations from repositories, add to service layer instead.

## Migration Path

### Consolidation Considerations

The current multi-transaction-manager architecture supports multi-database design. Consider consolidation if:

1. **Single database migration**: If consolidating all databases into one
2. **Simplified architecture**: If reducing complexity is a priority
3. **Performance optimization**: If transaction management overhead is significant

### Consolidation Steps

If consolidation is needed:

1. **Identify shared transaction manager**: Keep `sharedTransactionManager` as primary
2. **Migrate data**: Consolidate databases into single database
3. **Update configurations**: Remove redundant transaction manager beans
4. **Remove explicit transaction manager specifications**: Use default transaction manager
5. **Update documentation**: Reflect simplified architecture

## Testing

### Transaction Testing

Test transaction behavior using `@Transactional` in test methods:

```java
@SpringBootTest
@Transactional(value = "userTransactionManager")
class UserServiceTest {
    
    @Test
    void testUserCreation() {
        // Test transaction behavior
    }
}
```

### Rollback Testing

Verify rollback behavior:

```java
@Test
@Transactional(value = "userTransactionManager")
void testRollbackOnException() {
    assertThrows(Exception.class, () -> {
        userService.createUserWithInvalidData();
    });
    
    // Verify data was not persisted
    assertFalse(userRepository.existsByEmail("test@example.com"));
}
```

## Monitoring and Debugging

### Enable Transaction Logging

Add to `application.properties`:

```properties
logging.level.org.springframework.transaction.interceptor=TRACE
logging.level.org.springframework.transaction.support=DEBUG
```

### Common Logs

- **Transaction started**: `Creating new transaction with name [...]`
- **Transaction committed**: `Initiating transaction commit`
- **Transaction rolled back**: `Initiating transaction rollback`
- **No transaction found**: `No transaction aspect-managed TransactionStatus in scope`

## References

- [Spring Transaction Management](https://docs.spring.io/spring-framework/reference/data-access/transaction.html)
- [Spring Data JPA Transactions](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#transactions)
- [Multi-Database Configuration](https://www.baeldung.com/spring-data-jpa-multiple-databases)

## Summary

- **Use @Transactional on service layer only**
- **Specify transaction manager explicitly** (except for shared entities)
- **Always include rollbackFor = Exception.class**
- **Keep transactions short and focused**
- **Avoid external calls within transactions**
- **Use readOnly = true for read operations**
- **Test transaction behavior thoroughly**
