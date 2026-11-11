package com.chatbot.core.message.store.service;

import com.chatbot.core.message.store.model.ConversationEvent;
import com.chatbot.core.message.store.repository.ConversationEventRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.shared.exceptions.BaseException;
import com.chatbot.shared.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Records and queries conversation lifecycle events.
 * Recording is best-effort: failures are logged, never break the main flow.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationEventService {

    public static final String CONVERSATION_CREATED = "conversation.created";
    public static final String CONVERSATION_CLOSED = "conversation.closed";
    public static final String CONVERSATION_TAKEN_OVER = "conversation.taken_over";
    public static final String CONVERSATION_RELEASED = "conversation.released";
    public static final String CONVERSATION_ASSIGNED = "conversation.assigned";
    public static final String CONVERSATION_REASSIGNED = "conversation.reassigned";
    public static final String TRANSFER_CREATED = "transfer.created";
    public static final String TRANSFER_ACCEPTED = "transfer.accepted";
    public static final String TRANSFER_REJECTED = "transfer.rejected";
    public static final String TRANSFER_COMPLETED = "transfer.completed";
    public static final String TRANSFER_CANCELLED = "transfer.cancelled";
    public static final String ESCALATION_TRIGGERED = "escalation.triggered";

    public static final String ACTOR_SYSTEM = "SYSTEM";
    public static final String ACTOR_AGENT = "AGENT";
    public static final String ACTOR_USER = "USER";
    public static final String ACTOR_BOT = "BOT";

    private final ConversationEventRepository eventRepository;
    private final ConversationWebhookDispatcher webhookDispatcher;

    @Transactional(transactionManager = "messageTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void record(Long conversationId, String eventType, String actorType, String actorId,
                       String fromValue, String toValue, String metadata) {
        try {
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null || conversationId == null) {
                return;
            }
            ConversationEvent event = ConversationEvent.builder()
                    .conversationId(conversationId)
                    .eventType(eventType)
                    .actorType(actorType)
                    .actorId(actorId)
                    .fromValue(fromValue)
                    .toValue(toValue)
                    .metadata(metadata)
                    .occurredAt(LocalDateTime.now())
                    .build();
            event.setTenantId(tenantId);
            eventRepository.save(event);

            // Best-effort outbound webhook dispatch (async, never breaks flow)
            Map<String, Object> payload = new HashMap<>();
            payload.put("actorType", actorType);
            payload.put("actorId", actorId);
            payload.put("from", fromValue);
            payload.put("to", toValue);
            payload.put("metadata", metadata);
            webhookDispatcher.dispatch(tenantId, eventType, conversationId, payload);
        } catch (Exception e) {
            log.warn("Failed to record conversation event {} for conversation {}: {}", eventType, conversationId, e.getMessage());
        }
    }

    @Transactional(readOnly = true, transactionManager = "messageTransactionManager")
    public List<ConversationEvent> getEvents(Long conversationId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BaseException(ErrorCode.TENANT_CONTEXT_MISSING, "Tenant context not set");
        }
        return eventRepository.findByConversationIdAndTenantIdOrderByOccurredAtAsc(conversationId, tenantId);
    }
}
