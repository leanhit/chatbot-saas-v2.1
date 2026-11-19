package com.chatbot.core.payment.gateway.dto;

import com.chatbot.core.payment.gateway.model.Webhook;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class WebhookRequest {
    @NotBlank(message = "name is required")
    private String name;
    @NotBlank(message = "url is required")
    private String url;
    private String secret;
    private Boolean isActive;
    private Set<Webhook.WebhookEventType> eventTypes;
    private Integer retryCount;
    private Integer timeoutSeconds;
    private String description;
}
