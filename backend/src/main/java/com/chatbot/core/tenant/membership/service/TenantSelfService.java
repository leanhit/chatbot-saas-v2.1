package com.chatbot.core.tenant.membership.service;

import com.chatbot.core.user.model.User;
import com.chatbot.core.tenant.membership.dto.*;
import com.chatbot.core.tenant.membership.model.*;
import com.chatbot.core.tenant.membership.repository.TenantJoinRequestRepository;
import com.chatbot.core.tenant.membership.repository.TenantMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantSelfService {

    private final TenantMemberRepository memberRepo;
    private final TenantJoinRequestRepository joinRequestRepo;

    /* ================= MY PENDING ================= */

    public List<TenantPendingResponse> getMyPending(User user) {
        return joinRequestRepo.findByUser_IdAndStatus(user.getId(), MembershipStatus.PENDING)
                .stream()
                .map(request -> TenantPendingResponse.builder()
                        .id(request.getTenant().getId())
                        .name(request.getTenant().getName())
                        .status(request.getTenant().getStatus())
                        .visibility(request.getTenant().getVisibility())
                        .requestedAt(request.getCreatedAt())
                        .build())
                .toList();
    }

    /* ================= LEAVE ================= */

    @Transactional
    public void leaveTenant(Long tenantId, User user) {
        TenantMember member = memberRepo.findByTenant_IdAndUser_Id(tenantId, user.getId())
                .orElseThrow(() -> new IllegalStateException("Not a member"));

        if (member.getRole() == TenantRole.OWNER) {
            throw new IllegalStateException("OWNER must transfer ownership first");
        }

        memberRepo.delete(member);
    }
}
