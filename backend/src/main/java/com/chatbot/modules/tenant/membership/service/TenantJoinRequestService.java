package com.chatbot.modules.tenant.membership.service;

import com.chatbot.modules.tenant.core.model.Tenant;
import com.chatbot.modules.tenant.core.repository.TenantRepository;
import com.chatbot.modules.tenant.membership.dto.*;
import com.chatbot.modules.tenant.membership.model.*;
import com.chatbot.modules.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.modules.tenant.membership.repository.TenantJoinRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantJoinRequestService {

    private final TenantMemberRepository memberRepo;
    private final TenantJoinRequestRepository joinRequestRepo;
    private final TenantRepository tenantRepo;

    /* ================= REQUEST ================= */

    @Transactional
    public void requestToJoin(UUID tenantId, UUID userId, String userEmail) {
        // Check if user already has an active membership
        if (memberRepo.existsByTenantIdAndUserIdAndStatus(
                tenantId, userId, MembershipStatus.ACTIVE)) {
            throw new IllegalStateException("Bạn đã là thành viên của tenant này");
        }

        // Check for existing pending join request
        if (joinRequestRepo.existsByTenantIdAndUserIdAndStatus(
                tenantId, userId, MembershipStatus.PENDING)) {
            throw new IllegalStateException("Bạn đã gửi yêu cầu tham gia tenant này");
        }

        Tenant tenant = tenantRepo.findById(tenantId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tenant"));

        // Create new join request
        joinRequestRepo.save(TenantJoinRequest.builder()
                .tenant(tenant)
                .userId(userId)
                .userEmail(userEmail)
                .status(MembershipStatus.PENDING)
                .build());
    }

    /* ================= LIST ================= */

    public List<MemberResponse> getPendingRequests(UUID tenantId) {
        return joinRequestRepo.findByTenantIdAndStatus(tenantId, MembershipStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /* ================= UPDATE (SPEC) ================= */
    /**
     * PATCH /join-requests/{id}
     * { status: APPROVED | REJECTED }
     */
    @Transactional
    public void updateStatus(UUID tenantId, Long requestId, MembershipStatus status) {
        TenantJoinRequest request = getPendingRequest(tenantId, requestId);
        
        if (status == MembershipStatus.ACTIVE) {
            // Check if user already has ACTIVE membership
            if (!memberRepo.existsByTenantIdAndUserIdAndStatus(
                    tenantId, request.getUserId(), MembershipStatus.ACTIVE)) {
                // Create new member
                memberRepo.save(TenantMember.builder()
                        .tenant(request.getTenant())
                        .userId(request.getUserId())
                        .email(request.getUserEmail())
                        .role(TenantRole.MEMBER)
                        .status(MembershipStatus.ACTIVE)
                        .joinedAt(LocalDateTime.now())
                        .build());
            }
            
            // Delete the join request
            joinRequestRepo.delete(request);
            
        } else if (status == MembershipStatus.REJECTED) {
            // Just delete the request
            joinRequestRepo.delete(request);
        } else {
            throw new IllegalStateException("Trạng thái không hợp lệ");
        }
    }

    /* ================= HELPERS ================= */

    private TenantJoinRequest getPendingRequest(UUID tenantId, Long requestId) {
        TenantJoinRequest request = joinRequestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy yêu cầu tham gia"));

        if (!request.getTenant().getId().equals(tenantId)
                || request.getStatus() != MembershipStatus.PENDING) {
            throw new IllegalStateException("Yêu cầu tham gia không hợp lệ");
        }
        return request;
    }

    private MemberResponse toResponse(TenantJoinRequest request) {
        return MemberResponse.builder()
                .id(request.getUserId())
                .userId(request.getUserId())
                .email(request.getUserEmail())
                .status(request.getStatus())
                .requestedAt(request.getCreatedAt())
                .build();
    }
}
