package com.chatbot.core.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TenantWebhookRequest {
    @NotBlank(message = "url is required")
    private String url;
    private String secret;
    private String events;
    private Boolean isActive;
}
