package com.chatbot.core.message.store.controller;

import com.chatbot.core.membership.model.MembershipStatus;
import com.chatbot.core.membership.repository.TenantMemberRepository;
import com.chatbot.core.message.store.model.Agent;
import com.chatbot.core.message.store.service.AgentProvisioningService;
import com.chatbot.core.message.store.service.AgentService;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.tenant.service.TenantPermissionValidator;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Agent Management Controller
 *
 * Phân quyền:
 *   - READ (list, stats, by-id)     → bất kỳ active member của tenant
 *   - WRITE (update, delete, repair) → chỉ OWNER của tenant
 *   - STATUS (online/offline/status) → OWNER của tenant HOẶC chính agent đó
 *
 * System ADMIN không có quyền đặc biệt ở đây — agent management là việc nội bộ tenant.
 */
@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Agent Management", description = "Agent management operations — scoped to tenant OWNER")
public class AgentController {

    private final AgentService agentService;
    private final AgentProvisioningService agentProvisioningService;
    private final TenantPermissionValidator permissionValidator;
    private final TenantMemberRepository tenantMemberRepository;
    private final UserRepository userRepository;

    /* ========== READ — active members of this tenant ========== */

    @GetMapping
    @Operation(summary = "Get all agents for current tenant")
    public ResponseEntity<List<Agent>> getAgents() {
        Long tenantId = requireTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().build();
        requireActiveMember(tenantId);
        return ResponseEntity.ok(agentService.getAgentsByTenant(tenantId));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active agents for current tenant")
    public ResponseEntity<List<Agent>> getActiveAgents() {
        Long tenantId = requireTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().build();
        requireActiveMember(tenantId);
        return ResponseEntity.ok(agentService.getActiveAgentsByTenant(tenantId));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available agents (online and can accept more conversations)")
    public ResponseEntity<List<Agent>> getAvailableAgents() {
        Long tenantId = requireTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().build();
        requireActiveMember(tenantId);
        return ResponseEntity.ok(agentService.getAvailableAgents(tenantId));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get agent statistics for current tenant")
    public ResponseEntity<AgentService.AgentStats> getAgentStats() {
        Long tenantId = requireTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().build();
        requireActiveMember(tenantId);
        return ResponseEntity.ok(agentService.getAgentStats(tenantId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get agent by ID")
    public ResponseEntity<Agent> getAgent(@PathVariable Long id) {
        Long tenantId = requireTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().build();
        requireActiveMember(tenantId);
        return agentService.getAgentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /agents removed — Agents are auto-provisioned when a TenantMember becomes ACTIVE.
    // To add an agent, invite or approve a user to join the tenant.

    /* ========== WRITE — OWNER only ========== */

    /**
     * Update agent profile. Only OWNER of the tenant can update.
     * Agent belongs-to-tenant is validated in service layer.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update existing agent (OWNER only)")
    public ResponseEntity<Agent> updateAgent(@PathVariable Long id, @RequestBody Agent agent) {
        Long tenantId = requireTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().build();
        if (!isOwner(tenantId)) return forbidden("Only OWNER can update agents");
        try {
            return ResponseEntity.ok(agentService.updateAgent(id, agent, tenantId));
        } catch (com.chatbot.shared.exceptions.ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete agent. Only OWNER of the tenant can delete.
     * Agent belongs-to-tenant is validated in service layer.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete agent (OWNER only)")
    public ResponseEntity<Void> deleteAgent(@PathVariable Long id) {
        Long tenantId = requireTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().build();
        if (!isOwner(tenantId)) return forbidden("Only OWNER can delete agents");
        try {
            agentService.deleteAgent(id, tenantId);
            return ResponseEntity.ok().build();
        } catch (com.chatbot.shared.exceptions.ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /* ========== STATUS — OWNER or the agent themselves ========== */

    /**
     * Update agent status. Allowed for OWNER or the agent whose userId matches.
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update agent status (OWNER or self)")
    public ResponseEntity<Void> updateAgentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        Long tenantId = requireTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().build();
        if (!isOwnerOrSelf(tenantId, id)) return forbidden("Only OWNER or the agent themselves can update status");
        try {
            Agent.AgentStatus status = Agent.AgentStatus.valueOf(request.get("status"));
            agentService.updateAgentStatus(id, status);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/online")
    @Operation(summary = "Set agent online (OWNER or self)")
    public ResponseEntity<Void> setAgentOnline(@PathVariable Long id) {
        Long tenantId = requireTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().build();
        if (!isOwnerOrSelf(tenantId, id)) return forbidden("Only OWNER or the agent themselves can go online");
        agentService.setAgentOnline(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/offline")
    @Operation(summary = "Set agent offline (OWNER or self)")
    public ResponseEntity<Void> setAgentOffline(@PathVariable Long id) {
        Long tenantId = requireTenantId();
        if (tenantId == null) return ResponseEntity.badRequest().build();
        if (!isOwnerOrSelf(tenantId, id)) return forbidden("Only OWNER or the agent themselves can go offline");
        agentService.setAgentOffline(id);
        return ResponseEntity.ok().build();
    }

    /* ========== REPAIR — OWNER only ========== */

    /**
     * Repair a missing Agent profile for an ACTIVE member whose auto-provisioning previously failed.
     * Only OWNER of the tenant can trigger this. Idempotent — safe to call multiple times.
     */
    @PostMapping("/repair/{userId}")
    @Operation(summary = "Repair missing Agent profile for an active member (OWNER only)")
    public ResponseEntity<Map<String, String>> repairAgentForMember(@PathVariable Long userId) {
        Long tenantId = requireTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tenant context is missing"));
        }

        String currentEmail = permissionValidator.getCurrentUserEmail();
        if (!isOwner(tenantId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only OWNER can repair agent profiles"));
        }

        // Validate the target user is an ACTIVE member of this tenant
        boolean isActiveMember = tenantMemberRepository
                .existsByTenant_IdAndUserIdAndStatus(tenantId, userId, MembershipStatus.ACTIVE);
        if (!isActiveMember) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "User " + userId + " is not an active member of this tenant"));
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found: " + userId));
        }

        String fullName = (user.getProfile() != null) ? user.getProfile().getFullName() : user.getEmail();
        String avatar   = (user.getProfile() != null) ? user.getProfile().getAvatar()   : null;

        try {
            agentProvisioningService.repairAgentForMember(tenantId, userId, user.getEmail(), fullName, avatar);
            log.info("[AgentController] Repaired agent for userId={} in tenantId={} by {}", userId, tenantId, currentEmail);
            return ResponseEntity.ok(Map.of("message", "Agent repaired for userId=" + userId));
        } catch (Exception e) {
            log.error("[AgentController] Repair failed for userId={} in tenantId={}: {}", userId, tenantId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Repair failed: " + e.getMessage()));
        }
    }

    /* ========== Private helpers ========== */

    private Long requireTenantId() {
        return TenantContext.getTenantId();
    }

    /** Kiểm tra caller là OWNER của tenant. System ADMIN không được bypass. */
    private boolean isOwner(Long tenantId) {
        String email = permissionValidator.getCurrentUserEmail();
        return permissionValidator.isOwner(tenantId, email);
    }

    /** Kiểm tra caller là OWNER HOẶC chính agent đó (so sánh userId). */
    private boolean isOwnerOrSelf(Long tenantId, Long agentId) {
        if (isOwner(tenantId)) return true;
        // Kiểm tra agent có phải của chính người đang gọi không
        return agentService.getAgentById(agentId)
                .map(agent -> {
                    try {
                        User currentUser = permissionValidator.getCurrentUser();
                        return agent.getUserId() != null && agent.getUserId().equals(currentUser.getId());
                    } catch (Exception e) {
                        return false;
                    }
                })
                .orElse(false);
    }

    /** Kiểm tra caller là active member của tenant. */
    private void requireActiveMember(Long tenantId) {
        String email = permissionValidator.getCurrentUserEmail();
        if (!permissionValidator.isActiveMember(tenantId, email)) {
            throw new com.chatbot.core.tenant.exception.InsufficientPermissionException(
                    com.chatbot.shared.exceptions.ErrorCode.CANNOT_MANAGE_MEMBERS,
                    "You are not an active member of this tenant");
        }
    }

    private <T> ResponseEntity<T> forbidden(String message) {
        log.warn("[AgentController] 403 Forbidden: {}", message);
        return ResponseEntity.status(403).build();
    }
}
