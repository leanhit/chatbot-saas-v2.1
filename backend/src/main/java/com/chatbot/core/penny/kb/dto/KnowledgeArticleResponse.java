package com.chatbot.core.penny.kb.dto;

import com.chatbot.core.penny.kb.KnowledgeArticle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Knowledge article API response. The embedding vector is NEVER exposed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeArticleResponse {

    private UUID id;
    private UUID botId;
    private Long tenantId;
    private String title;
    private String content;
    private String category;
    private String tags;
    private String embeddingModel;
    private String sourceUrl;
    private boolean isActive;
    private Integer priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;

    public static KnowledgeArticleResponse from(KnowledgeArticle a) {
        if (a == null) return null;
        return KnowledgeArticleResponse.builder()
                .id(a.getId())
                .botId(a.getBotId())
                .tenantId(a.getTenantId())
                .title(a.getTitle())
                .content(a.getContent())
                .category(a.getCategory())
                .tags(a.getTags())
                .embeddingModel(a.getEmbeddingModel())
                .sourceUrl(a.getSourceUrl())
                .isActive(a.isActive())
                .priority(a.getPriority())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .createdBy(a.getCreatedBy())
                .build();
    }
}
