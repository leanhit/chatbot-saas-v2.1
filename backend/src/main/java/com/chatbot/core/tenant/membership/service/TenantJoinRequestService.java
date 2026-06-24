package com.chatbot.core.tenant.membership.service;

import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.UserRepository;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.tenant.membership.dto.*;
import com.chatbot.core.tenant.membership.model.*;
import com.chatbot.core.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.core.tenant.membership.repository.TenantJoinRequestRepository;
import com.chatbot.core.tenant.service.TenantAuditLogService;
import com.chatbot.core.tenant.exception.BusinessLogicException;
import com.chatbot.core.tenant.exception.TenantNotFoundException;
import com.chatbot.core.tenant.exception.InsufficientPermissionException;
import com.chatbot.core.tenant.service.TenantPermissionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
@Slf4j
public class TenantJoinRequestService {

    private final TenantMemberRepository memberRepo;
    private final TenantJoinRequestRepository joinRequestRepo;
    private final TenantRepository tenantRepo;
    private final TenantNotificationService notificationService;
    private final TenantAuditLogService auditLogService;
    private final UserRepository userRepo; // Added for application-level join
    private final TenantPermissionValidator permissionValidator;

    /* ================= REQUEST ================= */

    @Transactional(transactionManager = "tenantTransactionManager")
    public void requestToJoin(Long tenantId, User user) {
        if (memberRepo.existsByTenant_IdAndUserIdAndStatus(
                tenantId, user.getId(), MembershipStatus.ACTIVE)) {
            throw new BusinessLogicException(com.chatbot.shared.exceptions.ErrorCode.ALREADY_MEMBER, "You are already a member of this tenant");
        }

        if (joinRequestRepo.existsByTenant_IdAndUserIdAndStatus(
                tenantId, user.getId(), MembershipStatus.PENDING)) {
            throw new BusinessLogicException(com.chatbot.shared.exceptions.ErrorCode.JOIN_REQUEST_ALREADY_SENT, "You have already sent a join request for this tenant");
        }

        Tenant tenant = tenantRepo.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Không tìm thấy tenant"));

        joinRequestRepo.save(TenantJoinRequest.builder()
                .tenant(tenant)
                .userId(user.getId()) // Application-level join: store userId instead of User object
                .status(MembershipStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build());

        notificationService.sendJoinRequestNotification(
            tenantId,
            tenant.getName(),
            user.getEmail(),
            user.getEmail()
        );

        auditLogService.logAction(tenantId, user.getEmail(), "REQUEST_JOIN",
            "User requested to join tenant");
    }

    /* ================= LIST ================= */

    public List<MemberResponse> getPendingRequests(Long tenantId) {
        String currentUserEmail = permissionValidator.getCurrentUserEmail();
        if (!permissionValidator.isAdmin(currentUserEmail) && !permissionValidator.isTenantAdmin(tenantId, currentUserEmail)) {
            throw new InsufficientPermissionException(com.chatbot.shared.exceptions.ErrorCode.CANNOT_VIEW_JOIN_REQUESTS, "Only Admin or Tenant Owner can view join request list");
        }

        return joinRequestRepo.findByTenant_IdAndStatus(tenantId, MembershipStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /* ================= UPDATE ================= */

    @Transactional(transactionManager = "tenantTransactionManager")
    public void updateStatus(Long tenantId, Long requestId, MembershipStatus status) {
        String actorEmail = permissionValidator.getCurrentUserEmail();
        if (!permissionValidator.isAdmin(actorEmail) && !permissionValidator.isTenantAdmin(tenantId, actorEmail)) {
            throw new InsufficientPermissionException(com.chatbot.shared.exceptions.ErrorCode.CANNOT_APPROVE_JOIN_REQUESTS, "Only Admin or Tenant Owner can approve join requests");
        }

        TenantJoinRequest request = getPendingRequest(tenantId, requestId);

        if (status == MembershipStatus.ACTIVE || status == MembershipStatus.APPROVED) {
            // Application-level join: fetch user by userId
            User user = userRepo.findById(request.getUserId()).orElse(null);
            
            if (user != null && !memberRepo.existsByTenant_IdAndUserIdAndStatus(
                    tenantId, request.getUserId(), MembershipStatus.ACTIVE)) {
                memberRepo.save(TenantMember.builder()
                        .tenant(request.getTenant())
                        .userId(request.getUserId()) // Application-level join: store userId instead of User object
                        .role(TenantRole.MEMBER)
                        .status(MembershipStatus.ACTIVE)
                        .joinedAt(LocalDateTime.now())
                        .build());

                notificationService.sendJoinRequestApprovedNotification(
                    request.getTenant().getId(),
                    request.getTenant().getName(),
                    user.getEmail()
                );

                auditLogService.logAction(tenantId, actorEmail, "APPROVE_JOIN_REQUEST",
                    "Approved join request from " + user.getEmail());
            }
            joinRequestRepo.delete(request);

        } else if (status == MembershipStatus.REJECTED) {
            // Application-level join: fetch user by userId
            User user = userRepo.findById(request.getUserId()).orElse(null);
            String userEmail = user != null ? user.getEmail() : "unknown";
            
            joinRequestRepo.delete(request);
            auditLogService.logAction(tenantId, actorEmail, "REJECT_JOIN_REQUEST",
                "Rejected join request from " + userEmail);
        } else {
            throw new BusinessLogicException(com.chatbot.shared.exceptions.ErrorCode.INVALID_STATUS_TRANSITION, "Invalid status: " + status);
        }
    }

    /**
     * User tự hủy yêu cầu tham gia của mình.
     */
    @Transactional(transactionManager = "tenantTransactionManager")
    public void cancelUserRequest(Long requestId, User user) {
        TenantJoinRequest request = joinRequestRepo.findById(requestId)
                .orElseThrow(() -> new BusinessLogicException(com.chatbot.shared.exceptions.ErrorCode.JOIN_REQUEST_NOT_FOUND, "Join request not found"));

        if (!request.getUserId().equals(user.getId())) {
            throw new InsufficientPermissionException(com.chatbot.shared.exceptions.ErrorCode.CANNOT_CANCEL_OTHERS_REQUEST, "You cannot cancel other people's requests");
        }

        if (request.getStatus() != MembershipStatus.PENDING) {
            throw new BusinessLogicException(com.chatbot.shared.exceptions.ErrorCode.CANNOT_CANCEL_NON_PENDING, "Can only cancel pending requests");
        }

        Long tenantId = request.getTenant().getId();
        joinRequestRepo.delete(request);

        auditLogService.logAction(tenantId, user.getEmail(), "CANCEL_JOIN_REQUEST",
            "User cancelled their own join request");
    }

    /* ================= HELPERS ================= */

    private TenantJoinRequest getPendingRequest(Long tenantId, Long requestId) {
        TenantJoinRequest request = joinRequestRepo.findById(requestId)
                .orElseThrow(() -> new BusinessLogicException(com.chatbot.shared.exceptions.ErrorCode.JOIN_REQUEST_NOT_FOUND, "Join request not found"));

        if (!request.getTenant().getId().equals(tenantId)
                || request.getStatus() != MembershipStatus.PENDING) {
            throw new BusinessLogicException(com.chatbot.shared.exceptions.ErrorCode.INVALID_JOIN_REQUEST, "Invalid join request");
        }
        return request;
    }

    private MemberResponse toResponse(TenantJoinRequest request) {
        // Application-level join: fetch user by userId
        User user = userRepo.findById(request.getUserId()).orElse(null);
        
        String name = null;
        String avatar = null;
        if (user != null && user.getProfile() != null) {
            name = user.getProfile().getFullName();
            avatar = user.getProfile().getAvatar();
        }
        
        return MemberResponse.builder()
                .id(request.getId()) // ID của yêu cầu để duyệt/hủy
                .userId(request.getUserId()) // ID của User gửi yêu cầu
                .email(user != null ? user.getEmail() : null)
                .name(name)
                .avatar(avatar)
                .status(request.getStatus())
                .requestedAt(request.getCreatedAt())
                .build();
    }

}
