package com.chatbot.core.tenant.membership.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatbot.core.tenant.membership.dto.InviteMemberRequest;
import com.chatbot.core.tenant.membership.dto.InvitationResponse;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.tenant.exception.TenantNotFoundException;
import com.chatbot.core.tenant.exception.BusinessLogicException;
import com.chatbot.core.tenant.exception.InsufficientPermissionException;
import com.chatbot.core.tenant.membership.model.TenantInvitation;
import com.chatbot.core.tenant.membership.model.TenantMember;
import com.chatbot.core.tenant.membership.model.InvitationStatus;
import com.chatbot.core.tenant.membership.model.MembershipStatus;
import com.chatbot.core.tenant.membership.repository.TenantInvitationRepository;
import com.chatbot.core.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.core.tenant.service.TenantAuditLogService;
import com.chatbot.core.tenant.service.TenantPermissionValidator;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(transactionManager = "tenantTransactionManager")
@Slf4j
public class TenantInvitationService {

    private final TenantInvitationRepository invitationRepo;
    private final TenantRepository tenantRepo;
    private final TenantMemberRepository memberRepo;
    private final UserRepository userRepo;
    private final TenantNotificationService notificationService;
    private final TenantAuditLogService auditLogService;
    private final TenantPermissionValidator permissionValidator;

    /**
     * Admin thực hiện mời user vào tenant.
     */
    @Transactional(transactionManager = "tenantTransactionManager")
    public void inviteMember(Long tenantId, InviteMemberRequest request, User admin) {
        if (!permissionValidator.isAdmin(admin.getEmail()) && !permissionValidator.isTenantAdmin(tenantId, admin.getEmail())) {
            throw new InsufficientPermissionException(com.chatbot.shared.exceptions.ErrorCode.INVITATION_PERMISSION_DENIED, "Only Admin or Tenant Owner can invite members");
        }

        if (!permissionValidator.canAssignRole(tenantId, admin.getEmail(), request.getRole())) {
            throw new InsufficientPermissionException(com.chatbot.shared.exceptions.ErrorCode.CANNOT_ASSIGN_ROLE, "You do not have permission to invite others with role " + request.getRole());
        }

        Tenant tenant = tenantRepo.findById(tenantId)
            .orElseThrow(() -> new TenantNotFoundException("Tenant not found"));

        // Use generic message to avoid user enumeration vulnerability
        User userToBeInvited = userRepo.findByEmail(request.getEmail().toLowerCase())
            .orElseThrow(() -> new BusinessLogicException(
                com.chatbot.shared.exceptions.ErrorCode.USER_NOT_FOUND_FOR_INVITATION, "User not found. Please register before being invited."));

        if (memberRepo.existsByTenantIdAndUserId(tenantId, userToBeInvited.getId())) {
            throw new BusinessLogicException(com.chatbot.shared.exceptions.ErrorCode.ALREADY_MEMBER, "User is already a member of this organization");
        }

        // Check for pending invitations that are not expired
        List<TenantInvitation> pendingInvitations = invitationRepo.findByTenantIdAndEmailAndStatus(
            tenantId, request.getEmail().toLowerCase(), InvitationStatus.PENDING);
        boolean hasValidPendingInvitation = pendingInvitations.stream()
            .anyMatch(inv -> inv.getExpiresAt() == null || inv.getExpiresAt().isAfter(LocalDateTime.now()));
        
        if (hasValidPendingInvitation) {
            throw new BusinessLogicException(com.chatbot.shared.exceptions.ErrorCode.INVITATION_ALREADY_PENDING, "There is already a pending invitation for this email");
        }

        TenantInvitation invitation = TenantInvitation.builder()
            .tenant(tenant)
            .email(request.getEmail().toLowerCase())
            .role(request.getRole())
            .token(UUID.randomUUID().toString())
            .status(InvitationStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusDays(7))
            .invitedByUserId(admin.getId()) // Application-level join: store userId instead of User object
            .build();

        invitation = invitationRepo.save(invitation);

        notificationService.sendInvitationNotification(
            tenantId,
            tenant.getName(),
            request.getEmail(),
            request.getRole().toString(),
            invitation.getToken(),
            admin.getEmail()
        );

        auditLogService.logAction(tenantId, admin.getEmail(), "INVITE_MEMBER",
            "Invited " + request.getEmail() + " as " + request.getRole());
    }

