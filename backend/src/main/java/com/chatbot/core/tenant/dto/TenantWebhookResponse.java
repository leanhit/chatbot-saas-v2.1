package com.chatbot.core.tenant.dto;

import com.chatbot.core.tenant.model.TenantWebhook;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Tenant webhook API response. The signing secret is NEVER exposed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantWebhookResponse {

    private Long id;
    private Long tenantId;
    private String url;
    private String events;
    private Boolean isActive;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TenantWebhookResponse from(TenantWebhook w) {
        if (w == null) return null;
        return TenantWebhookResponse.builder()
                .id(w.getId())
                .tenantId(w.getTenantId())
                .url(w.getUrl())
                .events(w.getEvents())
                .isActive(w.getIsActive())
                .createdBy(w.getCreatedBy())
                .updatedBy(w.getUpdatedBy())
                .createdAt(w.getCreatedAt())
                .updatedAt(w.getUpdatedAt())
                .build();
    }
}
