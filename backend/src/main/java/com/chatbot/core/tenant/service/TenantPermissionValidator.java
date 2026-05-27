package com.chatbot.core.tenant.service;

import com.chatbot.core.identity.model.SystemRole;
import com.chatbot.core.tenant.membership.model.MembershipStatus;
import com.chatbot.core.tenant.membership.model.TenantRole;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.UserRepository;
import com.chatbot.core.tenant.membership.repository.TenantMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Centralised helper cho permission checks liên quan đến tenant.
 * Đây là nguồn truth duy nhất — tất cả service phải dùng class này
 * thay vì tự viết inline auth checks.
 */
@Component
@RequiredArgsConstructor
public class TenantPermissionValidator {

    private final UserRepository userRepository;
    private final TenantMemberRepository tenantMemberRepository;

    /**
     * Trả về email của user đang authenticate.
     */
    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        return auth.getName();
    }

    /**
     * Kiểm tra user có phải SYSTEM ADMIN không (dựa vào SystemRole trong DB).
     */
    public boolean isAdmin(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .map(User::getSystemRole)
                .map(role -> role == SystemRole.ADMIN)
                .orElse(false);
    }

    /**
     * Kiểm tra user có phải OWNER của tenant không.
     */
    public boolean isOwner(Long tenantId, String userEmail) {
        return tenantMemberRepository
                .findByTenantIdAndUserEmailAndStatus(tenantId, userEmail, MembershipStatus.ACTIVE)
                .map(member -> member.getRole() == TenantRole.OWNER)
                .orElse(false);
    }

    /**
     * Kiểm tra user có phải ADMIN hoặc OWNER của tenant không.
     */
    public boolean isAdminOrOwner(Long tenantId, String userEmail) {
        if (isAdmin(userEmail)) return true;
        return isOwner(tenantId, userEmail);
    }

    /**
     * Kiểm tra user có phải TENANT ADMIN (admin trong tenant, không phải system admin) không.
     */
    public boolean isTenantAdmin(Long tenantId, String userEmail) {
        return tenantMemberRepository
                .findByTenantIdAndUserEmailAndStatus(tenantId, userEmail, MembershipStatus.ACTIVE)
                .map(member -> member.getRole() == TenantRole.ADMIN || member.getRole() == TenantRole.OWNER)
                .orElse(false);
    }

    /**
     * Kiểm tra user có phải ACTIVE member của tenant không.
     */
    public boolean isActiveMember(Long tenantId, String userEmail) {
        return tenantMemberRepository
                .findByTenantIdAndUserEmailAndStatus(tenantId, userEmail, MembershipStatus.ACTIVE)
                .isPresent();
    }
}
