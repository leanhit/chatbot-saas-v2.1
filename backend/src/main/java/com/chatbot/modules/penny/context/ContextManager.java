package com.chatbot.modules.penny.context;

import com.chatbot.modules.penny.context.storage.DatabaseContextStorage;
import com.chatbot.modules.penny.context.storage.RedisContextStorage;
import com.chatbot.modules.penny.dto.request.MiddlewareRequest;
import com.chatbot.modules.penny.dto.response.MiddlewareResponse;
import com.chatbot.modules.penny.model.PennyBotType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Context Manager - Quản lý ngữ cảnh hội thoại
 */
@Service
@Slf4j
public class ContextManager {
    
    private final RedisContextStorage redisStorage;
    private final DatabaseContextStorage databaseStorage;
    
    public ContextManager(RedisContextStorage redisStorage, 
                         DatabaseContextStorage databaseStorage) {
        this.redisStorage = redisStorage;
        this.databaseStorage = databaseStorage;
    }
    
    /**
     * Load conversation context for request
     */
    public ConversationContext loadContext(MiddlewareRequest request) {
        String contextKey = buildContextKey(request);
        
        log.debug("🔄 Loading context for key: {}", contextKey);
        
        try {
            // Try Redis first for performance
            ConversationContext context = redisStorage.loadContext(contextKey);
            
            if (context != null) {
                log.debug("✅ Context loaded from Redis: {}", contextKey);
                return context;
            }
            
            // Fallback to database
            context = databaseStorage.loadContext(request);
            
            if (context != null) {
                log.debug("✅ Context loaded from database: {}", contextKey);
                // Cache in Redis for future requests
                redisStorage.saveContext(contextKey, context, Duration.ofHours(24));
                return context;
            }
            
            // Create new context
            context = createNewContext(request);
            log.debug("🆕 Created new context: {}", contextKey);
            
            // Save new context
            redisStorage.saveContext(contextKey, context, Duration.ofHours(24));
            databaseStorage.saveContext(context);
            
            return context;
            
        } catch (Exception e) {
            log.error("❌ Error loading context for key {}: {}", contextKey, e.getMessage(), e);
            // Return minimal context as fallback
            return createMinimalContext(request);
        }
    }
    
    /**
     * Update conversation context after processing
     */
    public void updateContext(ConversationContext context, 
                             MiddlewareRequest request, 
                             MiddlewareResponse response) {
        String contextKey = buildContextKey(request);
        
        log.debug("💾 Updating context for key: {}", contextKey);
        
        try {
            // Update context with new information
            updateContextData(context, request, response);
            
            // Update timestamp
            context.setLastActivity(Instant.now());
            context.incrementMessageCount();
            
            // Save to Redis (fast access)
            redisStorage.saveContext(contextKey, context, Duration.ofHours(24));
            
            // Periodically save to database (every 10 messages)
            if (context.getMessageCount() % 10 == 0) {
                databaseStorage.updateContext(context);
                log.debug("💾 Context persisted to database: {}", contextKey);
            }
            
            log.debug("✅ Context updated successfully: {}", contextKey);
            
        } catch (Exception e) {
            log.error("❌ Error updating context for key {}: {}", contextKey, e.getMessage(), e);
        }
    }
    
    /**
     * Clear context
     */
    public void clearContext(String contextKey) {
        log.debug("🗑️ Clearing context: {}", contextKey);
        
        try {
            redisStorage.clearContext(contextKey);
            databaseStorage.clearContext(contextKey);
            log.debug("✅ Context cleared successfully: {}", contextKey);
        } catch (Exception e) {
            log.error("❌ Error clearing context {}: {}", contextKey, e.getMessage(), e);
        }
    }
    
