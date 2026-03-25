package com.chatbot.core.message.decision.service;
import lombok.extern.slf4j.Slf4j;

import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.service.ConversationService;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.service.MessageService;
import com.chatbot.core.tenant.infra.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TakeoverCleanupService {

    private final ConversationRepository conversationRepository;
    private final ConversationService conversationService;
    private final MessageService messageService;
    
    private final ExecutorService cleanupExecutor = Executors.newFixedThreadPool(5);

    // Configuration constants
    private static final long IDLE_TIMEOUT_MS = 2 * 60 * 1000; // 2 minutes
    private static final long SCHEDULE_INTERVAL_MS = 30 * 1000; // 30 seconds
    private static final long WARNING_TIMEOUT_MS = 90 * 1000; // 1.5 minutes (warning before cleanup)
    
    /**
     * Scheduled cleanup of idle conversations
     */
    @Scheduled(fixedRate = SCHEDULE_INTERVAL_MS)
    public void autoReleaseIdleConversations() {
        log.info("⏰ [TakeoverCleanup] Starting idle conversation cleanup check...");
        
        try {
            // Get all taken over conversations across all tenants
            List<Conversation> takenOverConversations = conversationRepository.findAllByIsTakenOverByAgent(true);
            
            if (takenOverConversations.isEmpty()) {
                log.debug("📊 [TakeoverCleanup] No active takeovers found");
                return;
            }
            
            long currentTimeMillis = System.currentTimeMillis();
            CleanupStats stats = new CleanupStats();
            
            for (Conversation conversation : takenOverConversations) {
                stats.totalProcessed++;
                
                try {
                    // Calculate idle duration
                    long idleDuration = calculateIdleDuration(conversation, currentTimeMillis);
                    
                    if (idleDuration >= IDLE_TIMEOUT_MS) {
                        handleIdleConversation(conversation, idleDuration, stats);
                    } else if (idleDuration >= WARNING_TIMEOUT_MS) {
                        handleWarningConversation(conversation, idleDuration, stats);
                    }
                    
                } catch (Exception e) {
                    log.error("❌ [TakeoverCleanup] Error processing conversation {}: {}", 
                             conversation.getId(), e.getMessage());
                    stats.errors++;
                }
            }
            
            log.info("✅ [TakeoverCleanup] Cleanup completed: {}", stats.getSummary());
            
        } catch (Exception e) {
            log.error("❌ [TakeoverCleanup] Critical error in cleanup process", e);
        }
    }
    
    /**
     * Manual cleanup for specific conversation
     */
    public CompletableFuture<CleanupResult> cleanupConversation(Long conversationId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("🔧 [TakeoverCleanup] Manual cleanup requested for conversation: {}", conversationId);
                
                Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found: " + conversationId));
                
                if (!conversation.getIsTakenOverByAgent()) {
                    return CleanupResult.builder()
                            .conversationId(conversationId)
                            .success(false)
                            .reason("Conversation is not currently taken over")
                            .build();
                }
                
                long idleDuration = calculateIdleDuration(conversation, System.currentTimeMillis());
                
                if (idleDuration < IDLE_TIMEOUT_MS) {
                    return CleanupResult.builder()
                            .conversationId(conversationId)
                            .success(false)
                            .reason("Conversation not idle enough (idle: " + (idleDuration/1000) + "s)")
                            .build();
                }
                
                // Perform cleanup
                boolean success = performCleanup(conversation, idleDuration);
                
                return CleanupResult.builder()
                        .conversationId(conversationId)
                        .success(success)
                        .idleDuration(idleDuration)
                        .reason(success ? "Successfully released" : "Failed to release")
                        .build();
                
            } catch (Exception e) {
                log.error("❌ [TakeoverCleanup] Manual cleanup failed for conversation: {}", conversationId, e);
                return CleanupResult.builder()
                        .conversationId(conversationId)
                        .success(false)
                        .reason("Error: " + e.getMessage())
                        .build();
            }
        }, cleanupExecutor);
    }
    
    /**
     * Get cleanup statistics
     */
    public CleanupStats getCleanupStats() {
        List<Conversation> takenOverConversations = conversationRepository.findAllByIsTakenOverByAgent(true);
        long currentTime = System.currentTimeMillis();
        
        CleanupStats stats = new CleanupStats();
        stats.totalProcessed = takenOverConversations.size();
        
        for (Conversation conversation : takenOverConversations) {
            long idleDuration = calculateIdleDuration(conversation, currentTime);
            
            if (idleDuration >= IDLE_TIMEOUT_MS) {
                stats.needsCleanup++;
            } else if (idleDuration >= WARNING_TIMEOUT_MS) {
                stats.needsWarning++;
            } else {
                stats.active++;
            }
        }
        
        return stats;
    }
    
    /**
     * Calculate idle duration for conversation
     */
    private long calculateIdleDuration(Conversation conversation, long currentTimeMillis) {
        if (conversation.getUpdatedAt() == null) {
            log.warn("⚠️ [TakeoverCleanup] Conversation {} has null updatedAt", conversation.getId());
            return 0;
        }
        
        return conversation.getUpdatedAt()
                .atZone(ZoneOffset.ofHours(7)) // Vietnam timezone
                .toInstant()
                .toEpochMilli();
    }
    
    /**
     * Handle idle conversation that needs cleanup
     */
    private void handleIdleConversation(Conversation conversation, long idleDuration, CleanupStats stats) {
        log.info("🚨 [TakeoverCleanup] Auto-release: Conversation {} (Tenant: {}) idle for {}s", 
                 conversation.getId(), conversation.getTenantId(), idleDuration / 1000);
        
        try {
            boolean success = performCleanup(conversation, idleDuration);
            
            if (success) {
                stats.released++;
                log.info("✅ [TakeoverCleanup] Conversation {} successfully released", conversation.getId());
            } else {
                stats.errors++;
                log.error("❌ [TakeoverCleanup] Failed to release conversation {}", conversation.getId());
            }
            
        } catch (Exception e) {
            stats.errors++;
            log.error("❌ [TakeoverCleanup] Error releasing conversation {}: {}", 
                     conversation.getId(), e.getMessage());
        }
    }
    
    /**
     * Handle conversation that needs warning
     */
    private void handleWarningConversation(Conversation conversation, long idleDuration, CleanupStats stats) {
        log.warn("⚠️ [TakeoverCleanup] Warning: Conversation {} (Tenant: {}) idle for {}s", 
                conversation.getId(), conversation.getTenantId(), idleDuration / 1000);
        
        try {
            // Send warning message to conversation
            sendWarningMessage(conversation, idleDuration);
            stats.warnings++;
            
        } catch (Exception e) {
            log.error("❌ [TakeoverCleanup] Failed to send warning for conversation {}: {}", 
                     conversation.getId(), e.getMessage());
            stats.errors++;
        }
    }
    
    /**
     * Perform actual cleanup of conversation
     */
    private boolean performCleanup(Conversation conversation, long idleDuration) {
        try {
            // Set tenant context for proper multi-tenant handling
            TenantContext.setTenantId(conversation.getTenantId());
            
            // Release conversation
            conversationService.releaseConversation(conversation.getId());
            
            // Send system notification
            sendCleanupNotification(conversation, idleDuration);
            
            return true;
            
        } catch (Exception e) {
            log.error("❌ [TakeoverCleanup] Cleanup failed for conversation {}", conversation.getId(), e);
            return false;
        } finally {
            TenantContext.clear();
        }
    }
    
    /**
     * Send warning message to conversation
     */
    private void sendWarningMessage(Conversation conversation, long idleDuration) {
        try {
            String warningMessage = String.format(
                "⚠️ This conversation has been idle for %d minutes and will be returned to the bot soon if no activity occurs.",
                idleDuration / 60000
            );
            
            messageService.saveMessage(
                conversation.getId(),
                "system",
                warningMessage,
                "system_warning",
                Map.of("type", "warning", "idleTime", idleDuration)
            );
            
            log.debug("📢 [TakeoverCleanup] Warning message sent to conversation {}", conversation.getId());
            
        } catch (Exception e) {
            log.error("❌ [TakeoverCleanup] Failed to send warning message to conversation {}: {}", 
                     conversation.getId(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Send cleanup notification
     */
    private void sendCleanupNotification(Conversation conversation, long idleDuration) {
        try {
            String notificationMessage = String.format(
                "🔄 This conversation has been returned to the bot after being idle for %d minutes.",
                idleDuration / 60000
            );
            
            messageService.saveMessage(
                conversation.getId(),
                "system",
                notificationMessage,
                "system_notification",
                Map.of("type", "cleanup", "idleTime", idleDuration, "timestamp", System.currentTimeMillis())
            );
            
            log.debug("📢 [TakeoverCleanup] Cleanup notification sent to conversation {}", conversation.getId());
            
        } catch (Exception e) {
            log.error("❌ [TakeoverCleanup] Failed to send cleanup notification to conversation {}: {}", 
                     conversation.getId(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Shutdown hook
     */
    public void shutdown() {
        try {
            log.info("🛑 [TakeoverCleanup] Shutting down cleanup executor...");
            cleanupExecutor.shutdown();
            cleanupExecutor.awaitTermination(30, TimeUnit.SECONDS);
            log.info("✅ [TakeoverCleanup] Shutdown completed");
        } catch (InterruptedException e) {
            log.error("❌ [TakeoverCleanup] Shutdown interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
    
    // Data classes
    public static class CleanupStats {
        public int totalProcessed = 0;
        public int active = 0;
        public int needsWarning = 0;
        public int needsCleanup = 0;
        public int released = 0;
        public int warnings = 0;
        public int errors = 0;
        
        public String getSummary() {
            return String.format("processed=%d, active=%d, warnings=%d, released=%d, errors=%d", 
                               totalProcessed, active, warnings, released, errors);
        }
    }
    
    public static class CleanupResult {
        private Long conversationId;
        private boolean success;
        private long idleDuration;
        private String reason;
        private long timestamp;
        
        public static CleanupResultBuilder builder() {
            return new CleanupResultBuilder();
        }
        
        // Getters and setters
        public Long getConversationId() { return conversationId; }
        public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public long getIdleDuration() { return idleDuration; }
        public void setIdleDuration(long idleDuration) { this.idleDuration = idleDuration; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public static class CleanupResultBuilder {
            private final CleanupResult result = new CleanupResult();
            
            public CleanupResultBuilder conversationId(Long conversationId) {
                result.conversationId = conversationId;
                return this;
            }
            
            public CleanupResultBuilder success(boolean success) {
                result.success = success;
                return this;
            }
            
            public CleanupResultBuilder idleDuration(long idleDuration) {
                result.idleDuration = idleDuration;
                return this;
            }
            
            public CleanupResultBuilder reason(String reason) {
                result.reason = reason;
                return this;
            }
            
            public CleanupResult build() {
                result.timestamp = System.currentTimeMillis();
                return result;
            }
        }
    }
}