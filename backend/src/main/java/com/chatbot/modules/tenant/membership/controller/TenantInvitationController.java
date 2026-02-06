package com.chatbot.modules.tenant.membership.controller;

// LEGACY CLASS - DISABLED FOR TENANT HUB v0.1
// This class contains invitation controller logic that is not part of v0.1 scope
// TODO: Remove this class completely when v0.1 is stable

/*
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

@RestController
@RequestMapping("/api/tenants/{tenantId}/invitations")
@RequiredArgsConstructor
public class TenantInvitationController {
    
    private final TenantInvitationService invitationService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void inviteMember(
            @PathVariable Long tenantId,
            @Valid @RequestBody InviteMemberRequest request,
            @AuthenticationPrincipal(expression = "auth") Auth currentUser) {
        // Legacy invitation controller logic - not used in v0.1
    }
    
    @GetMapping
    public List<InvitationResponse> listInvitations(@PathVariable Long tenantId) {
        // Legacy invitation controller logic - not used in v0.1
        return null;
    }
    
    @PostMapping("/{token}/accept")
    public void acceptInvitation(
            @PathVariable String token,
            @AuthenticationPrincipal(expression = "auth") Auth user) {
        // Legacy invitation controller logic - not used in v0.1
    }

    @PostMapping("/{token}/reject")
    public void rejectInvitation(
            @PathVariable String token,
            @AuthenticationPrincipal(expression = "auth") Auth user) {
        // Legacy invitation controller logic - not used in v0.1
    }
    
    @DeleteMapping("/{invitationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeInvitation(
            @PathVariable Long tenantId,
            @PathVariable Long invitationId) {
        // Legacy invitation controller logic - not used in v0.1
    }
}
*/