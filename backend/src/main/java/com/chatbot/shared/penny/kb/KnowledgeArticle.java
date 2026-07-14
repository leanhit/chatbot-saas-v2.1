package com.chatbot.shared.penny.kb;

import com.chatbot.shared.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity representing a knowledge base article.
 * Used by RAG pipeline to inject relevant context into LLM prompts.
 */
@Entity
@Table(name = "penny_knowledge_articles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeArticle {

    @Id
    @Column(name = "id")
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(name = "bot_id", nullable = false)
    private UUID botId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "category", length = 100)
    private String category; // faq, product, policy, shipping, price

    @Column(name = "tags", length = 500)
    private String tags; // comma-separated

    @Column(name = "embedding", columnDefinition = "VECTOR(1536)")
    private float[] embedding; // OpenAI text-embedding-3-small: 1536 dimensions

    @Column(name = "embedding_model", length = 100)
    private String embeddingModel; // e.g., "text-embedding-3-small"

    @Column(name = "source_url", length = 1000)
    private String sourceUrl;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 0;

    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "created_by")
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Format article as context snippet for LLM prompt injection.
     */
    public String toContextSnippet() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(title).append("]");
        if (category != null) {
            sb.append(" (").append(category).append(")");
        }
        sb.append(": ").append(content);
        return sb.toString();
    }
}
