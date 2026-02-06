package com.chatbot.modules.tenant.context;

import com.chatbot.modules.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.modules.tenant.core.util.SecurityContextUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * TenantContextFilter - Extracts and validates tenant from X-Tenant-ID header
 * 
 * This filter runs after JWT authentication to establish tenant context for each request.
 * It validates that the current user is a member of the specified tenant before setting context.
 * 
 * Flow:
 * 1. Extract X-Tenant-ID header
 * 2. Get current user from SecurityContext
 * 3. Validate user membership in tenant
 * 4. Set tenant context if valid, otherwise return 403
 * 5. Clear context after request completes
 */
@Component
@RequiredArgsConstructor
public class TenantContextFilter extends OncePerRequestFilter {

    private final TenantMemberRepository tenantMemberRepository;
    private final SecurityContextUtil securityContextUtil;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String tenantHeader = request.getHeader("X-Tenant-ID");

        if (tenantHeader != null && !tenantHeader.isBlank()) {
            try {
                UUID tenantId = UUID.fromString(tenantHeader);
                UUID userId = securityContextUtil.getCurrentUserIdUUID();

                if (userId != null) {
                    boolean isMember = tenantMemberRepository.existsByTenantIdAndUserId(tenantId, userId);

                    if (!isMember) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "User is not a member of this tenant");
                        return;
                    }

                    TenantContext.setTenantId(tenantId);
                }
            } catch (IllegalArgumentException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tenant ID format");
                return;
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
