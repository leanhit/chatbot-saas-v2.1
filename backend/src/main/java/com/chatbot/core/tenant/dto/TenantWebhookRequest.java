package com.chatbot.core.tenant.dto;

import lombok.Data;

@Data
public class TenantWebhookRequest {
    private String url;
    private String secret;
    private String events;
    private Boolean isActive;
}
