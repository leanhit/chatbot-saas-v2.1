package com.chatbot.spokes.facebook.webhook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for passing Facebook webhook events through Kafka.
 * Partitioned by senderId to guarantee order.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacebookKafkaEvent {
    private Long tenantId;
    private String pageId;
    private String senderId;
    private WebhookRequest.Messaging messaging;
}
