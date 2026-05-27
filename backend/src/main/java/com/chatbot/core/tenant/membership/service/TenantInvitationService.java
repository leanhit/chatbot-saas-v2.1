package com.chatbot.core.tenant.membership.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatbot.core.tenant.membership.dto.InviteMemberRequest;
import com.chatbot.core.tenant.membership.dto.InvitationResponse;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.tenant.membership.model.TenantInvitation;
import com.chatbot.core.tenant.membership.model.TenantMember;
import com.chatbot.core.tenant.membership.model.InvitationStatus;
import com.chatbot.core.tenant.membership.model.MembershipStatus;
import com.chatbot.core.tenant.membership.repository.TenantInvitationRepository;
import com.chatbot.core.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.core.tenant.service.TenantAuditLogService;
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
@Transactional
@Slf4j
public class TenantInvitationService {

    private final TenantInvitationRepository invitationRepo;
    private final TenantRepository tenantRepo;
    private final TenantMemberRepository memberRepo;
    private final UserRepository userRepo;
    private final TenantNotificationService notificationService;
    private final TenantAuditLogService auditLogService;

    /**
     * Admin thực hiện mời user vào tenant.
     */
    @Transactional
    public void inviteMember(Long tenantId, InviteMemberRequest request, User admin) {
        Tenant tenant = tenantRepo.findById(tenantId)
            .orElseThrow(() -> new RuntimeException("Tenant không tồn tại"));

        // Dùng message chung để tránh user enumeration vulnerability
        User userToBeInvited = userRepo.findByEmail(request.getEmail().toLowerCase())
            .orElseThrow(() -> new IllegalStateException(
                "Không tìm thấy tài khoản với email này. Người dùng cần tự đăng ký trước khi được mời."));

        if (memberRepo.existsByTenantIdAndUserId(tenantId, userToBeInvited.getId())) {
            throw new IllegalStateException("Người dùng đã là thành viên của tổ chức này.");
        }

        if (invitationRepo.existsByTenantIdAndEmailAndStatus(tenantId, request.getEmail(), InvitationStatus.PENDING)) {
            throw new IllegalStateException("Đã có lời mời đang chờ xác nhận cho email này.");
        }

        TenantInvitation invitation = TenantInvitation.builder()
            .tenant(tenant)
            .email(request.getEmail().toLowerCase())
            .role(request.getRole())
            .token(UUID.randomUUID().toString())
            .status(InvitationStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusDays(7))
            .invitedBy(admin)
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
    @Transactional(readOnly = true)
    public List<InvitationResponse> listInvitations(Long tenantId) {
        return invitationRepo.findByTenantId(tenantId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách lời mời đang chờ xử lý của user (chỉ hiển thị chưa hết hạn).
     */
    @Transactional(readOnly = true)
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
    @Transactional
    public void acceptInvitation(String token, User user) {
        TenantInvitation invitation = invitationRepo.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Lời mời không hợp lệ hoặc đã bị thu hồi."));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("Lời mời này không còn ở trạng thái chờ.");
        }

        if (invitation.getExpiresAt() != null && invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Lời mời này đã hết hạn.");
        }

        if (!invitation.getEmail().equalsIgnoreCase(user.getEmail())) {
            throw new RuntimeException("Bạn không có quyền chấp nhận lời mời này.");
        }

        memberRepo.save(TenantMember.builder()
            .tenant(invitation.getTenant())
            .user(user)
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
    @Transactional
    public void rejectInvitation(String token, User user) {
        TenantInvitation invitation = invitationRepo.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Lời mời không tồn tại."));

        if (!invitation.getEmail().equalsIgnoreCase(user.getEmail())) {
            throw new RuntimeException("Bạn không có quyền từ chối lời mời này.");
        }

        invitation.setStatus(InvitationStatus.REJECTED);
        invitationRepo.save(invitation);

        auditLogService.logAction(invitation.getTenant().getId(), user.getEmail(), "REJECT_INVITATION",
            "Rejected invitation for tenant " + invitation.getTenant().getName());
    }

    /**
     * Admin thu hồi lời mời.
     */
    @Transactional
    public void revokeInvitation(Long tenantId, Long invitationId) {
        TenantInvitation invitation = invitationRepo.findByIdAndTenantId(invitationId, tenantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lời mời trong tổ chức này."));

        invitation.setStatus(InvitationStatus.REVOKED);
        invitationRepo.save(invitation);

        // Lấy email actor từ SecurityContext nếu có
        String actorEmail = getCurrentUserEmailSafe();
        auditLogService.logAction(tenantId, actorEmail, "REVOKE_INVITATION",
            "Revoked invitation id=" + invitationId + " for " + invitation.getEmail());
    }

    /* ================= HELPERS ================= */

    private InvitationResponse convertToResponse(TenantInvitation invitation) {
        return InvitationResponse.builder()
                .id(invitation.getId())
                .name(invitation.getTenant() != null ? invitation.getTenant().getName() : null)
                .email(invitation.getEmail())
                .role(invitation.getRole())
                .status(invitation.getStatus())
                .expiresAt(invitation.getExpiresAt())
                .invitedByName(invitation.getInvitedBy() != null &&
                    invitation.getInvitedBy().getProfile() != null
                    ? invitation.getInvitedBy().getProfile().getFullName()
                    : (invitation.getInvitedBy() != null ? invitation.getInvitedBy().getEmail() : null))
                .token(invitation.getToken())
                .build();
    }

    private String getCurrentUserEmailSafe() {
        try {
            org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) return auth.getName();
        } catch (Exception ignored) {}
        return "system";
    }
}