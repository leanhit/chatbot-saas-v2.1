package com.chatbot.core.tenant.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.chatbot.shared.utils.DateUtils;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Failed webhook delivery queued for retry (DLQ-lite).
 * status: PENDING → DELIVERED | FAILED (after max attempts).
 */
@Entity
@Table(name = "tenant_webhook_deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantWebhookDelivery {

    public enum Status { PENDING, DELIVERED, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "webhook_id", nullable = false)
    private Long webhookId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "conversation_id")
    private Long conversationId;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Builder.Default
    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0;

    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    @Column(name = "next_retry_at", nullable = false)
    private LocalDateTime nextRetryAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @CreationTimestamp
    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