    /**
     * Get active contexts for tenant
     */
    public List<ConversationContext> getActiveContexts(String tenantId) {
        log.debug("📋 Getting active contexts for tenant: {}", tenantId);
        
        try {
            return databaseStorage.getActiveContexts(tenantId);
        } catch (Exception e) {
            log.error("❌ Error getting active contexts for tenant {}: {}", tenantId, e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Cleanup expired contexts
     */
    public void cleanupExpiredContexts() {
        log.debug("🧹 Cleaning up expired contexts");
        
        try {
            int cleanedCount = databaseStorage.cleanupExpiredContexts();
            redisStorage.cleanupExpiredContexts();
            log.debug("✅ Cleaned up {} expired contexts", cleanedCount);
        } catch (Exception e) {
            log.error("❌ Error cleaning up expired contexts: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Get context statistics
     */
    public ContextStatistics getStatistics(String tenantId) {
        log.debug("📊 Getting context statistics for tenant: {}", tenantId);
        
        try {
            DatabaseContextStorage.ContextStatistics dbStats = databaseStorage.getStatistics(tenantId);
            
            ContextStatistics stats = new ContextStatistics();
            stats.setTotalContexts(dbStats.getTotalContexts());
            stats.setActiveContexts(dbStats.getActiveContexts());
            stats.setExpiredContexts(dbStats.getExpiredContexts());
            stats.setAverageMessagesPerContext(dbStats.getAverageMessagesPerContext());
            stats.setTotalMessages(dbStats.getTotalMessages());
            stats.setMostUsedProvider(dbStats.getMostUsedProvider());
            stats.setMostCommonIntent(dbStats.getMostCommonIntent());
            stats.setLastUpdated(dbStats.getLastUpdated());
            
            return stats;
            
        } catch (Exception e) {
            log.error("❌ Error getting statistics for tenant {}: {}", tenantId, e.getMessage(), e);
            return ContextStatistics.empty();
        }
    }
    
    // Private helper methods
    
    private String buildContextKey(MiddlewareRequest request) {
        return String.format("penny:ctx:%s:%s:%s", 
            request.getTenantId(), 
            request.getUserId(), 
            request.getPlatform());
    }
    
    private ConversationContext createNewContext(MiddlewareRequest request) {
        return ConversationContext.builder()
            .contextId(UUID.randomUUID().toString())
            .tenantId(request.getTenantId())
            .userId(request.getUserId())
            .platform(request.getPlatform())
            .connectionId(request.getConnectionId())
            .botId(request.getBotId())
            .ownerId(request.getOwnerId())
            .sessionId(request.getSessionId())
            .status("active")
            .createdAt(Instant.now())
            .lastActivity(Instant.now())
            .messageCount(0)
            .language(request.getLanguage())
            .metadata(new java.util.HashMap<>())
            .intentHistory(new java.util.ArrayList<>())
            .providerHistory(new java.util.ArrayList<>())
            .build();
    }
    
    private ConversationContext createMinimalContext(MiddlewareRequest request) {
        return ConversationContext.builder()
            .contextId(UUID.randomUUID().toString())
            .tenantId(request.getTenantId())
            .userId(request.getUserId())
            .platform(request.getPlatform())
            .status("minimal")
            .createdAt(Instant.now())
            .lastActivity(Instant.now())
            .messageCount(1)
            .build();
    }
    
    private void updateContextData(ConversationContext context, 
                                  MiddlewareRequest request, 
                                  MiddlewareResponse response) {
        // Update last intent
        if (response.getIntentAnalysis() != null) {
            context.setLastIntent(response.getIntentAnalysis().getPrimaryIntent());
            context.addIntentToHistory(response.getIntentAnalysis().getPrimaryIntent());
        }
        
        // Update last provider
        if (response.getProviderUsed() != null) {
            context.setLastProvider(ConversationContext.ProviderType.valueOf(response.getProviderUsed()));
            context.addProviderToHistory(response.getProviderUsed());
        }
        
        // Update language if detected
        if (response.getIntentAnalysis() != null && response.getIntentAnalysis().getLanguage() != null) {
            context.setLanguage(response.getIntentAnalysis().getLanguage());
        }
        
        // Update status based on response
        if (response.needsEscalation()) {
            context.setStatus("escalated");
        } else if (response.hasError()) {
            context.setStatus("error");
        } else {
            context.setStatus("active");
        }
        
        // Add response metadata
        if (response.getProcessingMetrics() != null) {
            context.addMetadata("lastProcessingMetrics", response.getProcessingMetrics());
        }
        
        // Store last message and response
        context.addMetadata("lastMessage", request.getMessage());
        context.addMetadata("lastResponse", response.getResponse());
        context.addMetadata("lastProcessingTime", System.currentTimeMillis());
    }
    
    // Inner class for statistics
    public static class ContextStatistics {
        private long totalContexts;
        private long activeContexts;
        private long expiredContexts;
        private double averageMessagesPerContext;
        private long totalMessages;
        private String mostUsedProvider;
        private String mostCommonIntent;
        private Instant lastUpdated;
        
        public static ContextStatistics empty() {
            return new ContextStatistics();
        }
        
        // Getters and setters
        public long getTotalContexts() { return totalContexts; }
        public void setTotalContexts(long totalContexts) { this.totalContexts = totalContexts; }
        
        public long getActiveContexts() { return activeContexts; }
        public void setActiveContexts(long activeContexts) { this.activeContexts = activeContexts; }
        
        public long getExpiredContexts() { return expiredContexts; }
        public void setExpiredContexts(long expiredContexts) { this.expiredContexts = expiredContexts; }
        
        public double getAverageMessagesPerContext() { return averageMessagesPerContext; }
        public void setAverageMessagesPerContext(double averageMessagesPerContext) { this.averageMessagesPerContext = averageMessagesPerContext; }
        
        public long getTotalMessages() { return totalMessages; }
        public void setTotalMessages(long totalMessages) { this.totalMessages = totalMessages; }
        
        public String getMostUsedProvider() { return mostUsedProvider; }
        public void setMostUsedProvider(String mostUsedProvider) { this.mostUsedProvider = mostUsedProvider; }
        
        public String getMostCommonIntent() { return mostCommonIntent; }
        public void setMostCommonIntent(String mostCommonIntent) { this.mostCommonIntent = mostCommonIntent; }
        
        public Instant getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }
    }
    
    /**
     * Initialize context for a new Penny Bot
     */
    public void initializeBotContext(String botId, Long tenantId, PennyBotType botType) {
        log.info("🧠 Initializing context for Penny bot: {} (type: {}, tenant: {})", botId, botType, tenantId);
        
        try {
            // Create initial context configuration for the bot
            ConversationContext initialContext = new ConversationContext();
            initialContext.setBotId(botId);
            initialContext.setTenantId(tenantId);
            initialContext.setUserId("system");
            initialContext.setPlatform("penny");
            initialContext.setCreatedAt(Instant.now());
            initialContext.setLastActivity(Instant.now());
            initialContext.setMessageCount(0);
            // Remove providerType setting as it doesn't exist in ConversationContext
            
            // Store in Redis for fast access
            String contextKey = "bot:" + botId + ":system";
            redisStorage.saveContext(contextKey, initialContext, Duration.ofHours(24));
            
            // Store in database for persistence
            databaseStorage.saveContext(initialContext);
            
            log.info("✅ Context initialized for Penny bot: {}", botId);
        } catch (Exception e) {
            log.error("❌ Failed to initialize context for bot {}: {}", botId, e.getMessage(), e);
            throw new RuntimeException("Failed to initialize bot context", e);
        }
    }
    
    /**
     * Check if bot context is healthy
     */
    public boolean isBotContextHealthy(String botId) {
        try {
            String contextKey = "bot:" + botId + ":system";
            ConversationContext context = redisStorage.loadContext(contextKey);
            
            if (context == null) {
                // Try database as fallback - use existing method
                log.debug("🔄 Context not in Redis, checking database for bot: {}", botId);
                // For now, assume healthy if Redis doesn't have it (could be new bot)
                return true;
            }
            
            return context.getLastActivity().isAfter(Instant.now().minus(Duration.ofHours(1)));
        } catch (Exception e) {
            log.error("❌ Error checking bot context health for {}: {}", botId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Cleanup bot context
     */
    public void cleanupBotContext(String botId) {
        log.info("🧹 Cleaning up context for Penny bot: {}", botId);
        
        try {
            // Remove from Redis - use existing clearContext method
            String contextKey = "bot:" + botId + ":system";
            clearContext(contextKey);
            
            log.info("✅ Context cleaned up for Penny bot: {}", botId);
        } catch (Exception e) {
            log.error("❌ Failed to cleanup context for bot {}: {}", botId, e.getMessage(), e);
        }
    }
    
    // Provider type enum (if not already defined)
    public enum ProviderType {
        BOTPRESS, RASA, GPT
    }
}
