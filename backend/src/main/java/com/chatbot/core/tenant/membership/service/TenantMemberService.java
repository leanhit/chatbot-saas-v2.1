package com.chatbot.core.tenant.membership.service;

import com.chatbot.core.user.model.User;
import com.chatbot.core.tenant.membership.dto.*;
import com.chatbot.core.tenant.membership.model.*;
import com.chatbot.core.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.tenant.infra.TenantContext;
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
    private final TenantRepository tenantRepo;

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
    public MemberResponse getMyMember(Long tenantId, User user) {
        return getMember(tenantId, user.getId());
    }

    /* ================= UPDATE ================= */

    @Transactional
    public void updateRole(Long tenantId, Long userId, TenantRole newRole) {
        // Check permissions
        if (!canManageMembers(tenantId, userId)) {
            throw new IllegalStateException("Insufficient privileges to manage member roles");
        }
        
        TenantMember member = getMemberEntityRequired(tenantId, userId);

        if (member.getRole() == TenantRole.OWNER) {
            throw new IllegalStateException("Cannot change OWNER role");
        }

        // Check if user is trying to assign higher role than their own
        if (!canAssignRole(tenantId, userId, newRole)) {
            throw new IllegalStateException("Cannot assign role higher than your own privileges");
        }

        member.setRole(newRole);
    }

    /* ================= DELETE ================= */

    @Transactional
    public void removeMember(Long tenantId, Long userId) {
        // Check permissions
        if (!canManageMembers(tenantId, userId)) {
            throw new IllegalStateException("Insufficient privileges to remove members");
        }
        
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

    private boolean canManageMembers(Long tenantId, Long userId) {
        try {
            // System ADMIN can manage any tenant
            Optional<TenantMember> memberOpt = getMemberEntity(tenantId, userId);
            if (memberOpt.isEmpty()) return false;
            
            TenantMember member = memberOpt.get();
            User user = member.getUser();
            
            if (user.getSystemRole() == com.chatbot.core.identity.model.SystemRole.ADMIN) {
                return true;
            }
            
            // OWNER and ADMIN can manage members
            return member.getRole() == TenantRole.OWNER || member.getRole() == TenantRole.ADMIN;
            
        } catch (Exception e) {
            return false;
        }
    }

    private boolean canAssignRole(Long tenantId, Long userId, TenantRole targetRole) {
        try {
            Optional<TenantMember> memberOpt = getMemberEntity(tenantId, userId);
            if (memberOpt.isEmpty()) return false;
            
            TenantMember member = memberOpt.get();
            User user = member.getUser();
            
            // System ADMIN can assign any role
            if (user.getSystemRole() == com.chatbot.core.identity.model.SystemRole.ADMIN) {
                return true;
            }
            
            TenantRole currentRole = member.getRole();
            
            // OWNER can assign any role except another OWNER
            if (currentRole == TenantRole.OWNER) {
                return targetRole != TenantRole.OWNER;
            }
            
            // ADMIN can assign EDITOR, MEMBER, but not OWNER or ADMIN
            if (currentRole == TenantRole.ADMIN) {
                return targetRole == TenantRole.EDITOR || targetRole == TenantRole.MEMBER;
            }
            
            // EDITOR and MEMBER cannot assign roles
            return false;
            
        } catch (Exception e) {
            return false;
        }
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
