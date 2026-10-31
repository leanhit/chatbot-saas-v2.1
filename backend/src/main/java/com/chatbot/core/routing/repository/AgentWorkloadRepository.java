package com.chatbot.core.routing.repository;

import com.chatbot.core.routing.model.AgentWorkload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentWorkloadRepository extends JpaRepository<AgentWorkload, Long> {

    List<AgentWorkload> findByTenantId(Long tenantId);

    Optional<AgentWorkload> findByTenantIdAndAgentId(Long tenantId, Long agentId);

    List<AgentWorkload> findByTenantIdAndStatus(Long tenantId, AgentWorkload.AgentStatus status);

    List<AgentWorkload> findByTenantIdAndStatusOrderByPriorityScoreDesc(Long tenantId, AgentWorkload.AgentStatus status);

    @Query("SELECT aw FROM AgentWorkload aw WHERE aw.tenantId = :tenantId AND aw.status = :status AND aw.activeConversations < aw.maxConcurrentConversations ORDER BY aw.activeConversations ASC")
    List<AgentWorkload> findAvailableAgentsByWorkload(@Param("tenantId") Long tenantId, @Param("status") AgentWorkload.AgentStatus status);

    @Query("SELECT aw FROM AgentWorkload aw WHERE aw.tenantId = :tenantId AND aw.status = :status ORDER BY aw.priorityScore DESC")
    List<AgentWorkload> findAvailableAgentsByPriority(@Param("tenantId") Long tenantId, @Param("status") AgentWorkload.AgentStatus status);
}
