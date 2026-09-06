package com.chatbot.core.payment.transaction.service;

import com.chatbot.core.tenant.infra.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentContextService {

    /**
     * Extract user ID from UserDetails
     */
    public Long extractUserId(UserDetails userDetails) {
        // This will be implemented after user service integration
        // For now, return a placeholder
        log.debug("Extracting user ID from UserDetails: {}", userDetails.getUsername());
        return 0L; // Placeholder
    }

    /**
     * Extract tenant ID from HTTP request
     */
    public Long extractTenantId(HttpServletRequest request) {
        // Try to get from TenantContext first
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            return tenantId;
        }

        // Fallback to header extraction
        String tenantIdHeader = request.getHeader("X-Tenant-ID");
        if (tenantIdHeader != null) {
            try {
                return Long.parseLong(tenantIdHeader);
            } catch (NumberFormatException e) {
                log.warn("Invalid tenant ID in header: {}", tenantIdHeader);
            }
        }

        return null;
    }

    /**
     * Validate that user has access to the tenant
     */
    public void validateTenantAccess(Long userId, Long tenantId) {
        // This will be implemented after tenant service integration
        log.debug("Validating tenant access for user: {}, tenant: {}", userId, tenantId);
    }
}
