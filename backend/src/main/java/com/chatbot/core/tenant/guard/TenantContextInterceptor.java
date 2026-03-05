package com.chatbot.core.tenant.guard;

import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.tenant.service.TenantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Tenant Context Interceptor - Extracts tenant key from header and sets it in TenantContext
 */
@Component
@Slf4j
public class TenantContextInterceptor implements HandlerInterceptor {

    private final TenantService tenantService;

    public TenantContextInterceptor(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        
        // Skip tenant context setting for auth endpoints and system endpoints
        if (isExcludedPath(path)) {
            return true;
        }

        // Extract tenant key from header
        String tenantKey = request.getHeader("X-Tenant-Key");
        
        if (tenantKey != null && !tenantKey.trim().isEmpty()) {
            try {
                // Validate tenant exists and get tenant ID
                Long tenantId = tenantService.getTenantIdByKey(tenantKey);
                if (tenantId != null) {
                    // Set both tenant key and tenant ID in context
                    TenantContext.setCurrentTenant(tenantKey);
                    TenantContext.setTenantId(tenantId);
                    log.debug("🏢 Set tenant context: {} (ID: {})", tenantKey, tenantId);
                } else {
                    log.warn("⚠️ Tenant not found for key: {}", tenantKey);
                }
            } catch (Exception e) {
                log.error("❌ Error setting tenant context for key: {}", tenantKey, e);
                // Don't block the request, just log the error
            }
        } else {
            log.debug("🔍 No X-Tenant-Key header found for path: {}", path);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Clear tenant context after request completes
        TenantContext.clear();
    }

    private boolean isExcludedPath(String path) {
        return path.startsWith("/api/auth/") ||
               path.startsWith("/api/actuator/") ||
               path.startsWith("/api/error") ||
               path.equals("/api/health") ||
               path.startsWith("/api/public/");
    }
}
