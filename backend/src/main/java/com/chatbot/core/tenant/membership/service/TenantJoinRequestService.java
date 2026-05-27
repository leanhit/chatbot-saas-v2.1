package com.chatbot.core.tenant.membership.service;

import com.chatbot.core.user.model.User;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.tenant.membership.dto.*;
import com.chatbot.core.tenant.membership.model.*;
import com.chatbot.core.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.core.tenant.membership.repository.TenantJoinRequestRepository;
import com.chatbot.core.tenant.service.TenantAuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TenantJoinRequestService {

    private final TenantMemberRepository memberRepo;
    private final TenantJoinRequestRepository joinRequestRepo;
    private final TenantRepository tenantRepo;
    private final TenantNotificationService notificationService;
    private final TenantAuditLogService auditLogService;

    /* ================= REQUEST ================= */

    @Transactional
    public void requestToJoin(Long tenantId, User user) {
        if (memberRepo.existsByTenant_IdAndUser_IdAndStatus(
                tenantId, user.getId(), MembershipStatus.ACTIVE)) {
            throw new IllegalStateException("Bạn đã là thành viên của tenant này");
        }

        if (joinRequestRepo.existsByTenant_IdAndUser_IdAndStatus(
                tenantId, user.getId(), MembershipStatus.PENDING)) {
            throw new IllegalStateException("Bạn đã gửi yêu cầu tham gia tenant này rồi");
        }

        Tenant tenant = tenantRepo.findById(tenantId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tenant"));

        joinRequestRepo.save(TenantJoinRequest.builder()
                .tenant(tenant)
                .user(user)
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
        return joinRequestRepo.findByTenant_IdAndStatus(tenantId, MembershipStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /* ================= UPDATE ================= */

    @Transactional
    public void updateStatus(Long tenantId, Long requestId, MembershipStatus status) {
        TenantJoinRequest request = getPendingRequest(tenantId, requestId);

        String actorEmail = getCurrentUserEmailSafe();

        if (status == MembershipStatus.ACTIVE) {
            if (!memberRepo.existsByTenant_IdAndUser_IdAndStatus(
                    tenantId, request.getUser().getId(), MembershipStatus.ACTIVE)) {
                memberRepo.save(TenantMember.builder()
                        .tenant(request.getTenant())
                        .user(request.getUser())
                        .role(TenantRole.MEMBER)
                        .status(MembershipStatus.ACTIVE)
                        .joinedAt(LocalDateTime.now())
                        .build());

                notificationService.sendJoinRequestApprovedNotification(
                    request.getTenant().getId(),
                    request.getTenant().getName(),
                    request.getUser().getEmail()
                );

                auditLogService.logAction(tenantId, actorEmail, "APPROVE_JOIN_REQUEST",
                    "Approved join request from " + request.getUser().getEmail());
            }
            joinRequestRepo.delete(request);

        } else if (status == MembershipStatus.REJECTED) {
            joinRequestRepo.delete(request);
            auditLogService.logAction(tenantId, actorEmail, "REJECT_JOIN_REQUEST",
                "Rejected join request from " + request.getUser().getEmail());
        } else {
            throw new IllegalStateException("Trạng thái không hợp lệ: " + status);
        }
    }

    /**
     * User tự hủy yêu cầu tham gia của mình.
     */
    @Transactional
    public void cancelUserRequest(Long requestId, User user) {
        TenantJoinRequest request = joinRequestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy yêu cầu"));

        if (!request.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Bạn không thể hủy yêu cầu của người khác");
        }

        if (request.getStatus() != MembershipStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể hủy yêu cầu đang chờ xử lý");
        }

        Long tenantId = request.getTenant().getId();
        joinRequestRepo.delete(request);

        auditLogService.logAction(tenantId, user.getEmail(), "CANCEL_JOIN_REQUEST",
            "User cancelled their own join request");
    }

    /* ================= HELPERS ================= */

    private TenantJoinRequest getPendingRequest(Long tenantId, Long requestId) {
        TenantJoinRequest request = joinRequestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy yêu cầu"));

        if (!request.getTenant().getId().equals(tenantId)
                || request.getStatus() != MembershipStatus.PENDING) {
            throw new IllegalStateException("Yêu cầu tham gia không hợp lệ");
        }
        return request;
    }

    private MemberResponse toResponse(TenantJoinRequest request) {
        return MemberResponse.builder()
                .id(request.getUser().getId())
                .email(request.getUser().getEmail())
                .status(request.getStatus())
                .requestedAt(request.getCreatedAt())
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
