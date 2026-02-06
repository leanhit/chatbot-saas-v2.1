package com.chatbot.modules.tenant.infra;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.UUID;

/**
 * Request-scoped Tenant Context
 * Reads tenant information from JWT claims stored in request attributes
 * Safe for async operations and doesn't rely on ThreadLocal
 * 
 * v0.2: Updated to use UUID for tenant IDs
 */
@Component
@Slf4j
public class RequestTenantContext {

    private static final String TENANT_IDS_ATTR = "tenant_ids";
    private static final String CURRENT_TENANT_ID_ATTR = "current_tenant_id";

    /**
     * Get current tenant ID from request context
     */
    public UUID getCurrentTenantId() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request == null) {
                return null;
            }

            // First try to get explicitly set current tenant
            Object currentTenant = request.getAttribute(CURRENT_TENANT_ID_ATTR);
            if (currentTenant instanceof UUID) {
                return (UUID) currentTenant;
            }

            // Fallback to first tenant from list
            List<UUID> tenantIds = getTenantIds();
            if (tenantIds != null && !tenantIds.isEmpty()) {
                return tenantIds.get(0);
            }

            return null;
        } catch (Exception e) {
            log.debug("Could not get current tenant ID", e);
            return null;
        }
    }

    /**
     * Get all tenant IDs from request context
     */
    @SuppressWarnings("unchecked")
    public List<UUID> getTenantIds() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request == null) {
                return List.of();
            }

            Object tenantIdsObj = request.getAttribute(TENANT_IDS_ATTR);
            if (tenantIdsObj instanceof List) {
                return (List<UUID>) tenantIdsObj;
            }

            return List.of();
        } catch (Exception e) {
            log.debug("Could not get tenant IDs", e);
            return List.of();
        }
    }

    /**
     * Set tenant IDs in request context (called by JWT filter)
     */
    public void setTenantIds(HttpServletRequest request, List<UUID> tenantIds) {
        if (request != null && tenantIds != null) {
            request.setAttribute(TENANT_IDS_ATTR, tenantIds);
            log.debug("Set tenant_ids in request context: {}", tenantIds);
        }
    }

    /**
     * Set current tenant ID explicitly
     */
    public void setCurrentTenantId(HttpServletRequest request, UUID tenantId) {
        if (request != null) {
            request.setAttribute(CURRENT_TENANT_ID_ATTR, tenantId);
            log.debug("Set current_tenant_id in request context: {}", tenantId);
        }
    }

    /**
     * Check if user has access to specific tenant
     */
    public boolean hasAccessToTenant(UUID tenantId) {
        List<UUID> tenantIds = getTenantIds();
        return tenantIds != null && tenantIds.contains(tenantId);
    }

    /**
     * Get current HTTP request
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.debug("Could not get current request", e);
            return null;
        }
    }

    /**
     * Clear tenant context (for cleanup)
     */
    public void clear() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                request.removeAttribute(TENANT_IDS_ATTR);
                request.removeAttribute(CURRENT_TENANT_ID_ATTR);
            }
        } catch (Exception e) {
            log.debug("Could not clear tenant context", e);
        }
    }
}
