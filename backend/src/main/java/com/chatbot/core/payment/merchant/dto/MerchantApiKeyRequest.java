package com.chatbot.core.payment.merchant.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MerchantApiKeyRequest {
    private Long tenantId;
    private String name;
    private String description;
    private String webhookUrl;
    private Boolean isActive;
    private Integer rateLimitPerMinute;
    private LocalDateTime expiresAt;
}
