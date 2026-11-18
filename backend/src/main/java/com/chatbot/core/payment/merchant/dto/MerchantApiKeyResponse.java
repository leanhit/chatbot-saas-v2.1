package com.chatbot.core.payment.merchant.dto;

import com.chatbot.core.payment.merchant.model.MerchantApiKey;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API response for merchant API keys. Secrets are NEVER included by default;
 * they are only populated on create/regenerate responses (show-once contract).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantApiKeyResponse {

    private Long id;
    private Long tenantId;
    private String apiKey;
    private String name;
    private String description;
    private String webhookUrl;
    private Boolean isActive;
    private Integer rateLimitPerMinute;
    private LocalDateTime lastUsedAt;
    private Long usageCount;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Show-once secrets (create / regenerate only)
    private String apiSecret;
    private String webhookSecret;

    public static MerchantApiKeyResponse from(MerchantApiKey key) {
        return from(key, false);
    }

    public static MerchantApiKeyResponse from(MerchantApiKey key, boolean includeSecrets) {
        if (key == null) return null;
        MerchantApiKeyResponseBuilder b = MerchantApiKeyResponse.builder()
                .id(key.getId())
                .tenantId(key.getTenantId())
                .apiKey(key.getApiKey())
                .name(key.getName())
                .description(key.getDescription())
                .webhookUrl(key.getWebhookUrl())
                .isActive(key.getIsActive())
                .rateLimitPerMinute(key.getRateLimitPerMinute())
                .lastUsedAt(key.getLastUsedAt())
                .usageCount(key.getUsageCount())
                .expiresAt(key.getExpiresAt())
                .createdAt(key.getCreatedAt())
                .updatedAt(key.getUpdatedAt());
        if (includeSecrets) {
            b.apiSecret(key.getApiSecret())
             .webhookSecret(key.getWebhookSecret());
        }
        return b.build();
    }
}
