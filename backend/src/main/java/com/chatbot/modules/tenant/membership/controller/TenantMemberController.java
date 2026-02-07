package com.chatbot.modules.tenant.membership.controller;

import com.chatbot.modules.auth.security.CustomUserDetails;
import com.chatbot.modules.tenant.membership.dto.*;
import com.chatbot.modules.tenant.membership.model.TenantRole;
import com.chatbot.modules.tenant.membership.service.TenantMemberService;
import com.chatbot.modules.tenant.membership.service.TenantJoinRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

/**
 * Tenant member controller for v0.1
 * Core member management endpoints
 */
@RestController
@RequestMapping("/api/tenants/{tenantId}/members")
@RequiredArgsConstructor
@Slf4j
public class TenantMemberController {

    private final TenantMemberService tenantMemberService;
    private final TenantJoinRequestService joinRequestService;

    /* =====================================================
     * 2️⃣ JOIN REQUESTS (User → Tenant)
     * ===================================================== */

    /**
     * POST /tenants/{tenantId}/members/join-requests
     */
    @PostMapping("/join-requests")
    public ResponseEntity<Void> requestJoin(
            @PathVariable UUID tenantId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest request
    ) {
        joinRequestService.requestToJoin(tenantId, currentUser.getUserId(), currentUser.getUsername());
        return ResponseEntity.ok().build();
    }

    /**
     * GET /tenants/{tenantId}/members/join-requests
     */
    @GetMapping("/join-requests")
    public ResponseEntity<List<MemberResponse>> listJoinRequests(
            @PathVariable UUID tenantId
    ) {
        List<MemberResponse> requests = joinRequestService.getPendingRequests(tenantId);
        return ResponseEntity.ok(requests);
    }

    /**
     * PATCH /tenants/{tenantId}/members/join-requests/{requestId}
     */
    @PatchMapping("/join-requests/{requestId}")
    public ResponseEntity<Void> updateJoinRequest(
            @PathVariable UUID tenantId,
            @PathVariable Long requestId,
            @RequestBody UpdateJoinRequest request
    ) {
        joinRequestService.updateStatus(tenantId, requestId, request.getStatus());
        return ResponseEntity.ok().build();
    }

    /* =====================================================
     * 3️⃣ TENANT MEMBERS (ADMIN MANAGEMENT)
     * ===================================================== */

    /**
     * Get all members of a tenant
     */
    @GetMapping
    public ResponseEntity<List<MemberResponse>> getTenantMembers(@PathVariable UUID tenantId) {
        List<MemberResponse> members = tenantMemberService.getTenantMembers(tenantId);
        return ResponseEntity.ok(members);
    }

    /**
     * Update member role
     */
    @PutMapping("/{userId}/role")
    public ResponseEntity<Void> updateMemberRole(
            @PathVariable UUID tenantId,
            @PathVariable UUID userId,
            @RequestBody TenantRole newRole) {
        tenantMemberService.updateMemberRole(tenantId, userId, newRole);
        return ResponseEntity.ok().build();
    }

    /**
     * Remove member from tenant
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID tenantId,
            @PathVariable UUID userId) {
        tenantMemberService.removeMember(tenantId, userId);
        return ResponseEntity.ok().build();
    }
}
