package com.chatbot.spokes.facebook.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kafka event published when a Facebook connection is updated
 * Other spokes can consume this event to react to connection changes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacebookConnectionUpdatedEvent {
    private UUID connectionId;
    private Long tenantId;
    private String botId;
    private String pageId;
    private boolean isActive;
    private boolean isEnabled;
    private LocalDateTime updatedAt;
    private final String eventType = "FACEBOOK_CONNECTION_UPDATED";
}
