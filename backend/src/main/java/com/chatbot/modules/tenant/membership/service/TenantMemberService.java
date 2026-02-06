package com.chatbot.modules.tenant.membership.service;

import com.chatbot.modules.tenant.membership.dto.MemberResponse;
import com.chatbot.modules.tenant.membership.model.TenantMember;
import com.chatbot.modules.tenant.membership.model.TenantRole;
import com.chatbot.modules.tenant.membership.repository.TenantMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Tenant member service for v0.1
 * Core member management functionality
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantMemberService {

    private final TenantMemberRepository tenantMemberRepository;

    /**
     * Get all members of a tenant
     * TODO: Validate user has access to tenant
     */
    @Transactional(readOnly = true)
    public List<MemberResponse> getTenantMembers(UUID tenantId) {
        // TODO: Validate current user has permission to view members
        
        List<TenantMember> members = tenantMemberRepository.findByTenantId(tenantId);
        
        return members.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update member role
     * Only OWNER can change roles
     * OWNER cannot be changed
     * TODO: Validate current user is OWNER
     */
    @Transactional
    public void updateMemberRole(UUID tenantId, UUID userId, TenantRole newRole) {
        // TODO: Validate current user is OWNER of this tenant
        
        TenantMember member = tenantMemberRepository
                .findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        if (member.getRole() == TenantRole.OWNER) {
            throw new IllegalStateException("Cannot change OWNER role");
        }

        if (newRole == TenantRole.OWNER) {
            // TODO: Validate this is the last OWNER transfer
            throw new IllegalStateException("Cannot assign OWNER role through this method");
        }

        member.setRole(newRole);
        tenantMemberRepository.save(member);
        
        log.info("Updated member role: tenant={}, user={}, role={}", tenantId, userId, newRole);
    }

    /**
     * Remove member from tenant
     * OWNER cannot remove themselves if they're the last OWNER
     * ADMIN can remove MEMBER only
     * TODO: Validate current user permissions
     */
    @Transactional
    public void removeMember(UUID tenantId, UUID userId) {
        // TODO: Validate current user permissions based on their role
        
        TenantMember member = tenantMemberRepository
                .findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        if (member.getRole() == TenantRole.OWNER) {
            // TODO: Check if this is the last OWNER
            throw new IllegalStateException("Cannot remove OWNER member");
        }

        tenantMemberRepository.delete(member);
        log.info("Removed member: tenant={}, user={}", tenantId, userId);
    }

    private MemberResponse mapToResponse(TenantMember member) {
        return MemberResponse.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .email(member.getEmail())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
