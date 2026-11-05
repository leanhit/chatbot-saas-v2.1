package com.chatbot.core.training.repository;

import com.chatbot.core.training.model.AgentOnboardingProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgentOnboardingProgressRepository extends JpaRepository<AgentOnboardingProgress, Long> {

    List<AgentOnboardingProgress> findByAgentId(Long agentId);

    List<AgentOnboardingProgress> findByAgentIdAndTenantId(Long agentId, Long tenantId);

    List<AgentOnboardingProgress> findByTenantId(Long tenantId);

    Optional<AgentOnboardingProgress> findByAgentIdAndTaskId(Long agentId, Long taskId);

    Optional<AgentOnboardingProgress> findByAgentIdAndTaskIdAndTenantId(Long agentId, Long taskId, Long tenantId);

    List<AgentOnboardingProgress> findByTenantIdAndAgentId(Long tenantId, Long agentId);
}
