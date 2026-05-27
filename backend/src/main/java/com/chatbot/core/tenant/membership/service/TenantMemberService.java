package com.chatbot.core.tenant.membership.service;

import com.chatbot.core.user.model.User;
import com.chatbot.core.tenant.membership.dto.*;
import com.chatbot.core.tenant.membership.model.*;
import com.chatbot.core.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.tenant.service.TenantAuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TenantMemberService {

    private final TenantMemberRepository memberRepo;
    private final TenantRepository tenantRepo;
    private final TenantAuditLogService auditLogService;

    /* ================= LIST ================= */

    public Page<MemberResponse> listMembers(Long tenantId, Pageable pageable) {
        return memberRepo
                .findByTenant_IdAndStatus(tenantId, MembershipStatus.ACTIVE, pageable)
                .map(this::toResponse);
    }

    /* ================= GET ================= */

    public MemberResponse getMember(Long tenantId, Long userId) {
        return getMemberEntity(tenantId, userId)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalStateException("Member not found"));
    }

    /** GET /tenants/{tenantId}/members/me */
    public MemberResponse getMyMember(Long tenantId, User user) {
        return getMember(tenantId, user.getId());
    }

    /* ================= UPDATE ================= */

    @Transactional
    public void updateRole(Long tenantId, Long targetUserId, TenantRole newRole) {
        // Lấy actor (người thực hiện request) từ SecurityContext
        String actorEmail = getCurrentUserEmail();

        // Kiểm tra quyền của ACTOR, không phải của target
        if (!canManageMembers(tenantId, actorEmail)) {
            throw new IllegalStateException("Không đủ quyền để quản lý thành viên");
        }

        // Kiểm tra actor có thể gán role này không
        if (!canAssignRole(tenantId, actorEmail, newRole)) {
            throw new IllegalStateException("Không thể gán role cao hơn quyền hiện tại của bạn");
        }

        TenantMember targetMember = getMemberEntityRequired(tenantId, targetUserId);

        if (targetMember.getRole() == TenantRole.OWNER) {
            throw new IllegalStateException("Không thể thay đổi role của OWNER");
        }

        targetMember.setRole(newRole);

        auditLogService.logAction(tenantId, actorEmail, "UPDATE_MEMBER_ROLE",
                "Changed userId=" + targetUserId + " role to " + newRole);
    }

    /* ================= DELETE ================= */

    @Transactional
    public void removeMember(Long tenantId, Long targetUserId) {
        String actorEmail = getCurrentUserEmail();

        if (!canManageMembers(tenantId, actorEmail)) {
            throw new IllegalStateException("Không đủ quyền để xóa thành viên");
        }

        TenantMember targetMember = getMemberEntityRequired(tenantId, targetUserId);

        if (targetMember.getRole() == TenantRole.OWNER) {
            throw new IllegalStateException("Không thể xóa OWNER khỏi tenant");
        }

        memberRepo.delete(targetMember);

        auditLogService.logAction(tenantId, actorEmail, "REMOVE_MEMBER",
                "Removed userId=" + targetUserId);
    }

    /* ================= HELPERS ================= */

    Optional<TenantMember> getMemberEntity(Long tenantId, Long userId) {
        return memberRepo.findByTenant_IdAndUser_Id(tenantId, userId);
    }

    TenantMember getMemberEntityRequired(Long tenantId, Long userId) {
        return getMemberEntity(tenantId, userId)
                .orElseThrow(() -> new IllegalStateException("Member not found"));
    }

    /**
     * Kiểm tra ACTOR (người thực hiện request) có quyền quản lý members không.
     * Phải check role của actor, không phải target.
     */
    private boolean canManageMembers(Long tenantId, String actorEmail) {
        try {
            Optional<TenantMember> actorMembership = memberRepo
                    .findByTenantIdAndUserEmailAndStatus(tenantId, actorEmail, MembershipStatus.ACTIVE);
            if (actorMembership.isEmpty()) return false;

            TenantMember actor = actorMembership.get();
            User actorUser = actor.getUser();

            // System ADMIN luôn có quyền
            if (actorUser.getSystemRole() == com.chatbot.core.identity.model.SystemRole.ADMIN) {
                return true;
            }
            // OWNER và ADMIN của tenant có quyền quản lý member
            return actor.getRole() == TenantRole.OWNER || actor.getRole() == TenantRole.ADMIN;

        } catch (Exception e) {
            log.error("Error checking manage-member permission for actor={}: {}", actorEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Kiểm tra ACTOR có thể gán targetRole cho người khác không.
     */
    private boolean canAssignRole(Long tenantId, String actorEmail, TenantRole targetRole) {
        try {
            Optional<TenantMember> actorMembership = memberRepo
                    .findByTenantIdAndUserEmailAndStatus(tenantId, actorEmail, MembershipStatus.ACTIVE);
            if (actorMembership.isEmpty()) return false;

            TenantMember actor = actorMembership.get();
            User actorUser = actor.getUser();

            // System ADMIN có thể gán bất kỳ role nào
            if (actorUser.getSystemRole() == com.chatbot.core.identity.model.SystemRole.ADMIN) {
                return true;
            }

            TenantRole actorRole = actor.getRole();

            // OWNER có thể gán bất kỳ role nào trừ OWNER khác
            if (actorRole == TenantRole.OWNER) {
                return targetRole != TenantRole.OWNER;
            }
            // ADMIN có thể gán EDITOR và MEMBER
            if (actorRole == TenantRole.ADMIN) {
                return targetRole == TenantRole.EDITOR || targetRole == TenantRole.MEMBER;
            }
            // EDITOR và MEMBER không có quyền gán role
            return false;

        } catch (Exception e) {
            log.error("Error checking assign-role permission for actor={}: {}", actorEmail, e.getMessage());
            return false;
        }
    }

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("User chưa được xác thực");
        }
        return auth.getName();
    }

    private MemberResponse toResponse(TenantMember m) {
        return MemberResponse.builder()
                .id(m.getId())
                .userId(m.getUser().getId())
                .email(m.getUser().getEmail())
                .role(m.getRole())
                .joinedAt(m.getJoinedAt() != null ? m.getJoinedAt() : m.getCreatedAt())
                .build();
    }

    public Long getTenantIdByKey(String tenantKey) {
        return tenantRepo.findByTenantKey(tenantKey)
                .map(Tenant::getId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with key: " + tenantKey));
    }
}
