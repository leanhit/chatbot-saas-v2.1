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
 * Entity representing a knowledge document uploaded for RAG
 */
@Entity
@Table(name = "penny_knowledge_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PennyKnowledgeDocument {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "bot_id", nullable = false)
    private UUID botId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "document_name", nullable = false)
    private String documentName;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type", nullable = false)
    private String fileType; // PDF, DOCX, XLSX

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "status")
    @Builder.Default
    private String status = "PROCESSING"; // PROCESSING, COMPLETED, FAILED

    @Column(name = "total_pages")
    @Builder.Default
    private Integer totalPages = 0;

    @Column(name = "total_chunks")
    @Builder.Default
    private Integer totalChunks = 0;

    @Column(name = "metadata")
    private String metadata; // JSON string

    @Column(name = "uploaded_by")
    private String uploadedBy;

    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
