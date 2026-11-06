package com.chatbot.core.analytics.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Map;

/**
 * Daily aggregated analytics summary per tenant.
 * Populated by scheduled aggregation job from conversations/messages/escalations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "analytics_daily_summary", indexes = {
    @Index(name = "idx_analytics_daily_tenant_date", columnList = "tenant_id, summary_date")
})
@EqualsAndHashCode(callSuper = true)
public class AnalyticsDailySummary extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "summary_date", nullable = false)
    private LocalDate summaryDate;

    @Column(name = "total_conversations", nullable = false)
    private Integer totalConversations;

    @Column(name = "new_conversations", nullable = false)
    private Integer newConversations;

    @Column(name = "closed_conversations", nullable = false)
    private Integer closedConversations;

    @Column(name = "total_messages", nullable = false)
    private Integer totalMessages;

    @Column(name = "user_messages", nullable = false)
    private Integer userMessages;

    @Column(name = "bot_messages", nullable = false)
    private Integer botMessages;

    @Column(name = "agent_messages", nullable = false)
    private Integer agentMessages;

    @Column(name = "escalations_count", nullable = false)
    private Integer escalationsCount;

    @Column(name = "avg_first_response_minutes")
    private Double avgFirstResponseMinutes;

    @Column(name = "avg_resolution_minutes")
    private Double avgResolutionMinutes;

    @Column(name = "avg_sentiment_score")
    private Double avgSentimentScore;

    @Column(name = "negative_sentiment_count", nullable = false)
    private Integer negativeSentimentCount;

    @Column(name = "ai_suggestions_count", nullable = false)
    private Integer aiSuggestionsCount;

    @Column(name = "ai_suggestions_accepted", nullable = false)
    private Integer aiSuggestionsAccepted;

    @Column(name = "ai_suggestions_rejected", nullable = false)
    private Integer aiSuggestionsRejected;

    @Column(name = "metrics")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metrics;

    @Override
    public Long getId() {
        return id;
    }
}
