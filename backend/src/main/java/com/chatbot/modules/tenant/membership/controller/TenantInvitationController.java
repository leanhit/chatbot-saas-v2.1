package com.chatbot.modules.tenant.membership.controller;

import com.chatbot.modules.tenant.membership.service.TenantInvitationService;
import com.chatbot.modules.tenant.membership.dto.InviteMemberRequest;
import com.chatbot.modules.tenant.membership.dto.InvitationResponse;
import com.chatbot.modules.auth.model.Auth;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Tenant invitation controller for v0.1
 * Handles member invitations for tenants
 */
@RestController
@RequestMapping("/api/tenants/{tenantId}/invitations")
@RequiredArgsConstructor
public class TenantInvitationController {
    
    private final TenantInvitationService invitationService;
    
    /**
     * Send invitation to a user
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void inviteMember(
            @PathVariable UUID tenantId,
            @Valid @RequestBody InviteMemberRequest request,
            @AuthenticationPrincipal(expression = "auth") Auth currentUser) {
        invitationService.inviteMember(tenantId, request, currentUser);
    }
    
    /**
     * List all invitations for a tenant
     */
    @GetMapping
    public List<InvitationResponse> listInvitations(@PathVariable UUID tenantId) {
        return invitationService.listInvitations(tenantId);
    }
    
    /**
     * Accept invitation with token
     */
    @PostMapping("/{token}/accept")
    public void acceptInvitation(
            @PathVariable String token,
            @AuthenticationPrincipal(expression = "auth") Auth user) {
        invitationService.acceptInvitation(token, user);
    }
    
    /**
     * Reject invitation with token
     */
    @PostMapping("/{token}/reject")
    public void rejectInvitation(
            @PathVariable String token,
            @AuthenticationPrincipal(expression = "auth") Auth user) {
        invitationService.rejectInvitation(token, user);
    }
    
    /**
     * Revoke an invitation
     */
    @DeleteMapping("/{invitationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeInvitation(
            @PathVariable UUID tenantId,
            @PathVariable Long invitationId) {
        invitationService.revokeInvitation(tenantId, invitationId);
    }
}