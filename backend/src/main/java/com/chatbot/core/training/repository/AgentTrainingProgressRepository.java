package com.chatbot.core.training.repository;

import com.chatbot.core.training.model.AgentTrainingProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgentTrainingProgressRepository extends JpaRepository<AgentTrainingProgress, Long> {

    List<AgentTrainingProgress> findByAgentId(Long agentId);

    List<AgentTrainingProgress> findByAgentIdAndTenantId(Long agentId, Long tenantId);

    List<AgentTrainingProgress> findByTenantId(Long tenantId);

    Optional<AgentTrainingProgress> findByAgentIdAndTrainingId(Long agentId, Long trainingId);

    Optional<AgentTrainingProgress> findByAgentIdAndTrainingIdAndTenantId(Long agentId, Long trainingId, Long tenantId);

    List<AgentTrainingProgress> findByTenantIdAndAgentId(Long tenantId, Long agentId);

    List<AgentTrainingProgress> findByTrainingIdAndTenantId(Long trainingId, Long tenantId);
}
