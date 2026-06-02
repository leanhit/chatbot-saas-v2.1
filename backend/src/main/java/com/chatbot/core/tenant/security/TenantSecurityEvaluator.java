package com.chatbot.core.tenant.security;

import com.chatbot.core.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.core.user.repository.AuthRepository;
import com.chatbot.core.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component("tenantSecurity")
@RequiredArgsConstructor
public class TenantSecurityEvaluator {

    private final TenantMemberRepository tenantMemberRepository;
    private final com.chatbot.core.tenant.service.TenantService tenantService;
    private final AuthRepository authRepository;

    /**
     * Check if current user is member of tenant
     */
    public boolean isTenantMember(Long tenantId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }

            String userEmail = auth.getName();
            Long userId = authRepository.findByEmail(userEmail)
                    .map(User::getId)
                    .orElse(null);
            if (userId == null) return false;
            
            return tenantMemberRepository.findByTenantIdAndUserIdAndStatus(
                tenantId, userId, com.chatbot.core.tenant.membership.model.MembershipStatus.ACTIVE
            ).isPresent();
        } catch (Exception e) {
            log.error("Error checking tenant membership", e);
            return false;
        }
    }

    /**
     * Check if current user is member of tenant by tenant key
     */
    public boolean isTenantMemberByKey(String tenantKey) {
        try {
            Long tenantId = tenantService.getTenantIdByKey(tenantKey);
            if (tenantId == null) {
                return false;
            }
            return isTenantMember(tenantId);
        } catch (Exception e) {
            log.error("Error checking tenant membership by key", e);
            return false;
        }
    }

    /**
     * Check if current user is tenant owner
     */
    public boolean isTenantOwner(Long tenantId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }

            String userEmail = auth.getName();
            Long userId = authRepository.findByEmail(userEmail)
                    .map(User::getId)
                    .orElse(null);
            if (userId == null) return false;
            
            // Find member and check role
            return tenantMemberRepository.findByTenantIdAndUserIdAndStatus(
                tenantId, userId, com.chatbot.core.tenant.membership.model.MembershipStatus.ACTIVE
            ).map(member -> member.getRole() == com.chatbot.core.tenant.membership.model.TenantRole.OWNER)
            .orElse(false);
        } catch (Exception e) {
            log.error("Error checking tenant ownership", e);
            return false;
        }
    }

    /**
     * Check if current user can manage tenant
     */
    public boolean canManageTenant(Long tenantId) {
        return isTenantOwner(tenantId) || isAdmin();
    }

    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }

            return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        } catch (Exception e) {
            log.error("Error checking admin role", e);
            return false;
        }
    }
}
