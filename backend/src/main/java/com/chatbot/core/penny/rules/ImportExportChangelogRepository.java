package com.chatbot.core.penny.rules;

import com.chatbot.core.penny.rules.ImportExportChangelog.OperationType;
import com.chatbot.core.penny.rules.ImportExportChangelog.OperationStatus;
import com.chatbot.core.penny.rules.ImportExportChangelog.SourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Import/Export Changelog
 */
@Repository
public interface ImportExportChangelogRepository extends JpaRepository<ImportExportChangelog, UUID> {
    
    /**
     * Find changelogs by bot ID
     */
    List<ImportExportChangelog> findByBotIdOrderByPerformedAtDesc(UUID botId);
    
    /**
     * Find changelogs by operation type
     */
    List<ImportExportChangelog> findByOperationTypeOrderByPerformedAtDesc(OperationType operationType);
    
    /**
     * Find changelogs by bot ID and operation type
     */
    List<ImportExportChangelog> findByBotIdAndOperationTypeOrderByPerformedAtDesc(UUID botId, OperationType operationType);
    
    /**
     * Find changelogs by tenant ID
     */
    List<ImportExportChangelog> findByTenantIdOrderByPerformedAtDesc(Long tenantId);
    
    /**
     * Find changelogs by status
     */
    List<ImportExportChangelog> findByStatusOrderByPerformedAtDesc(OperationStatus status);
    
    /**
     * Find changelogs by bot ID and date range
     */
    List<ImportExportChangelog> findByBotIdAndPerformedAtBetweenOrderByPerformedAtDesc(
        UUID botId, Instant startDate, Instant endDate);
    
    /**
     * Count changelogs by bot ID
     */
    long countByBotId(UUID botId);
    
    /**
     * Count changelogs by operation type and status
     */
    long countByOperationTypeAndStatus(OperationType operationType, OperationStatus status);
}
