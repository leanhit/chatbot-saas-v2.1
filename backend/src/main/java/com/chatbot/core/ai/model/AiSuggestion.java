package com.chatbot.core.ai.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AI-generated suggestion for agent assistance.
 * Display-only: never auto-applied to conversations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ai_suggestions", indexes = {
    @Index(name = "idx_ai_suggestion_tenant", columnList = "tenant_id"),
    @Index(name = "idx_ai_suggestion_conversation", columnList = "conversation_id"),
    @Index(name = "idx_ai_suggestion_tenant_status", columnList = "tenant_id, status")
})
@EqualsAndHashCode(callSuper = true)
public class AiSuggestion extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Column(name = "agent_id")
    private Long agentId;

    @Column(name = "suggestion_text", nullable = false, columnDefinition = "TEXT")
    private String suggestionText;

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "provider", length = 50)
    private String provider;

    @Column(name = "model", length = 100)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AiSuggestionStatus status;

    @Column(name = "response_note", columnDefinition = "TEXT")
    private String responseNote;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Override
    public Long getId() {
        return id;
    }
}
