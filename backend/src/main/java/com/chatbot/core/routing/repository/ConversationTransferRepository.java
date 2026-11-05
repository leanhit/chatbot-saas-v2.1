package com.chatbot.core.routing.repository;

import com.chatbot.core.routing.model.ConversationTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationTransferRepository extends JpaRepository<ConversationTransfer, Long> {

    List<ConversationTransfer> findByTenantId(Long tenantId);

    List<ConversationTransfer> findByTenantIdAndConversationIdOrderByCreatedAtDesc(Long tenantId, Long conversationId);

    List<ConversationTransfer> findByTenantIdAndFromAgentIdOrderByCreatedAtDesc(Long tenantId, Long fromAgentId);

    List<ConversationTransfer> findByTenantIdAndToAgentIdOrderByCreatedAtDesc(Long tenantId, Long toAgentId);

    List<ConversationTransfer> findByTenantIdAndStatusOrderByCreatedAtDesc(Long tenantId, String status);

    Optional<ConversationTransfer> findByTenantIdAndConversationIdAndStatus(Long tenantId, Long conversationId, String status);

    long countByTenantIdAndStatus(Long tenantId, String status);
}
