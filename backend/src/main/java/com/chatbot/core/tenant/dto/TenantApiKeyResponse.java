package com.chatbot.core.tenant.dto;

import com.chatbot.core.tenant.model.TenantApiKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Tenant API key API response. The key hash is NEVER exposed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantApiKeyResponse {

    private Long id;
    private Long tenantId;
    private String name;
    private String keyPrefix;
    private String role;
    private Boolean isActive;
    private LocalDateTime lastUsedAt;
    private Long usageCount;
    private LocalDateTime expiresAt;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TenantApiKeyResponse from(TenantApiKey k) {
        if (k == null) return null;
        return TenantApiKeyResponse.builder()
                .id(k.getId())
                .tenantId(k.getTenantId())
                .name(k.getName())
                .keyPrefix(k.getKeyPrefix())
                .role(k.getRole() != null ? k.getRole().name() : null)
                .isActive(k.getIsActive())
                .lastUsedAt(k.getLastUsedAt())
                .usageCount(k.getUsageCount())
                .expiresAt(k.getExpiresAt())
                .createdBy(k.getCreatedBy())
                .createdAt(k.getCreatedAt())
                .updatedAt(k.getUpdatedAt())
                .build();
    }
}
