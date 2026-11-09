package com.chatbot.core.message.store.repository;

import com.chatbot.core.message.store.model.ConversationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationEventRepository extends JpaRepository<ConversationEvent, Long> {

    List<ConversationEvent> findByConversationIdAndTenantIdOrderByOccurredAtAsc(Long conversationId, Long tenantId);

    List<ConversationEvent> findByTenantIdAndEventTypeOrderByOccurredAtDesc(Long tenantId, String eventType);
}
