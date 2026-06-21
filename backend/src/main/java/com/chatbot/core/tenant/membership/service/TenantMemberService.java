package com.chatbot.core.tenant.membership.service;

import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.UserRepository;
import com.chatbot.core.user.repository.AuthRepository;
import com.chatbot.core.tenant.membership.dto.*;
import com.chatbot.core.tenant.membership.model.*;
import com.chatbot.core.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.tenant.service.TenantAuditLogService;
import com.chatbot.core.tenant.service.TenantPermissionValidator;
import com.chatbot.core.tenant.exception.BusinessLogicException;
import com.chatbot.core.tenant.exception.InsufficientPermissionException;
import com.chatbot.core.tenant.exception.TenantNotFoundException;
import com.chatbot.shared.exceptions.ResourceNotFoundException;
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
@Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
@Slf4j
public class TenantMemberService {

    private final TenantMemberRepository memberRepo;
    private final TenantRepository tenantRepo;
    private final TenantAuditLogService auditLogService;
    private final TenantPermissionValidator permissionValidator;
    private final UserRepository userRepository; // Added for application-level join
    private final AuthRepository authRepository;

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
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
    }

    /** GET /tenants/{tenantId}/members/me */
    public MemberResponse getMyMember(Long tenantId) {
        String email = permissionValidator.getCurrentUserEmail();
        Long userId = authRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return getMember(tenantId, userId);
    }

    /* ================= UPDATE ================= */

    @Transactional(transactionManager = "tenantTransactionManager")
    public void updateRole(Long tenantId, Long targetUserId, TenantRole newRole) {
        // Lấy actor (người thực hiện request) từ SecurityContext
        String actorEmail = getCurrentUserEmail();

        // Kiểm tra quyền của ACTOR, không phải của target
        if (!canManageMembers(tenantId, actorEmail)) {
            throw new InsufficientPermissionException("Không đủ quyền để quản lý thành viên");
        }

        // Kiểm tra actor có thể gán role này không (Dùng Validator chung)
        if (!permissionValidator.canAssignRole(tenantId, actorEmail, newRole)) {
            throw new InsufficientPermissionException("Không thể gán role cao hơn quyền hiện tại của bạn");
        }

        TenantMember targetMember = getMemberEntityRequired(tenantId, targetUserId);

        if (targetMember.getRole() == TenantRole.OWNER) {
            throw new BusinessLogicException("Không thể thay đổi role của OWNER");
        }

        targetMember.setRole(newRole);

        auditLogService.logAction(tenantId, actorEmail, "UPDATE_MEMBER_ROLE",
                "Changed userId=" + targetUserId + " role to " + newRole);
    }

    /* ================= DELETE ================= */

    @Transactional(transactionManager = "tenantTransactionManager")
    public void removeMember(Long tenantId, Long targetUserId) {
        String actorEmail = getCurrentUserEmail();

        if (!canManageMembers(tenantId, actorEmail)) {
            throw new InsufficientPermissionException("Không đủ quyền để xóa thành viên");
        }

        TenantMember targetMember = getMemberEntityRequired(tenantId, targetUserId);

        if (targetMember.getRole() == TenantRole.OWNER) {
            throw new BusinessLogicException("Không thể xóa OWNER khỏi tenant");
        }

        memberRepo.delete(targetMember);

        auditLogService.logAction(tenantId, actorEmail, "REMOVE_MEMBER",
                "Removed userId=" + targetUserId);
    }

    /* ================= HELPERS ================= */

    Optional<TenantMember> getMemberEntity(Long tenantId, Long userId) {
        return memberRepo.findByTenant_IdAndUserId(tenantId, userId);
    }

    TenantMember getMemberEntityRequired(Long tenantId, Long userId) {
        return getMemberEntity(tenantId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
    }

    /**
     * Kiểm tra ACTOR (người thực hiện request) có quyền quản lý members không.
     * Phải check role của actor, không phải target.
     */
    private boolean canManageMembers(Long tenantId, String actorEmail) {
        try {
            Long actorUserId = authRepository.findByEmail(actorEmail)
                    .map(User::getId)
                    .orElse(null);
            if (actorUserId == null) return false;

            Optional<TenantMember> actorMembership = memberRepo
                    .findByTenantIdAndUserIdAndStatus(tenantId, actorUserId, MembershipStatus.ACTIVE);
            if (actorMembership.isEmpty()) return false;

            TenantMember actor = actorMembership.get();
            // Application-level join: fetch user by userId
            User actorUser = userRepository.findById(actor.getUserId())
                    .orElse(null);

            // System ADMIN luôn có quyền
            if (actorUser != null && actorUser.getSystemRole() == com.chatbot.core.identity.model.SystemRole.ADMIN) {
                return true;
            }
            // OWNER và ADMIN của tenant có quyền quản lý member
            return actor.getRole() == TenantRole.OWNER || actor.getRole() == TenantRole.ADMIN;

        } catch (Exception e) {
            log.error("Error checking manage-member permission for actor={}: {}", actorEmail, e.getMessage());
            return false;
        }
    }



    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new InsufficientPermissionException("User chưa được xác thực");
        }
        return auth.getName();
    }

    private MemberResponse toResponse(TenantMember m) {
        // Application-level join: fetch user by userId
        User user = userRepository.findById(m.getUserId())
                .orElse(null);
        
        String name = null;
        String avatar = null;
        if (user != null && user.getProfile() != null) {
            name = user.getProfile().getFullName();
            avatar = user.getProfile().getAvatar();
        }
        
        return MemberResponse.builder()
                .id(m.getId())
                .userId(m.getUserId())
                .email(user != null ? user.getEmail() : null)
                .name(name)
                .avatar(avatar)
                .role(m.getRole())
                .status(m.getStatus())
                .joinedAt(m.getJoinedAt() != null ? m.getJoinedAt() : m.getCreatedAt())
                .build();
    }

    public Long getTenantIdByKey(String tenantKey) {
        return tenantRepo.findByTenantKey(tenantKey)
                .map(Tenant::getId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with key: " + tenantKey));
    }
}
