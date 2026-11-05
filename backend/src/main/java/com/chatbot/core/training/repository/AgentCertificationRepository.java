package com.chatbot.core.training.repository;

import com.chatbot.core.training.model.AgentCertification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgentCertificationRepository extends JpaRepository<AgentCertification, Long> {

    List<AgentCertification> findByAgentId(Long agentId);

    List<AgentCertification> findByAgentIdAndTenantId(Long agentId, Long tenantId);

    List<AgentCertification> findByTenantId(Long tenantId);

    Optional<AgentCertification> findByAgentIdAndCertificationLevelId(Long agentId, Long certificationLevelId);

    Optional<AgentCertification> findByAgentIdAndCertificationLevelIdAndTenantId(Long agentId, Long certificationLevelId, Long tenantId);

    List<AgentCertification> findByTenantIdAndAgentId(Long tenantId, Long agentId);

    List<AgentCertification> findByTenantIdAndCertificationLevelId(Long tenantId, Long certificationLevelId);
}
