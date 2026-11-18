package com.chatbot.core.payment.gateway.dto;

import com.chatbot.core.payment.gateway.model.Webhook;
import lombok.Data;

import java.util.Set;

@Data
public class WebhookRequest {
    private String name;
    private String url;
    private String secret;
    private Boolean isActive;
    private Set<Webhook.WebhookEventType> eventTypes;
    private Integer retryCount;
    private Integer timeoutSeconds;
    private String description;
}
