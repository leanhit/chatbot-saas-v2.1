package com.chatbot.modules.messagehub.core.dto;

import lombok.*;

import java.util.UUID;

/**
 * Message request DTO for Message Hub MVP
 * Standard message format from adapters to Message Gateway
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageRequest {

    private String conversationId;
    private UUID userId;
    private UUID tenantId;
    private String message;
    private String senderType; // bot, human, user
    private String channel; // facebook, web, etc.
    private String intent; // ask_price, greeting, etc.

    // Validation helpers
    public boolean isValid() {
        return conversationId != null && !conversationId.trim().isEmpty()
                && userId != null
                && tenantId != null
                && message != null && !message.trim().isEmpty();
    }
}
