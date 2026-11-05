package com.chatbot.core.training.repository;

import com.chatbot.core.training.model.AgentOnboardingTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgentOnboardingTaskRepository extends JpaRepository<AgentOnboardingTask, Long> {

    List<AgentOnboardingTask> findByTenantId(Long tenantId);
}
