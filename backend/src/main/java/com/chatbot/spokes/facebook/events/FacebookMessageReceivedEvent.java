package com.chatbot.spokes.facebook.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka event published when a Facebook message is received
 * Core domain and other spokes can consume this event
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacebookMessageReceivedEvent {
    private String messageId;
    private String senderId;
    private String recipientId;
    private String messageText;
    private String messageType;
    private Map<String, Object> attachments;
    private LocalDateTime timestamp;
    private UUID connectionId;
    private Long tenantId;
    private final String eventType = "FACEBOOK_MESSAGE_RECEIVED";
}
