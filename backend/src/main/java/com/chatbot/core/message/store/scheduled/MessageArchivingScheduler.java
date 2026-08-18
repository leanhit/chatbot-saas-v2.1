package com.chatbot.core.message.store.scheduled;

import com.chatbot.core.message.store.service.MessageArchivingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job for message archiving and partition management.
 * Runs daily to archive old messages and ensure future partitions exist.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MessageArchivingScheduler {

    private final MessageArchivingService messageArchivingService;

    @Value("${message.archive.retention-days:90}")
    private int retentionDays;

    /**
     * Archive old messages daily at 3 AM
     * Uses ShedLock to prevent concurrent execution across cluster nodes
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @SchedulerLock(
        name = "MessageArchivingScheduler_archiveOldMessages",
        lockAtMostFor = "2h",
        lockAtLeastFor = "10m"
    )
    public void archiveOldMessages() {
        log.info("🗄️ Starting scheduled message archiving job");
        
        try {
            int archivedCount = messageArchivingService.archiveOldMessages(retentionDays);
            log.info("✅ Scheduled archiving completed. Archived {} messages older than {} days", 
                    archivedCount, retentionDays);
            
            // Log archive statistics
            var stats = messageArchivingService.getArchiveStatistics();
            log.info("📊 Archive statistics: Main table: {}, Archive table: {}", 
                    stats.get("mainTableCount"), stats.get("archiveTableCount"));
            
        } catch (Exception e) {
            log.error("❌ Scheduled message archiving failed: {}", e.getMessage(), e);
            // Don't throw exception to allow next scheduled execution
        }
    }

    /**
     * Ensure future partitions exist weekly on Sundays at 4 AM
     * Uses ShedLock to prevent concurrent execution across cluster nodes
     */
    @Scheduled(cron = "0 0 4 ? * SUN")
    @SchedulerLock(
        name = "MessageArchivingScheduler_ensureFuturePartitions",
        lockAtMostFor = "30m",
        lockAtLeastFor = "5m"
    )
    public void ensureFuturePartitions() {
        log.info("🗂️ Starting scheduled partition management job");
        
        try {
            messageArchivingService.ensureFuturePartitions();
            log.info("✅ Scheduled partition management completed");
            
        } catch (Exception e) {
            log.error("❌ Scheduled partition management failed: {}", e.getMessage(), e);
            // Don't throw exception to allow next scheduled execution
        }
    }

    /**
     * Log archive statistics daily at 5 AM
     * Uses ShedLock to prevent concurrent execution across cluster nodes
     */
    @Scheduled(cron = "0 0 5 * * ?")
    @SchedulerLock(
        name = "MessageArchivingScheduler_logArchiveStatistics",
        lockAtMostFor = "10m",
        lockAtLeastFor = "1m"
    )
    public void logArchiveStatistics() {
        log.info("📊 Starting archive statistics logging");
        
        try {
            var stats = messageArchivingService.getArchiveStatistics();
            log.info("📊 Archive Statistics Report:");
            log.info("  - Main table messages: {}", stats.get("mainTableCount"));
            log.info("  - Archive table messages: {}", stats.get("archiveTableCount"));
            log.info("  - Oldest main message: {}", stats.get("oldestMainMessage"));
            log.info("  - Newest archive message: {}", stats.get("newestArchiveMessage"));
            log.info("  - Number of partitions: {}", stats.get("partitions") != null ? 
                    ((java.util.List<?>) stats.get("partitions")).size() : 0);
            
        } catch (Exception e) {
            log.error("❌ Failed to log archive statistics: {}", e.getMessage(), e);
        }
    }
}
