package com.chatbot.core.tenant.membership.service;

import com.chatbot.core.identity.model.Auth;
import com.chatbot.core.tenant.core.model.Tenant;
import com.chatbot.core.tenant.core.repository.TenantRepository;
import com.chatbot.core.tenant.membership.dto.*;
import com.chatbot.core.tenant.membership.model.*;
import com.chatbot.core.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.core.tenant.membership.repository.TenantJoinRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantJoinRequestService {

    private final TenantMemberRepository memberRepo;
    private final TenantJoinRequestRepository joinRequestRepo;
    private final TenantRepository tenantRepo;

    /* ================= REQUEST ================= */

    @Transactional
    public void requestToJoin(Long tenantId, Auth user) {
        // Check if user already has an active membership
        if (memberRepo.existsByTenant_IdAndUser_IdAndStatus(
                tenantId, user.getId(), MembershipStatus.ACTIVE)) {
            throw new IllegalStateException("Bạn đã là thành viên của tenant này");
        }

        // Check for existing pending join request
        if (joinRequestRepo.existsByTenant_IdAndUser_IdAndStatus(
                tenantId, user.getId(), MembershipStatus.PENDING)) {
            throw new IllegalStateException("Bạn đã gửi yêu cầu tham gia tenant này");
        }

        Tenant tenant = tenantRepo.findById(tenantId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tenant"));

        // Create new join request
        joinRequestRepo.save(TenantJoinRequest.builder()
                .tenant(tenant)
                .user(user)
                .status(MembershipStatus.PENDING)
                .build());
    }

    /* ================= LIST ================= */

    public List<MemberResponse> getPendingRequests(Long tenantId) {
        return joinRequestRepo.findByTenant_IdAndStatus(tenantId, MembershipStatus.PENDING)
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
    public void updateStatus(Long tenantId, Long requestId, MembershipStatus status) {
        TenantJoinRequest request = getPendingRequest(tenantId, requestId);
        
        if (status == MembershipStatus.ACTIVE) {
            // Check if user already has ACTIVE membership
            if (!memberRepo.existsByTenant_IdAndUser_IdAndStatus(
                    tenantId, request.getUser().getId(), MembershipStatus.ACTIVE)) {
                // Create new member
                memberRepo.save(TenantMember.builder()
                        .tenant(request.getTenant())
                        .user(request.getUser())
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
}
