package com.chatbot.modules.tenant.membership.service;

import com.chatbot.modules.tenant.membership.repository.TenantInvitationRepository;
import com.chatbot.modules.tenant.core.repository.TenantRepository;
import com.chatbot.modules.identity.repository.UserRepository;
import com.chatbot.modules.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.modules.tenant.membership.dto.InvitationResponse;
import com.chatbot.modules.tenant.membership.dto.InviteMemberRequest;
import com.chatbot.modules.tenant.membership.model.TenantInvitation;
import com.chatbot.modules.tenant.membership.model.InvitationStatus;
import com.chatbot.modules.auth.model.Auth;
import com.chatbot.modules.identity.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Tenant invitation service for v0.1
 * Handles member invitations for tenants
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantInvitationService {
    
    private final TenantInvitationRepository invitationRepo;
    private final TenantRepository tenantRepo;
    private final UserRepository userRepository;
    private final TenantMemberRepository memberRepo;

    /**
     * Send invitation to a user
     */
    @Transactional
    public void inviteMember(UUID tenantId, InviteMemberRequest request, Auth admin) {
        // TODO: Validate admin has permission to invite members
        
        TenantInvitation invitation = TenantInvitation.builder()
                .tenant(tenantRepo.findById(tenantId).orElseThrow(() -> new IllegalArgumentException("Tenant not found")))
                .email(request.getEmail())
                .role(request.getRole())
                .invitedBy(admin.getId())
                .status(InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusDays(request.getExpiryDays()))
                .createdAt(LocalDateTime.now())
                .build();
        
        invitationRepo.save(invitation);
        log.info("Invitation sent: tenant={}, email={}, role={}", tenantId, request.getEmail(), request.getRole());
    }

    /**
     * List all invitations for a tenant
     */
    @Transactional(readOnly = true)
    public List<InvitationResponse> listInvitations(UUID tenantId) {
        // TODO: Validate user has permission to view invitations
        
        List<TenantInvitation> invitations = invitationRepo.findByTenantId(tenantId);
        
        return invitations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get user's pending invitations
     */
    @Transactional(readOnly = true)
    public List<InvitationResponse> getMyPendingInvitations(Auth user) {
        List<TenantInvitation> invitations = invitationRepo.findByEmailAndStatus(user.getEmail(), InvitationStatus.PENDING);
        
        return invitations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Accept invitation with token
     */
    @Transactional
    public void acceptInvitation(String token, Auth user) {
        TenantInvitation invitation = invitationRepo.findByTokenAndStatus(token, InvitationStatus.PENDING)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired invitation"));

        if (!invitation.getEmail().equals(user.getEmail())) {
            throw new IllegalArgumentException("Invitation email does not match user email");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invitation has expired");
        }

        // Update invitation status
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepo.save(invitation);

        // Add user as tenant member
        // TODO: Create tenant member entry
        log.info("Invitation accepted: token={}, user={}", token, user.getId());
    }

    /**
     * Reject invitation with token
     */
    @Transactional
    public void rejectInvitation(String token, Auth user) {
        TenantInvitation invitation = invitationRepo.findByTokenAndStatus(token, InvitationStatus.PENDING)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invitation"));

        if (!invitation.getEmail().equals(user.getEmail())) {
            throw new IllegalArgumentException("Invitation email does not match user email");
        }

        invitation.setStatus(InvitationStatus.REJECTED);
        invitationRepo.save(invitation);
        
        log.info("Invitation rejected: token={}, user={}", token, user.getId());
    }

    /**
     * Revoke an invitation
     */
    @Transactional
    public void revokeInvitation(UUID tenantId, Long invitationId) {
        // TODO: Validate user has permission to revoke invitations
        
        TenantInvitation invitation = invitationRepo.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

        if (!invitation.getTenant().getId().equals(tenantId)) {
            throw new IllegalArgumentException("Invitation does not belong to this tenant");
        }

        invitationRepo.delete(invitation);
        log.info("Invitation revoked: tenant={}, invitation={}", tenantId, invitationId);
    }

    private InvitationResponse convertToResponse(TenantInvitation invitation) {
        // TODO: Get invited by user details
        return InvitationResponse.builder()
                .id(invitation.getId())
                .tenantId(invitation.getTenant().getId())
                .email(invitation.getEmail())
                .role(invitation.getRole())
                .status(invitation.getStatus())
                .invitedAt(invitation.getCreatedAt())
                .expiresAt(invitation.getExpiresAt())
                .invitedBy(invitation.getInvitedBy())
                .build();
    }
}