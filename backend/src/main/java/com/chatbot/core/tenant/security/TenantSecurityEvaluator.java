package com.chatbot.core.tenant.security;

import com.chatbot.core.tenant.membership.repository.TenantMemberRepository;
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
            return tenantMemberRepository.findByTenantIdAndUserEmailAndStatus(
                tenantId, userEmail, com.chatbot.core.tenant.membership.model.MembershipStatus.ACTIVE
            ).isPresent();
        } catch (Exception e) {
            log.error("Error checking tenant membership", e);
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
            // Find member and check role
            return tenantMemberRepository.findByTenantIdAndUserEmailAndStatus(
                tenantId, userEmail, com.chatbot.core.tenant.membership.model.MembershipStatus.ACTIVE
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
