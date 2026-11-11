package com.chatbot.core.training.controller;

import com.chatbot.core.training.model.AgentOnboardingProgress;
import com.chatbot.core.training.model.AgentOnboardingTask;
import com.chatbot.core.training.service.OnboardingService;
import com.chatbot.core.message.store.model.Agent;
import com.chatbot.core.message.store.repository.AgentRepository;
import com.chatbot.core.user.model.User;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.tenant.service.TenantPermissionValidator;
import com.chatbot.core.tenant.exception.InsufficientPermissionException;
import com.chatbot.shared.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final TenantPermissionValidator permissionValidator;
    private final AgentRepository agentRepository;

    private void requireOwnerOrEditor() {
        Long tenantId = TenantContext.getTenantId();
        String email = permissionValidator.getCurrentUserEmail();
        if (tenantId == null || email == null || !permissionValidator.isOwnerOrEditor(tenantId, email)) {
            throw new InsufficientPermissionException(ErrorCode.INSUFFICIENT_PERMISSION,
                    "Only tenant owner or editor can modify onboarding tasks");
        }
    }

    private void requireSelfAgentOrOwnerOrEditor(Long agentId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        Agent agent = agentRepository.findByIdAndTenantId(agentId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found"));
        User current = permissionValidator.getCurrentUser();
        boolean isSelf = current != null && (
                (agent.getUserId() != null && agent.getUserId().equals(current.getId()))
                || (agent.getEmail() != null && current.getEmail() != null
                    && agent.getEmail().equalsIgnoreCase(current.getEmail())));
        if (!isSelf) {
            requireOwnerOrEditor();
        }
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<AgentOnboardingTask>> getAllOnboardingTasks() {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<AgentOnboardingTask> tasks = onboardingService.getAllOnboardingTasks(tenantId);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/tasks")
    public ResponseEntity<AgentOnboardingTask> createOnboardingTask(@RequestBody AgentOnboardingTask task) {
        requireOwnerOrEditor();
        AgentOnboardingTask created = onboardingService.createOnboardingTask(task);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/progress/start")
    public ResponseEntity<AgentOnboardingProgress> startTask(
            @RequestParam Long agentId,
            @RequestParam Long taskId) {
        requireSelfAgentOrOwnerOrEditor(agentId);
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        AgentOnboardingProgress progress = onboardingService.startTask(agentId, taskId, tenantId);
        return ResponseEntity.ok(progress);
    }

    @PostMapping("/progress/complete")
    public ResponseEntity<AgentOnboardingProgress> completeTask(
            @RequestParam Long agentId,
            @RequestParam Long taskId,
            @RequestParam(required = false) String notes) {
        requireSelfAgentOrOwnerOrEditor(agentId);
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        AgentOnboardingProgress progress = onboardingService.completeTask(agentId, taskId, tenantId, notes);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/progress/agent/{agentId}")
    public ResponseEntity<List<AgentOnboardingProgress>> getAgentOnboardingProgress(@PathVariable Long agentId) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<AgentOnboardingProgress> progress = onboardingService.getAgentOnboardingProgress(agentId, tenantId);
        return ResponseEntity.ok(progress);
    }

    @PostMapping("/progress/approve")
    public ResponseEntity<Void> approveTask(
            @RequestParam Long agentId,
            @RequestParam Long taskId,
            @RequestParam(required = false) String comments) {
        requireOwnerOrEditor();
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        Long supervisorId = permissionValidator.getCurrentUser() != null
                ? permissionValidator.getCurrentUser().getId() : null;
        onboardingService.approveTask(agentId, taskId, supervisorId, comments, tenantId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/progress/reject")
    public ResponseEntity<Void> rejectTask(
            @RequestParam Long agentId,
            @RequestParam Long taskId,
            @RequestParam(required = false) String comments) {
        requireOwnerOrEditor();
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        Long supervisorId = permissionValidator.getCurrentUser() != null
                ? permissionValidator.getCurrentUser().getId() : null;
        onboardingService.rejectTask(agentId, taskId, supervisorId, comments, tenantId);
        return ResponseEntity.ok().build();
    }
}
