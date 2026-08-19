package com.chatbot.spokes.facebook.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kafka event published when a Facebook connection is created
 * Other spokes can consume this event to react to new connections
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacebookConnectionCreatedEvent {
    private UUID connectionId;
    private Long tenantId;
    private String botId;
    private String botName;
    private String pageId;
    private String ownerId;
    private LocalDateTime createdAt;
    private final String eventType = "FACEBOOK_CONNECTION_CREATED";
}
