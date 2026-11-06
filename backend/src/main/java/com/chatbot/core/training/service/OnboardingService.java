package com.chatbot.core.training.service;

import com.chatbot.core.training.model.AgentOnboardingProgress;
import com.chatbot.core.training.model.AgentOnboardingTask;
import com.chatbot.core.training.repository.AgentOnboardingProgressRepository;
import com.chatbot.core.training.repository.AgentOnboardingTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final AgentOnboardingTaskRepository onboardingTaskRepository;
    private final AgentOnboardingProgressRepository onboardingProgressRepository;

    public AgentOnboardingTask createOnboardingTask(AgentOnboardingTask task) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        task.setTenantId(tenantId);
        return onboardingTaskRepository.save(task);
    }

    public List<AgentOnboardingTask> getAllOnboardingTasks(Long tenantId) {
        return onboardingTaskRepository.findByTenantId(tenantId);
    }

    public AgentOnboardingProgress startTask(Long agentId, Long taskId, Long tenantId) {
        AgentOnboardingProgress progress = AgentOnboardingProgress.builder()
            .agentId(agentId)
            .taskId(taskId)
            .status(AgentOnboardingProgress.CompletionStatus.IN_PROGRESS)
            .startedAt(LocalDateTime.now().toString())
            .build();
        progress.setTenantId(tenantId);
        return onboardingProgressRepository.save(progress);
    }

    @Transactional
    public AgentOnboardingProgress completeTask(Long agentId, Long taskId, Long tenantId, String notes) {
        AgentOnboardingProgress progress = onboardingProgressRepository
            .findByAgentIdAndTaskIdAndTenantId(agentId, taskId, tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Onboarding progress not found"));

        progress.setStatus(AgentOnboardingProgress.CompletionStatus.COMPLETED);
        progress.setCompletedAt(LocalDateTime.now().toString());
        progress.setNotes(notes);
        return onboardingProgressRepository.save(progress);
    }

    public List<AgentOnboardingProgress> getAgentOnboardingProgress(Long agentId, Long tenantId) {
        return onboardingProgressRepository.findByTenantIdAndAgentId(tenantId, agentId);
    }

    public boolean isOnboardingComplete(Long agentId, Long tenantId) {
        // Get all required onboarding tasks for tenant
        List<AgentOnboardingTask> requiredTasks = onboardingTaskRepository.findByTenantId(tenantId);

        // If no tasks defined, consider onboarding complete
        if (requiredTasks.isEmpty()) {
            return true;
        }

        // Check if all required tasks are completed
        for (AgentOnboardingTask task : requiredTasks) {
            if (task.getRequired() != null && task.getRequired()) {
                AgentOnboardingProgress progress = onboardingProgressRepository
                    .findByAgentIdAndTaskIdAndTenantId(agentId, task.getId(), tenantId)
                    .orElse(null);
                
                if (progress == null || progress.getStatus() != AgentOnboardingProgress.CompletionStatus.COMPLETED) {
                    return false;
                }
            }
        }

        return true;
    }

    @Transactional
    public void approveTask(Long agentId, Long taskId, Long supervisorId, String comments, Long tenantId) {
        AgentOnboardingProgress progress = onboardingProgressRepository
            .findByAgentIdAndTaskIdAndTenantId(agentId, taskId, tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Onboarding progress not found"));

        progress.setSupervisorApproval(AgentOnboardingProgress.SupervisorApproval.APPROVED);
        progress.setSupervisorId(supervisorId);
        progress.setSupervisorComments(comments);
        progress.setApprovedAt(LocalDateTime.now().toString());
        onboardingProgressRepository.save(progress);
    }

    @Transactional
    public void rejectTask(Long agentId, Long taskId, Long supervisorId, String comments, Long tenantId) {
        AgentOnboardingProgress progress = onboardingProgressRepository
            .findByAgentIdAndTaskIdAndTenantId(agentId, taskId, tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Onboarding progress not found"));

        progress.setSupervisorApproval(AgentOnboardingProgress.SupervisorApproval.REJECTED);
        progress.setSupervisorId(supervisorId);
        progress.setSupervisorComments(comments);
        onboardingProgressRepository.save(progress);
    }
}
