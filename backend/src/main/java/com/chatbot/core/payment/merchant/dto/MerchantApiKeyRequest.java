package com.chatbot.core.payment.merchant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MerchantApiKeyRequest {
    private Long tenantId;
    @NotBlank(message = "name is required")
    private String name;
    private String description;
    private String webhookUrl;
    private Boolean isActive;
    private Integer rateLimitPerMinute;
    private LocalDateTime expiresAt;
}
