package com.chatbot.shared.penny.analytics;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Analytics Event Entity - Persisted analytics events
 */
@Entity
@Table(name = "penny_analytics_events", indexes = {
    @Index(name = "idx_bot_id", columnList = "bot_id"),
    @Index(name = "idx_event_type", columnList = "event_type"),
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_bot_timestamp", columnList = "bot_id, timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "bot_id", nullable = false)
    private UUID botId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "data")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> data;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "platform")
    private String platform;

    @Column(name = "intent")
    private String intent;

    @Column(name = "provider_used")
    private String providerUsed;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Column(name = "has_error")
    private Boolean hasError;

    @Column(name = "error_type")
    private String errorType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}
