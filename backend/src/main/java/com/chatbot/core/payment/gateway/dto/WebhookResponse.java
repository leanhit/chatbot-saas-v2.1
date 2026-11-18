package com.chatbot.core.payment.gateway.dto;

import com.chatbot.core.payment.gateway.model.Webhook;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Webhook API response. The signing secret is NEVER exposed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookResponse {

    private Long id;
    private String name;
    private String url;
    private Boolean isActive;
    private Set<String> eventTypes;
    private Integer retryCount;
    private Integer currentRetryAttempt;
    private Integer timeoutSeconds;
    private LocalDateTime nextRetryAt;
    private String lastError;
    private String status;
    private LocalDateTime lastTriggeredAt;
    private Integer successCount;
    private Integer failureCount;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static WebhookResponse from(Webhook w) {
        if (w == null) return null;
        Set<String> events = w.getEventTypes() == null ? null
                : w.getEventTypes().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet());
        return WebhookResponse.builder()
                .id(w.getId())
                .name(w.getName())
                .url(w.getUrl())
                .isActive(w.getIsActive())
                .eventTypes(events)
                .retryCount(w.getRetryCount())
                .currentRetryAttempt(w.getCurrentRetryAttempt())
                .timeoutSeconds(w.getTimeoutSeconds())
                .nextRetryAt(w.getNextRetryAt())
                .lastError(w.getLastError())
                .status(w.getStatus())
                .lastTriggeredAt(w.getLastTriggeredAt())
                .successCount(w.getSuccessCount())
                .failureCount(w.getFailureCount())
                .description(w.getDescription())
                .createdAt(w.getCreatedAt())
                .updatedAt(w.getUpdatedAt())
                .build();
    }
}
