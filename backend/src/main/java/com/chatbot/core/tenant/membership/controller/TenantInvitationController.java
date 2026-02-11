package com.chatbot.core.tenant.membership.controller;

import com.chatbot.core.tenant.membership.service.TenantInvitationService;
import com.chatbot.core.tenant.membership.dto.InviteMemberRequest;
import com.chatbot.core.tenant.membership.dto.InvitationResponse;
import com.chatbot.core.identity.model.Auth;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid; // Đổi từ javax sang jakarta nếu dùng Spring Boot 3
import java.util.List;

@RestController
@RequestMapping("/api/tenants/{tenantId}/invitations")
@RequiredArgsConstructor
public class TenantInvitationController {
    
    private final TenantInvitationService invitationService;
    
    // 1. Admin mời thành viên
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void inviteMember(
            @PathVariable Long tenantId,
            @Valid @RequestBody InviteMemberRequest request,
            @AuthenticationPrincipal(expression = "auth") Auth currentUser) {
        invitationService.inviteMember(tenantId, request, currentUser);
    }
    
    // 2. Danh sách lời mời của Tenant
    @GetMapping
    public List<InvitationResponse> listInvitations(@PathVariable Long tenantId) {
        return invitationService.listInvitations(tenantId);
    }
    
    // 3. User chấp nhận lời mời
    @PostMapping("/{token}/accept")
    public void acceptInvitation(
            @PathVariable String token,
            @AuthenticationPrincipal(expression = "auth") Auth user) {
        invitationService.acceptInvitation(token, user);
    }

    // 4. User từ chối lời mời (THÊM MỚI)
    @PostMapping("/{token}/reject")
    public void rejectInvitation(
            @PathVariable String token,
            @AuthenticationPrincipal(expression = "auth") Auth user) {
        invitationService.rejectInvitation(token, user);
    }
    
    // 5. Admin thu hồi lời mời
    @DeleteMapping("/{invitationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeInvitation(
            @PathVariable Long tenantId,
            @PathVariable Long invitationId) {
        invitationService.revokeInvitation(tenantId, invitationId);
    }
}