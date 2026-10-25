package com.chatbot.core.penny.rules;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Import/Export Changelog Entity
 * Tracks all import/export operations for audit purposes
 */
@Entity
@Table(name = "import_export_changelog")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportExportChangelog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "bot_id", nullable = false)
    private UUID botId;
    
    @Column(name = "operation_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private OperationType operationType;
    
    @Column(name = "source_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private SourceType sourceType;
    
    @Column(name = "file_name")
    private String fileName;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "item_count")
    private Integer itemCount;
    
    @Column(name = "strategy")
    private String strategy;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OperationStatus status;
    
    @Column(name = "imported_count")
    private Integer importedCount;
    
    @Column(name = "updated_count")
    private Integer updatedCount;
    
    @Column(name = "skipped_count")
    private Integer skippedCount;
    
    @Column(name = "error_count")
    private Integer errorCount;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    @Column(name = "performed_by")
    private String performedBy;
    
    @Column(name = "performed_at", nullable = false)
    private Instant performedAt;
    
    @Column(name = "tenant_id")
    private Long tenantId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        performedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
    
    public enum OperationType {
        IMPORT,
        EXPORT,
        VALIDATE,
        DIFF
    }
    
    public enum SourceType {
        JSON,
        CSV,
        BOTPRESS
    }
    
    public enum OperationStatus {
        SUCCESS,
        FAILED,
        PARTIAL,
        DRY_RUN
    }
}
