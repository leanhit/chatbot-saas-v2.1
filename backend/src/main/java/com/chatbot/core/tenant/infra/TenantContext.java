package com.chatbot.core.tenant.infra;

import org.springframework.util.Assert;

/**
 * Tenant Context - Thread-local storage for current tenant information
 */
public class TenantContext {
    
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<Long> CURRENT_TENANT_ID = new ThreadLocal<>();
    
    /**
     * Set current tenant for the current thread
     */
    public static void setCurrentTenant(String tenantKey) {
        Assert.hasText(tenantKey, "Tenant key must not be empty");
        CURRENT_TENANT.set(tenantKey);
    }
    
    /**
     * Get current tenant for the current thread
     */
    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }
    
    /**
     * Set current tenant ID for the current thread
     */
    public static void setTenantId(Long tenantId) {
        Assert.notNull(tenantId, "Tenant ID must not be null");
        CURRENT_TENANT_ID.set(tenantId);
    }
    
    /**
     * Get current tenant ID for the current thread
     */
    public static Long getTenantId() {
        return CURRENT_TENANT_ID.get();
    }
    
    /**
     * Check if tenant context is set
     */
    public static boolean hasTenant() {
        return CURRENT_TENANT.get() != null;
    }
    
    /**
     * Clear current tenant for the current thread
     */
    public static void clear() {
        CURRENT_TENANT.remove();
        CURRENT_TENANT_ID.remove();
    }
    
    /**
     * Execute operation with tenant context
     */
    public static <T> T executeWithTenant(String tenantKey, TenantOperation<T> operation) {
        String originalTenant = getCurrentTenant();
        Long originalTenantId = getTenantId();
        try {
            setCurrentTenant(tenantKey);
            return operation.execute();
        } finally {
            if (originalTenant != null) {
                setCurrentTenant(originalTenant);
            } else {
                CURRENT_TENANT.remove();
            }
            if (originalTenantId != null) {
                setTenantId(originalTenantId);
            } else {
                CURRENT_TENANT_ID.remove();
            }
        }
    }
    
    /**
     * Execute operation with tenant ID context
     */
    public static <T> T executeWithTenantId(Long tenantId, TenantIdOperation<T> operation) {
        Long originalTenantId = getTenantId();
        try {
            setTenantId(tenantId);
            return operation.execute();
        } finally {
            if (originalTenantId != null) {
                setTenantId(originalTenantId);
            } else {
                CURRENT_TENANT_ID.remove();
            }
        }
    }
    
    @FunctionalInterface
    public interface TenantOperation<T> {
        T execute();
    }
    
    @FunctionalInterface
    public interface TenantIdOperation<T> {
        T execute();
    }
}
