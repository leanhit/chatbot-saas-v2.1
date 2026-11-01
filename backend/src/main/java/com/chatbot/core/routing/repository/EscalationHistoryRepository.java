package com.chatbot.core.routing.repository;

import com.chatbot.core.routing.model.EscalationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscalationHistoryRepository extends JpaRepository<EscalationHistory, Long> {

    List<EscalationHistory> findByTenantId(Long tenantId);

    List<EscalationHistory> findByTenantIdAndConversationId(Long tenantId, Long conversationId);

    List<EscalationHistory> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
}
