package com.chatbot.config.transaction;

import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * Custom annotation for type-safe transaction manager selection.
 * 
 * This annotation provides compile-time type safety when selecting transaction managers,
 * preventing typos and making the code more maintainable.
 * 
 * Note: This annotation is currently for documentation and type-safety purposes only.
 * The actual transaction manager selection must still be done using the standard
 * @Transactional annotation with the explicit transaction manager name.
 * 
 * Usage Pattern:
 * <pre>
 * {@code
 * // Type-safe annotation for documentation
 * @TransactionalWith(UserTransactionManager.class)
 * @Transactional(value = "userTransactionManager", rollbackFor = Exception.class)
 * public void someMethod() { }
 * 
 * // Read-only operation
 * @TransactionalWith(UserTransactionManager.class)
 * @Transactional(value = "userTransactionManager", readOnly = true, rollbackFor = Exception.class)
 * public User getUser(Long id) { }
 * }
 * </pre>
 * 
 * Future Enhancement: An AOP aspect could be added to automatically translate
 * @TransactionalWith to @Transactional with the correct transaction manager.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Transactional
public @interface TransactionalWith {
    
    /**
     * The transaction manager class to use.
     * Must be one of the predefined transaction manager classes.
     */
    Class<? extends TransactionManager> value();
    
    /**
     * Whether the transaction is read-only.
     * Default: false
     */
    boolean readOnly() default false;
    
    /**
     * Propagation behavior.
     * Default: REQUIRED
     */
    Propagation propagation() default Propagation.REQUIRED;
    
    /**
     * Isolation level.
     * Default: DEFAULT
     */
    Isolation isolation() default Isolation.DEFAULT;
    
    /**
     * Timeout in seconds.
     * Default: -1 (no timeout)
     */
    int timeout() default -1;
    
    /**
     * Exception classes that should trigger rollback.
     * Default: Exception.class (all exceptions)
     */
    Class<? extends Throwable>[] rollbackFor() default {Exception.class};
    
    /**
     * Exception classes that should NOT trigger rollback.
     * Default: none
     */
    Class<? extends Throwable>[] noRollbackFor() default {};
    
    /**
     * Propagation enum matching Spring's TransactionDefinition.
     */
    enum Propagation {
        REQUIRED,
        REQUIRES_NEW,
        MANDATORY,
        SUPPORTS,
        NOT_SUPPORTED,
        NEVER,
        NESTED
    }
    
    /**
     * Isolation enum matching Spring's TransactionDefinition.
     */
    enum Isolation {
        DEFAULT,
        READ_UNCOMMITTED,
        READ_COMMITTED,
        REPEATABLE_READ,
        SERIALIZABLE
    }
    
    /**
     * Predefined transaction manager classes for type-safe selection.
     */
    interface TransactionManager {}
    
    /**
     * Shared transaction manager (primary).
     * Used for: address, infrastructure, simplepayment entities.
     */
    class SharedTransactionManager implements TransactionManager {}
    
    /**
     * User transaction manager.
     * Used for: user, user_profile entities.
     */
    class UserTransactionManager implements TransactionManager {}
    
    /**
     * Identity transaction manager.
     * Used for: auth, refresh_token entities.
     */
    class IdentityTransactionManager implements TransactionManager {}
    
    /**
     * Tenant transaction manager.
     * Used for: tenant, membership, profile, professional entities.
     */
    class TenantTransactionManager implements TransactionManager {}
    
    /**
     * Message transaction manager.
     * Used for: conversation, message entities.
     */
    class MessageTransactionManager implements TransactionManager {}
    
    /**
     * Facebook transaction manager.
     * Used for: facebook_connection entities.
     */
    class FacebookTransactionManager implements TransactionManager {}
    
    /**
     * Minio transaction manager.
     * Used for: file_metadata, category entities.
     */
    class MinioTransactionManager implements TransactionManager {}
    
    /**
     * Config transaction manager.
     * Used for: configuration entities.
     */
    class ConfigTransactionManager implements TransactionManager {}
    
    /**
     * Spokes transaction manager.
     * Used for: spokes entities.
     */
    class SpokesTransactionManager implements TransactionManager {}
}
