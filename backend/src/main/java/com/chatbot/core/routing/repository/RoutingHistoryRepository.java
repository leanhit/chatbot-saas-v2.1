package com.chatbot.core.routing.repository;

import com.chatbot.core.routing.model.RoutingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoutingHistoryRepository extends JpaRepository<RoutingHistory, Long> {

    List<RoutingHistory> findByTenantId(Long tenantId);

    List<RoutingHistory> findByTenantIdAndConversationIdOrderByCreatedAtDesc(Long tenantId, Long conversationId);

    List<RoutingHistory> findByTenantIdAndAssignedAgentIdOrderByCreatedAtDesc(Long tenantId, Long assignedAgentId);
}
