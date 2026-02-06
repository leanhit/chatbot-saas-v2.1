package com.chatbot.modules.tenant.context;

import java.util.UUID;

/**
 * TenantContext - ThreadLocal storage for current tenant ID
 * 
 * Provides request-scoped tenant context for multi-tenant SaaS platform.
 * Tenant is extracted from X-Tenant-ID header and validated against user membership.
 * 
 * This is a utility class with static methods - no dependency injection needed.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
        // Utility class - prevent instantiation
    }

    /**
     * Set the current tenant ID for this thread/request
     * 
     * @param tenantId the tenant ID to set
     */
    public static void setTenantId(UUID tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Get the current tenant ID for this thread/request
     * 
     * @return the current tenant ID, or null if not set
     */
    public static UUID getTenantId() {
        return CURRENT_TENANT.get();
    }

    /**
     * Clear the tenant context for this thread/request
     * Should be called after request processing completes
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
