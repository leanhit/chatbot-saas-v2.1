package com.chatbot.core.routing.repository;

import com.chatbot.core.routing.model.ConversationRouting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRoutingRepository extends JpaRepository<ConversationRouting, Long> {

    Optional<ConversationRouting> findByConversationId(Long conversationId);

    List<ConversationRouting> findByTenantIdAndRoutingStatusOrderByPriorityAsc(Long tenantId, ConversationRouting.RoutingStatus status);

    List<ConversationRouting> findByTenantIdAndAssignedAgentId(Long tenantId, Long agentId);

    List<ConversationRouting> findByTenantIdAndAssignedTeamId(Long tenantId, Long teamId);

    List<ConversationRouting> findByTenantIdAndRoutingStatusAndQueuePositionNotNullOrderByQueuePositionAsc(Long tenantId, ConversationRouting.RoutingStatus status);

    List<ConversationRouting> findByTenantIdAndIsEscalatedTrueOrderByEscalatedAtDesc(Long tenantId);

    Optional<ConversationRouting> findByTenantIdAndRoutingStatusAndQueuePosition(Long tenantId, ConversationRouting.RoutingStatus status, Integer position);

    long countByTenantIdAndRoutingStatus(Long tenantId, ConversationRouting.RoutingStatus status);

    long countByTenantIdAndAssignedAgentIdAndRoutingStatus(Long tenantId, Long agentId, ConversationRouting.RoutingStatus status);
}
