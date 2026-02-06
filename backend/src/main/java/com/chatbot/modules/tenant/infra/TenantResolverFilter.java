package com.chatbot.modules.tenant.infra;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Tenant Resolver Filter - handles both legacy and Identity Hub approaches
 * 
 * For Identity Hub tokens: tenant_ids are already in request context from JwtFilter
 * For Legacy tokens: still reads X-Tenant-ID header and sets in ThreadLocal for backward compatibility
 * 
 * v0.2: Updated to handle UUID tenant IDs
 */
@Component
@Slf4j
public class TenantResolverFilter extends OncePerRequestFilter {

    private final RequestTenantContext requestTenantContext;

    public TenantResolverFilter(RequestTenantContext requestTenantContext) {
        this.requestTenantContext = requestTenantContext;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/identity/login") 
            || path.equals("/api/identity/refresh")
            || path.startsWith("/api/identity/")
            || path.startsWith("/api/test/") // Bỏ qua tenant filter cho test endpoints
            || path.startsWith("/api/addresses"); // Bỏ qua tenant filter cho address endpoints
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // Check if tenant_ids are already set by JwtFilter (Identity Hub tokens)
            if (requestTenantContext.getTenantIds().isEmpty()) {
                // No tenant_ids from JWT - try legacy X-Tenant-ID header
                String tenantHeader = request.getHeader("X-Tenant-ID");

                if (tenantHeader != null && !tenantHeader.isBlank()) {
                    try {
                        UUID tenantId = UUID.fromString(tenantHeader);
                        
                        // Set in ThreadLocal for backward compatibility
                        TenantContext.setTenantId(tenantId);
                        
                        // Also set in request context for consistency
                        requestTenantContext.setCurrentTenantId(request, tenantId);
                        
                        log.debug("Set tenant from X-Tenant-ID header: {}", tenantId);
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid X-Tenant-ID header value: {}", tenantHeader);
                    }
                }
            } else {
                // Tenant IDs already set by JwtFilter - also set in ThreadLocal for backward compatibility
                UUID currentTenantId = requestTenantContext.getCurrentTenantId();
                if (currentTenantId != null) {
                    TenantContext.setTenantId(currentTenantId);
                    log.debug("Set tenant from JWT context: {}", currentTenantId);
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            // Always clear ThreadLocal (but keep request context)
            TenantContext.clear();
        }
    }
}
