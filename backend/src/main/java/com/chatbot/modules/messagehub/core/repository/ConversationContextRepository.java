package com.chatbot.modules.messagehub.core.repository;

import com.chatbot.modules.messagehub.core.model.ConversationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Conversation context repository for Message Hub MVP
 */
@Repository
public interface ConversationContextRepository extends JpaRepository<ConversationContext, UUID> {

    /**
     * Find context by conversation ID
     */
    Optional<ConversationContext> findByConversationId(String conversationId);

    /**
     * Find context by user and tenant
     */
    Optional<ConversationContext> findByUserIdAndTenantId(UUID userId, UUID tenantId);
}
