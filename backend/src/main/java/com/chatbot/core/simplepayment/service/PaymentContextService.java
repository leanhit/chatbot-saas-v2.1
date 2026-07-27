package com.chatbot.core.simplepayment.service;

import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentContextService {
    private final AuthRepository authRepository;
    private final TenantRepository tenantRepository;

    public Long extractUserId(UserDetails userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        String username = userDetails.getUsername();
        User user = authRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return user.getId();
    }

    public Long extractTenantId(HttpServletRequest request) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            log.debug("Found tenant ID from context: {}", tenantId);
            return tenantId;
        }

        String tenantKey = request.getHeader("X-Tenant-Key");
        if (tenantKey != null && !tenantKey.isBlank()) {
            return tenantRepository.findByTenantKey(tenantKey)
                    .map(Tenant::getId)
                    .orElse(null);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            try {
                Long userId = extractUserId(userDetails);
                return tenantRepository.findByUserId(userId)
                        .map(Tenant::getId)
                        .orElse(null);
            } catch (Exception e) {
                log.warn("Could not extract tenant ID from user: {}", e.getMessage());
            }
        }

        log.warn("Tenant ID not found in context, header, or user - user may not have a tenant assigned");
        return null;
    }

    public void validateTenantAccess(Long userId, Long tenantId) {
        if (tenantId == null) {
            throw new AccessDeniedException("User has no tenant assigned. Please contact support.");
        }
        if (!tenantRepository.existsByUserIdAndTenantId(userId, tenantId)) {
            log.warn("Unauthorized access attempt: User {} trying to access tenant {}", userId, tenantId);
            throw new AccessDeniedException("Unauthorized access to tenant");
        }
    }
}
