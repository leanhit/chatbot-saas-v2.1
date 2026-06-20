package com.chatbot.core.tenant.membership.controller;

import com.chatbot.core.user.model.User;
import com.chatbot.core.identity.security.CustomUserDetails;
import com.chatbot.core.tenant.membership.dto.*;
import com.chatbot.core.tenant.membership.service.TenantMembershipFacade;
import com.chatbot.core.tenant.service.TenantPermissionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tenants/members")
public class TenantMemberQueryController {

    private final TenantMembershipFacade facade;
    private final TenantPermissionValidator permissionValidator;

    @GetMapping("/pending-tenants")
    public List<TenantPendingResponse> myPendingTenants() {
        User user = permissionValidator.getCurrentUser(); 
        return facade.myPending(user);
    }
    
    /**
     * Lấy danh sách lời mời đang chờ xử lý của user hiện tại
     */
    @GetMapping("/my-invitations")
    public List<InvitationResponse> getMyInvitations() {
        User user = permissionValidator.getCurrentUser();
        return facade.getMyInvitations(user);
    }

    /**
     * User chấp nhận lời mời
     */
    @PostMapping("/invitations/{token}/accept")
    public void acceptInvitation(@PathVariable String token) {
        User user = permissionValidator.getCurrentUser();
        facade.acceptInvitation(token, user);
    }

    /**
     * User từ chối lời mời
     */
    @PostMapping("/invitations/{token}/reject")
    public void rejectInvitation(@PathVariable String token) {
        User user = permissionValidator.getCurrentUser();
        facade.rejectInvitation(token, user);
    }
}
