package com.chatbot.shared.penny.model;

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
 * Entity representing a chunk of text from a knowledge document with embedding
 */
@Entity
@Table(name = "penny_knowledge_chunks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PennyKnowledgeChunk {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "bot_id", nullable = false)
    private UUID botId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(name = "chunk_text", nullable = false, columnDefinition = "TEXT")
    private String chunkText;

    @Column(name = "chunk_tokens")
    @Builder.Default
    private Integer chunkTokens = 0;

    @Column(name = "embedding")
    private String embedding; // VECTOR(1536) stored as string for JPA compatibility

    @Column(name = "embedding_model")
    private String embeddingModel;

    @Column(name = "page_number")
    private Integer pageNumber;

    @Column(name = "sheet_name")
    private String sheetName;

    @Column(name = "row_index")
    private Integer rowIndex;

    @Column(name = "metadata")
    private String metadata; // JSON string

    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
