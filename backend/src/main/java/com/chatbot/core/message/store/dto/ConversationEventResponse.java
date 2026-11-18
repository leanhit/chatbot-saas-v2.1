package com.chatbot.core.message.store.dto;

import com.chatbot.core.message.store.model.ConversationEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationEventResponse {
    private Long id;
    private Long conversationId;
    private String eventType;
    private String actorType;
    private String actorId;
    private String fromValue;
    private String toValue;
    private String metadata;
    private LocalDateTime occurredAt;

    public static ConversationEventResponse from(ConversationEvent e) {
        if (e == null) return null;
        return ConversationEventResponse.builder()
                .id(e.getId()).conversationId(e.getConversationId())
                .eventType(e.getEventType()).actorType(e.getActorType()).actorId(e.getActorId())
                .fromValue(e.getFromValue()).toValue(e.getToValue()).metadata(e.getMetadata())
                .occurredAt(e.getOccurredAt())
                .build();
    }
}
