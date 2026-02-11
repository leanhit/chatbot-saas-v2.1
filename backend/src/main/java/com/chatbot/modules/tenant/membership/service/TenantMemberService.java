package com.chatbot.modules.tenant.membership.service;

import com.chatbot.core.identity.model.Auth;
import com.chatbot.modules.tenant.membership.dto.*;
import com.chatbot.modules.tenant.membership.model.*;
import com.chatbot.modules.tenant.membership.repository.TenantMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantMemberService {

    private final TenantMemberRepository memberRepo;

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

    /** ✅ SPEC: GET /tenants/{tenantId}/members/me */
    public MemberResponse getMyMember(Long tenantId, Auth user) {
        return getMember(tenantId, user.getId());
    }

    /* ================= UPDATE ================= */

    @Transactional
    public void updateRole(Long tenantId, Long userId, TenantRole role) {
        TenantMember member = getMemberEntityRequired(tenantId, userId);

        if (member.getRole() == TenantRole.OWNER) {
            throw new IllegalStateException("Cannot change OWNER role");
        }

        member.setRole(role);
    }

    /* ================= DELETE ================= */

    @Transactional
    public void removeMember(Long tenantId, Long userId) {
        TenantMember member = getMemberEntityRequired(tenantId, userId);

        if (member.getRole() == TenantRole.OWNER) {
            throw new IllegalStateException("Cannot remove OWNER");
        }

        memberRepo.delete(member);
    }

    /* ================= HELPERS ================= */

    Optional<TenantMember> getMemberEntity(Long tenantId, Long userId) {
        return memberRepo.findByTenant_IdAndUser_Id(tenantId, userId);
    }

    TenantMember getMemberEntityRequired(Long tenantId, Long userId) {
        return getMemberEntity(tenantId, userId)
                .orElseThrow(() -> new IllegalStateException("Member not found"));
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
}
