package com.chatbot.core.tenant.membership.controller;

import com.chatbot.core.identity.model.Auth;
import com.chatbot.core.tenant.membership.dto.*;
import com.chatbot.core.tenant.membership.service.TenantMembershipFacade;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tenants/{tenantId}/members")
public class TenantMemberController {

    private final TenantMembershipFacade facade;

    /* =====================================================
     * 2️⃣ JOIN REQUESTS (User → Tenant)
     * ===================================================== */

    /**
     * POST /tenants/{tenantId}/members/join-requests
     */
    @PostMapping("/join-requests")
    public void requestJoin(
            @PathVariable Long tenantId,
            @AuthenticationPrincipal(expression = "auth") Auth user, // Thêm (expression = "auth")
            HttpServletRequest request
    ) {
        // Lúc này user sẽ không còn null nữa
        facade.requestJoin(tenantId, user);
    }

    /**
     * GET /tenants/{tenantId}/members/join-requests
     */
    @GetMapping("/join-requests")
    public List<MemberResponse> listJoinRequests(
            @PathVariable Long tenantId
    ) {
        return facade.pending(tenantId);
    }

    /**
     * PATCH /tenants/{tenantId}/members/join-requests/{requestId}
     */
    @PatchMapping("/join-requests/{requestId}")
    public void updateJoinRequest(
            @PathVariable Long tenantId,
            @PathVariable Long requestId,
            @RequestBody UpdateJoinRequest request
    ) {
        facade.updateJoinRequest(tenantId, requestId, request.getStatus());
    }

    /* =====================================================
     * 3️⃣ TENANT MEMBERS (ADMIN MANAGEMENT)
     * ===================================================== */

    /**
     * GET /tenants/{tenantId}/members
     */
    @GetMapping
    public Page<MemberResponse> listMembers(
            @PathVariable Long tenantId,
            Pageable pageable
    ) {
        return facade.listMembers(tenantId, pageable);
    }

    /**
     * GET /tenants/{tenantId}/members/{userId}
     */
    @GetMapping("/{userId}")
    public MemberResponse getMember(
            @PathVariable Long tenantId,
            @PathVariable Long userId
    ) {
        return facade.getMember(tenantId, userId);
    }

    /**
     * PUT /tenants/{tenantId}/members/{userId}/role
     */
    @PutMapping("/{userId}/role")
    public void updateRole(
            @PathVariable Long tenantId,
            @PathVariable Long userId,
            @RequestBody UpdateRoleRequest request
    ) {
        facade.updateRole(tenantId, userId, request.getRole());
    }

    /**
     * DELETE /tenants/{tenantId}/members/{userId}
     */
    @DeleteMapping("/{userId}")
    public void removeMember(
            @PathVariable Long tenantId,
            @PathVariable Long userId
    ) {
        facade.removeMember(tenantId, userId);
    }

    /* =====================================================
     * 4️⃣ MEMBER SELF-SERVICE
     * ===================================================== */

    /**
     * GET /tenants/{tenantId}/members/me
     */
    @GetMapping("/me")
    public MemberResponse myMember(
            @PathVariable Long tenantId,
            @AuthenticationPrincipal Auth user
    ) {
        return facade.myMember(tenantId, user);
    }

    /**
     * DELETE /tenants/{tenantId}/members/me
     */
    @DeleteMapping("/me")
    public void leaveTenant(
            @PathVariable Long tenantId,
            @AuthenticationPrincipal Auth user
    ) {
        facade.leave(tenantId, user);
    }
}
