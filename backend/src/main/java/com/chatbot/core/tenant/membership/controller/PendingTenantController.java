package com.chatbot.core.tenant.membership.controller;

import com.chatbot.core.user.model.User;
import com.chatbot.core.identity.security.CustomUserDetails;
import com.chatbot.core.tenant.membership.dto.TenantPendingResponse;
import com.chatbot.core.tenant.membership.service.TenantMembershipFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for user's pending tenant requests
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PendingTenantController {

    private final TenantMembershipFacade facade;

    @GetMapping("/pending-tenants")
    public List<TenantPendingResponse> myPendingTenants(
            @AuthenticationPrincipal CustomUserDetails customUser
    ) {
        User user = customUser.getUser(); 
        return facade.myPending(user);
    }
}
