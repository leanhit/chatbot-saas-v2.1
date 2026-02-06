package com.chatbot.modules.tenant.infra;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Tenant Context Provider - unified access to tenant information
 * 
 * This class provides a single point of access for tenant context,
 * supporting both:
 * 1. New request-scoped context (from JWT claims)
 * 2. Legacy ThreadLocal context (from X-Tenant-ID header)
 * 
 * Usage:
 * - getCurrentTenantId() - gets current active tenant ID
 * - getTenantIds() - gets all tenant IDs user has access to
 * - hasAccessToTenant(tenantId) - checks if user can access specific tenant
 * 
 * v0.2: Updated to use UUID for tenant IDs
 */
@Component
@Slf4j
public class TenantContextProvider {

    private final RequestTenantContext requestTenantContext;

    public TenantContextProvider(RequestTenantContext requestTenantContext) {
        this.requestTenantContext = requestTenantContext;
    }

    /**
     * Get current tenant ID
     * Priority: Request context > ThreadLocal > null
     */
    public UUID getCurrentTenantId() {
        // First try request context (Identity Hub tokens)
        UUID requestTenantId = requestTenantContext.getCurrentTenantId();
        if (requestTenantId != null) {
            return requestTenantId;
        }

        // Fallback to ThreadLocal (legacy tokens)
        return TenantContext.getTenantId();
    }

    /**
     * Get all tenant IDs user has access to
     * Only available from request context (Identity Hub tokens)
     */
    public List<UUID> getTenantIds() {
        // Only request context has the full list of tenant IDs
        return requestTenantContext.getTenantIds();
    }

    /**
     * Check if user has access to specific tenant
     */
    public boolean hasAccessToTenant(UUID tenantId) {
        if (tenantId == null) {
            return false;
        }

        // Check request context first (Identity Hub tokens)
        if (requestTenantContext.hasAccessToTenant(tenantId)) {
            return true;
        }

        // Fallback to current tenant check (legacy tokens)
        UUID currentTenantId = getCurrentTenantId();
        return tenantId.equals(currentTenantId);
    }

    /**
     * Check if user has any tenant access
     */
    public boolean hasAnyTenantAccess() {
        return getCurrentTenantId() != null || !getTenantIds().isEmpty();
    }

    /**
     * Get tenant count
     */
    public int getTenantCount() {
        List<UUID> tenantIds = getTenantIds();
        if (!tenantIds.isEmpty()) {
            return tenantIds.size();
        }
        
        return getCurrentTenantId() != null ? 1 : 0;
    }

    /**
     * Check if this is an Identity Hub token (has multiple tenants)
     */
    public boolean isIdentityHubToken() {
        return !getTenantIds().isEmpty();
    }

    /**
     * Check if this is a legacy token (single tenant from header)
     */
    public boolean isLegacyToken() {
        return getTenantIds().isEmpty() && getCurrentTenantId() != null;
    }

    /**
     * Clear tenant context (for cleanup)
     */
    public void clear() {
        requestTenantContext.clear();
        TenantContext.clear();
    }
}
