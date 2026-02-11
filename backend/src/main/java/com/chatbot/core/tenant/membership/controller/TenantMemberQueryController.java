package com.chatbot.core.tenant.membership.controller;

import com.chatbot.core.identity.model.Auth;
import com.chatbot.core.identity.security.CustomUserDetails;
import com.chatbot.core.tenant.membership.dto.*;
import com.chatbot.core.tenant.membership.service.TenantMembershipFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping("/pending-tenants")
    public List<TenantPendingResponse> myPendingTenants(
            @AuthenticationPrincipal CustomUserDetails customUser // Lấy CustomUserDetails
    ) {
        // Lấy Entity Auth từ bên trong CustomUserDetails
        Auth user = customUser.getAuth(); 
        return facade.myPending(user);
    }
    
    /**
     * Lấy danh sách lời mời đang chờ xử lý của user hiện tại
     */
    @GetMapping("/my-invitations")
    public List<InvitationResponse> getMyInvitations(
            @AuthenticationPrincipal CustomUserDetails customUser
    ) {
        return facade.getMyInvitations(customUser.getAuth());
    }
}