    /**
     * Lấy danh sách lời mời của một Tenant.
     */
    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public List<InvitationResponse> listInvitations(Long tenantId) {
        String currentUserEmail = permissionValidator.getCurrentUserEmail();
        if (!permissionValidator.isAdmin(currentUserEmail) && !permissionValidator.isTenantAdmin(tenantId, currentUserEmail)) {
            throw new InsufficientPermissionException(com.chatbot.shared.exceptions.ErrorCode.INVITATION_PERMISSION_DENIED, "Only Admin or Tenant Owner can view invitation list");
        }

        return invitationRepo.findByTenantId(tenantId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách lời mời đang chờ xử lý của user (chỉ hiển thị chưa hết hạn).
     */
    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public List<InvitationResponse> getMyPendingInvitations(User user) {
        return invitationRepo.findByEmailAndStatus(user.getEmail(), InvitationStatus.PENDING)
                .stream()
                .filter(inv -> inv.getExpiresAt() == null || inv.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * User chấp nhận lời mời qua token.
     */
    @Transactional(transactionManager = "tenantTransactionManager")
    public void acceptInvitation(String token, User user) {
        TenantInvitation invitation = invitationRepo.findByToken(token)
            .orElseThrow(() -> new BusinessLogicException(com.chatbot.shared.exceptions.ErrorCode.INVITATION_INVALID, "Invitation is invalid or has been revoked"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BusinessLogicException(com.chatbot.shared.exceptions.ErrorCode.INVITATION_NOT_PENDING, "This invitation is no longer pending");
        }

        if (invitation.getExpiresAt() != null && invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessLogicException(com.chatbot.shared.exceptions.ErrorCode.INVITATION_EXPIRED, "This invitation has expired");
        }

        if (!invitation.getEmail().equalsIgnoreCase(user.getEmail())) {
            throw new InsufficientPermissionException(com.chatbot.shared.exceptions.ErrorCode.CANNOT_ACCEPT_INVITATION, "You do not have permission to accept this invitation");
        }

        memberRepo.save(TenantMember.builder()
            .tenant(invitation.getTenant())
            .userId(user.getId()) // Application-level join: store userId instead of User object
            .role(invitation.getRole())
            .status(MembershipStatus.ACTIVE)
            .joinedAt(LocalDateTime.now())
            .build());

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepo.save(invitation);

        Long tenantId = invitation.getTenant().getId();

        notificationService.sendInvitationAcceptedNotification(
            tenantId,
            invitation.getTenant().getName(),
            user.getEmail(),
            invitation.getRole().toString()
        );

        auditLogService.logAction(tenantId, user.getEmail(), "ACCEPT_INVITATION",
            "Accepted invitation as " + invitation.getRole());
    }

    /**
     * User từ chối lời mời.
     */
    @Transactional(transactionManager = "tenantTransactionManager")
    public void rejectInvitation(String token, User user) {
        TenantInvitation invitation = invitationRepo.findByToken(token)
            .orElseThrow(() -> new BusinessLogicException(com.chatbot.shared.exceptions.ErrorCode.INVITATION_NOT_FOUND, "Invitation not found"));

        if (!invitation.getEmail().equalsIgnoreCase(user.getEmail())) {
            throw new InsufficientPermissionException(com.chatbot.shared.exceptions.ErrorCode.CANNOT_REJECT_INVITATION, "You do not have permission to reject this invitation");
        }

        invitation.setStatus(InvitationStatus.REJECTED);
        invitationRepo.save(invitation);

        auditLogService.logAction(invitation.getTenant().getId(), user.getEmail(), "REJECT_INVITATION",
            "Rejected invitation for tenant " + invitation.getTenant().getName());
    }

    /**
     * Admin thu hồi lời mời.
     */
    @Transactional(transactionManager = "tenantTransactionManager")
    public void revokeInvitation(Long tenantId, Long invitationId) {
        String currentUserEmail = permissionValidator.getCurrentUserEmail();
        if (!permissionValidator.isAdmin(currentUserEmail) && !permissionValidator.isTenantAdmin(tenantId, currentUserEmail)) {
            throw new InsufficientPermissionException(com.chatbot.shared.exceptions.ErrorCode.CANNOT_REVOKE_INVITATION, "Only Admin or Tenant Owner can revoke invitations");
        }

        TenantInvitation invitation = invitationRepo.findByIdAndTenantId(invitationId, tenantId)
                .orElseThrow(() -> new BusinessLogicException(com.chatbot.shared.exceptions.ErrorCode.INVITATION_NOT_FOUND_IN_TENANT, "Invitation not found in this organization"));

        invitation.setStatus(InvitationStatus.REVOKED);
        invitationRepo.save(invitation);

        auditLogService.logAction(tenantId, currentUserEmail, "REVOKE_INVITATION",
            "Revoked invitation id=" + invitationId + " for " + invitation.getEmail());
    }

    /* ================= HELPERS ================= */

    private InvitationResponse convertToResponse(TenantInvitation invitation) {
        // Application-level join: fetch invitedBy user when needed
        String invitedByName = null;
        if (invitation.getInvitedByUserId() != null) {
            User invitedByUser = userRepo.findById(invitation.getInvitedByUserId()).orElse(null);
            if (invitedByUser != null) {
                invitedByName = invitedByUser.getProfile() != null 
                    ? invitedByUser.getProfile().getFullName()
                    : invitedByUser.getEmail();
            }
        }
        
        return InvitationResponse.builder()
                .id(invitation.getId())
                .name(invitation.getTenant() != null ? invitation.getTenant().getName() : null)
                .email(invitation.getEmail())
                .role(invitation.getRole())
                .status(invitation.getStatus())
                .expiresAt(invitation.getExpiresAt())
                .invitedByName(invitedByName)
                .token(invitation.getToken())
                .build();
    }

    private String getCurrentUserEmailSafe() {
        try {
            org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) return auth.getName();
        } catch (Exception e) {
            log.warn("⚠️ Failed to retrieve current user email: {}", e.getMessage());
        }
        return "system";
    }
}