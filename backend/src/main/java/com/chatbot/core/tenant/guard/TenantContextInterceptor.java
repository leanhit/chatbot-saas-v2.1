package com.chatbot.core.tenant.guard;

import com.chatbot.core.tenant.exception.TenantInactiveException;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.tenant.service.TenantService;
import com.chatbot.core.tenant.service.TenantService.TenantKeyInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Tenant Context Interceptor - Extracts tenant key from header, validates tenant status, and sets it in TenantContext
 * Merged with TenantStatusInterceptor to reduce DB queries from 2 to 1 per request
 * Uses cached TenantService.getTenantInfoByKey() to avoid bypassing cache
 */
@Component
@Slf4j
public class TenantContextInterceptor implements HandlerInterceptor {

    private final TenantRepository tenantRepository;
    private final TenantService tenantService;

    public TenantContextInterceptor(TenantRepository tenantRepository, TenantService tenantService) {
        this.tenantRepository = tenantRepository;
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
                // Use cached service method to avoid bypassing cache
                TenantKeyInfo tenantInfo = tenantService.getTenantInfoByKey(tenantKey);
                if (tenantInfo != null) {
                    // Check tenant status (merged from TenantStatusInterceptor)
                    if (tenantInfo.status() != TenantStatus.ACTIVE) {
                        throw new TenantInactiveException("Tenant is not active: " + tenantInfo.status());
                    }

                    // Set both tenant key and tenant ID in context
                    TenantContext.setCurrentTenant(tenantKey);
                    TenantContext.setTenantId(tenantInfo.id());
                    log.debug("[TenantContext] Set tenant context: {} (ID: {}, Status: {})", tenantKey, tenantInfo.id(), tenantInfo.status());
                } else {
                    log.warn("[TenantContext] Tenant not found for key: {}", tenantKey);
                }
            } catch (TenantInactiveException e) {
                // Re-throw status check exceptions
                throw e;
            } catch (Exception e) {
                log.error("[TenantContext] Error setting tenant context for key: {}", tenantKey, e);
                // Don't block the request, just log the error
            }
        } else {
            log.debug("[TenantContext] No X-Tenant-Key header found for path: {}", path);
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
               path.startsWith("/api/public/health") ||
               path.startsWith("/api/public/bank-info") ||
               path.startsWith("/api/public/test/");
    }
}
