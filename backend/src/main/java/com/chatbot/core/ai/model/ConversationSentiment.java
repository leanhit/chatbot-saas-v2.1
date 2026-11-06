package com.chatbot.core.ai.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Sentiment score for a conversation or individual message.
 * Display/alert only — never triggers automatic escalation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "conversation_sentiment", indexes = {
    @Index(name = "idx_sentiment_tenant", columnList = "tenant_id"),
    @Index(name = "idx_sentiment_conversation", columnList = "conversation_id"),
    @Index(name = "idx_sentiment_conversation_created", columnList = "conversation_id, created_at")
})
@EqualsAndHashCode(callSuper = true)
public class ConversationSentiment extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "score", nullable = false)
    private Double score;

    @Enumerated(EnumType.STRING)
    @Column(name = "label", nullable = false, length = 20)
    private SentimentLabel label;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private SentimentSource source;

    @Override
    public Long getId() {
        return id;
    }
}
