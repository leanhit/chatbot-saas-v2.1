package com.chatbot.core.message.store.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for archiving old messages to improve database performance.
 * Moves messages older than retention period to archive table.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageArchivingService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Archive messages older than the specified number of days
     * @param daysToKeep Number of days to keep messages in main table
     * @return Number of messages archived
     */
    @Transactional(transactionManager = "messageTransactionManager", rollbackFor = Exception.class)
    public int archiveOldMessages(int daysToKeep) {
        log.info("Starting message archiving for messages older than {} days", daysToKeep);
        
        try {
            // Call the PostgreSQL function to archive old messages
            Integer archivedCount = jdbcTemplate.queryForObject(
                "SELECT archive_old_messages(?)",
                Integer.class,
                daysToKeep
            );
            
            int count = archivedCount != null ? archivedCount : 0;
            log.info("Successfully archived {} messages older than {} days", count, daysToKeep);
            return count;
            
        } catch (Exception e) {
            log.error("Error during message archiving: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to archive messages", e);
        }
    }

    /**
     * Ensure future partitions exist for the messages table
     * This should be called periodically (e.g., monthly)
     */
    public void ensureFuturePartitions() {
        log.info("Ensuring future partitions exist for messages table");
        
        try {
            jdbcTemplate.execute("SELECT ensure_future_partitions()");
            log.info("Successfully ensured future partitions");
        } catch (Exception e) {
            log.error("Error ensuring future partitions: {}", e.getMessage(), e);
            // Don't throw exception - this is a maintenance operation
        }
    }

    /**
     * Get statistics about archived messages
     * @return Map with archive statistics
     */
    public java.util.Map<String, Object> getArchiveStatistics() {
        log.info("Retrieving archive statistics");
        
        try {
            java.util.Map<String, Object> stats = new java.util.HashMap<>();
            
            // Count messages in main table
            Integer mainCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM messages",
                Integer.class
            );
            stats.put("mainTableCount", mainCount != null ? mainCount : 0);
            
            // Count messages in archive table
            Integer archiveCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM messages_archive",
                Integer.class
            );
            stats.put("archiveTableCount", archiveCount != null ? archiveCount : 0);
            
            // Get oldest message date in main table
            LocalDateTime oldestMain = jdbcTemplate.queryForObject(
                "SELECT MIN(created_at) FROM messages",
                LocalDateTime.class
            );
            stats.put("oldestMainMessage", oldestMain);
            
            // Get newest message date in archive table
            LocalDateTime newestArchive = jdbcTemplate.queryForObject(
                "SELECT MAX(created_at) FROM messages_archive",
                LocalDateTime.class
            );
            stats.put("newestArchiveMessage", newestArchive);
            
            // Get partition information
            java.util.List<java.util.Map<String, Object>> partitions = jdbcTemplate.queryForList(
                "SELECT schemaname, tablename, partitionname " +
                "FROM pg_partitions " +
                "WHERE tablename = 'messages' " +
                "ORDER BY partitionname"
            );
            stats.put("partitions", partitions);
            
            log.info("Archive statistics: {}", stats);
            return stats;
            
        } catch (Exception e) {
            log.error("Error retrieving archive statistics: {}", e.getMessage(), e);
            return java.util.Collections.emptyMap();
        }
    }

    /**
     * Restore messages from archive for a specific conversation
     * This is useful when a conversation needs to be re-opened
     * @param conversationId The conversation ID
     * @return Number of messages restored
     */
    @Transactional(transactionManager = "messageTransactionManager", rollbackFor = Exception.class)
    public int restoreMessagesForConversation(Long conversationId) {
        log.info("Restoring archived messages for conversation {}", conversationId);
        
        try {
            // Move messages from archive back to main table for specific conversation
            int restoredCount = jdbcTemplate.update(
                "INSERT INTO messages (id, tenant_id, conversation_id, sender, content, raw_payload, " +
                "message_type, external_message_id, is_read, sent_time, created_at) " +
                "SELECT id, tenant_id, conversation_id, sender, content, raw_payload, " +
                "message_type, external_message_id, is_read, sent_time, created_at " +
                "FROM messages_archive " +
                "WHERE conversation_id = ? " +
                "ON CONFLICT (id, created_at) DO NOTHING",
                conversationId
            );
            
            // Delete restored messages from archive
            jdbcTemplate.update(
                "DELETE FROM messages_archive WHERE conversation_id = ?",
                conversationId
            );
            
            log.info("Successfully restored {} messages for conversation {}", restoredCount, conversationId);
            return restoredCount;
            
        } catch (Exception e) {
            log.error("Error restoring messages for conversation {}: {}", conversationId, e.getMessage(), e);
            throw new RuntimeException("Failed to restore messages", e);
        }
    }
}
