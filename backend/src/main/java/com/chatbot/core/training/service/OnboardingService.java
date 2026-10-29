package com.chatbot.core.training.service;

import com.chatbot.core.training.model.AgentOnboardingProgress;
import com.chatbot.core.training.model.AgentOnboardingTask;
import com.chatbot.core.training.repository.AgentOnboardingProgressRepository;
import com.chatbot.core.training.repository.AgentOnboardingTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final AgentOnboardingTaskRepository onboardingTaskRepository;
    private final AgentOnboardingProgressRepository onboardingProgressRepository;

    public AgentOnboardingTask createOnboardingTask(AgentOnboardingTask task) {
        return onboardingTaskRepository.save(task);
    }

    public List<AgentOnboardingTask> getAllOnboardingTasks(Long tenantId) {
        return onboardingTaskRepository.findAll();
    }

    public AgentOnboardingProgress startTask(Long agentId, Long taskId, Long tenantId) {
        AgentOnboardingProgress progress = AgentOnboardingProgress.builder()
            .agentId(agentId)
            .taskId(taskId)
            .build();
        return onboardingProgressRepository.save(progress);
    }

    public List<AgentOnboardingProgress> getAgentOnboardingProgress(Long agentId, Long tenantId) {
        return onboardingProgressRepository.findAll();
    }
}
