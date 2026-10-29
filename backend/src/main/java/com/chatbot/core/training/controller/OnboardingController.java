package com.chatbot.core.training.controller;

import com.chatbot.core.training.model.AgentOnboardingProgress;
import com.chatbot.core.training.model.AgentOnboardingTask;
import com.chatbot.core.training.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @GetMapping("/tasks")
    public ResponseEntity<List<AgentOnboardingTask>> getAllOnboardingTasks() {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<AgentOnboardingTask> tasks = onboardingService.getAllOnboardingTasks(tenantId);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/tasks")
    public ResponseEntity<AgentOnboardingTask> createOnboardingTask(@RequestBody AgentOnboardingTask task) {
        AgentOnboardingTask created = onboardingService.createOnboardingTask(task);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/progress/start")
    public ResponseEntity<AgentOnboardingProgress> startTask(
            @RequestParam Long agentId,
            @RequestParam Long taskId) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        AgentOnboardingProgress progress = onboardingService.startTask(agentId, taskId, tenantId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/progress/agent/{agentId}")
    public ResponseEntity<List<AgentOnboardingProgress>> getAgentOnboardingProgress(@PathVariable Long agentId) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<AgentOnboardingProgress> progress = onboardingService.getAgentOnboardingProgress(agentId, tenantId);
        return ResponseEntity.ok(progress);
    }
}
