package com.chatbot.core.penny.rules;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Dead letter queue for failed outbound webhook calls from bot rules.
 * Stores payloads that exhausted all retry attempts for manual inspection/replay.
 */
@Entity
@Table(name = "bot_webhook_dead_letters",
       indexes = {
           @Index(name = "idx_bot_webhook_dlq_status", columnList = "status"),
           @Index(name = "idx_bot_webhook_dlq_bot", columnList = "bot_id")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotWebhookDeadLetter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;

    @Column(name = "bot_id", nullable = false)
    private UUID botId;

    @Column(name = "rule_name", length = 200)
    private String ruleName;

    @Column(name = "webhook_url", nullable = false, length = 500)
    private String webhookUrl;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "attempts", nullable = false)
    private Integer attempts;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING"; // PENDING, PROCESSED, DISCARDED

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
