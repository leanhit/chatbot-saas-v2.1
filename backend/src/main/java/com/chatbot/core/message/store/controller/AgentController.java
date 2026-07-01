package com.chatbot.core.message.store.controller;

import com.chatbot.core.message.store.model.Agent;
import com.chatbot.core.message.store.service.AgentService;
import com.chatbot.core.tenant.infra.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Agent Management", description = "Agent management operations")
public class AgentController {

    private final AgentService agentService;

    @GetMapping
    @Operation(summary = "Get all agents for current tenant")
    public ResponseEntity<List<Agent>> getAgents() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(agentService.getAgentsByTenant(tenantId));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active agents for current tenant")
    public ResponseEntity<List<Agent>> getActiveAgents() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(agentService.getActiveAgentsByTenant(tenantId));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available agents (online and can accept more conversations)")
    public ResponseEntity<List<Agent>> getAvailableAgents() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(agentService.getAvailableAgents(tenantId));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get agent statistics for current tenant")
    public ResponseEntity<AgentService.AgentStats> getAgentStats() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(agentService.getAgentStats(tenantId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get agent by ID")
    public ResponseEntity<Agent> getAgent(@PathVariable Long id) {
        return agentService.getAgentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new agent")
    public ResponseEntity<Agent> createAgent(@RequestBody Agent agent) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        agent.setTenantId(tenantId);
        return ResponseEntity.ok(agentService.createAgent(agent));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update existing agent")
    public ResponseEntity<Agent> updateAgent(@PathVariable Long id, @RequestBody Agent agent) {
        try {
            return ResponseEntity.ok(agentService.updateAgent(id, agent));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete agent")
    public ResponseEntity<Void> deleteAgent(@PathVariable Long id) {
        try {
            agentService.deleteAgent(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update agent status")
    public ResponseEntity<Void> updateAgentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            Agent.AgentStatus status = Agent.AgentStatus.valueOf(request.get("status"));
            agentService.updateAgentStatus(id, status);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/online")
    @Operation(summary = "Set agent online")
    public ResponseEntity<Void> setAgentOnline(@PathVariable Long id) {
        agentService.setAgentOnline(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/offline")
    @Operation(summary = "Set agent offline")
    public ResponseEntity<Void> setAgentOffline(@PathVariable Long id) {
        agentService.setAgentOffline(id);
        return ResponseEntity.ok().build();
    }
}
