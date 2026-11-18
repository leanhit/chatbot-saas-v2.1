package com.chatbot.core.tenant.dto;

import com.chatbot.core.tenant.model.TenantWebhookDelivery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantWebhookDeliveryResponse {
    private Long id;
    private Long tenantId;
    private Long webhookId;
    private String eventType;
    private Long conversationId;
    private String deliveryKey;
    private String payload;
    private String status;
    private Integer attempts;
    private LocalDateTime nextRetryAt;
    private String lastError;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TenantWebhookDeliveryResponse from(TenantWebhookDelivery d) {
        if (d == null) return null;
        return TenantWebhookDeliveryResponse.builder()
                .id(d.getId()).tenantId(d.getTenantId()).webhookId(d.getWebhookId())
                .eventType(d.getEventType()).conversationId(d.getConversationId())
                .deliveryKey(d.getDeliveryKey()).payload(d.getPayload())
                .status(d.getStatus() != null ? d.getStatus().name() : null)
                .attempts(d.getAttempts()).nextRetryAt(d.getNextRetryAt())
                .lastError(d.getLastError()).deliveredAt(d.getDeliveredAt())
                .createdAt(d.getCreatedAt()).updatedAt(d.getUpdatedAt())
                .build();
    }
}
