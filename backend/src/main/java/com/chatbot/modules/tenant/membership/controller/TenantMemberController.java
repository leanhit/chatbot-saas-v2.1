package com.chatbot.modules.tenant.membership.controller;

import com.chatbot.modules.tenant.membership.dto.MemberResponse;
import com.chatbot.modules.tenant.membership.model.TenantRole;
import com.chatbot.modules.tenant.membership.service.TenantMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
