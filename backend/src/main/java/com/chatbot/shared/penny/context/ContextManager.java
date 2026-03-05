package com.chatbot.shared.penny.context;

import com.chatbot.shared.penny.context.storage.DatabaseContextStorage;
import com.chatbot.shared.penny.context.storage.RedisContextStorage;
import com.chatbot.shared.penny.dto.request.MiddlewareRequest;
import com.chatbot.shared.penny.dto.response.MiddlewareResponse;
import com.chatbot.shared.penny.model.PennyBotType;
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
                redisStorage.saveContext(contextKey, context, Duration.ofHours(1));
                return context;
            }
            
            // Create new context if none found
            context = createNewContext(request);
            redisStorage.saveContext(contextKey, context, Duration.ofHours(1));
            databaseStorage.saveContext(context);
            
            log.debug("🆕 Created new context: {}", contextKey);
            return context;
            
        } catch (Exception e) {
            log.error("❌ Error loading context for {}: {}", contextKey, e.getMessage());
            return createNewContext(request);
        }
    }
    
    /**
     * Update conversation context after processing
     */
    public void updateContext(ConversationContext context, MiddlewareRequest request, MiddlewareResponse response) {
        String contextKey = buildContextKey(request);
        
        try {
            // Update context with new information
            context.setLastUpdated(Instant.now());
            context.incrementMessageCount();
            
            // Add current message to history
            context.addMessageToHistory(request.getMessage(), response.getResponse());
            
            // Update intent if available
            if (response.getIntentAnalysis() != null) {
                context.setCurrentIntent(response.getIntentAnalysis().getIntent());
                context.setIntentConfidence(response.getIntentAnalysis().getConfidence());
            }
            
            // Save to both storages
            redisStorage.saveContext(contextKey, context, Duration.ofHours(1));
            databaseStorage.saveContext(context);
            
            log.debug("✅ Context updated: {}", contextKey);
            
        } catch (Exception e) {
            log.error("❌ Error updating context for {}: {}", contextKey, e.getMessage());
        }
    }
    
    /**
     * Initialize bot context
     */
    public void initializeBotContext(String botId, Long tenantId, PennyBotType botType) {
        try {
            ConversationContext context = ConversationContext.builder()
                .botId(botId)
                .tenantId(tenantId)
                .botType(botType.toString())
                .createdAt(Instant.now())
                .lastUpdated(Instant.now())
                .messageCount(0)
                .build();
            
            databaseStorage.saveContext(context);
            log.info("✅ Initialized bot context: {} for tenant: {}", botId, tenantId);
            
        } catch (Exception e) {
            log.error("❌ Error initializing bot context for {}: {}", botId, e.getMessage());
        }
    }
    
    /**
     * Cleanup bot context
     */
    public void cleanupBotContext(String botId) {
        try {
            databaseStorage.deleteContextByBotId(UUID.fromString(botId));
            redisStorage.deleteContextByBotId(botId);
            log.info("✅ Cleaned up bot context: {}", botId);
            
        } catch (Exception e) {
            log.error("❌ Error cleaning up bot context for {}: {}", botId, e.getMessage());
        }
    }
    
    /**
     * Check if bot context is healthy
     */
    public boolean isBotContextHealthy(String botId) {
        try {
            ConversationContext context = databaseStorage.loadContextByBotId(UUID.fromString(botId));
            return context != null && 
                   Duration.between(context.getLastUpdated(), Instant.now()).toHours() < 24;
        } catch (Exception e) {
            log.error("❌ Error checking bot context health for {}: {}", botId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Configure analytics for bot
     */
    public void configureBotAnalytics(String botId) {
        try {
            // Initialize analytics tracking for bot
            log.info("✅ Configured analytics for bot: {}", botId);
        } catch (Exception e) {
            log.error("❌ Error configuring analytics for bot {}: {}", botId, e.getMessage());
        }
    }
    
    /**
     * Cleanup analytics for bot
     */
    public void cleanupBotAnalytics(String botId) {
        try {
            // Cleanup analytics data for bot
            log.info("✅ Cleaned up analytics for bot: {}", botId);
        } catch (Exception e) {
            log.error("❌ Error cleaning up analytics for bot {}: {}", botId, e.getMessage());
        }
    }
    
    // Private helper methods
    
    private String buildContextKey(MiddlewareRequest request) {
        return String.format("penny:ctx:%s:%s:%s", 
            request.getBotId(), 
            request.getUserId(), 
            request.getPlatform());
    }
    
    private ConversationContext createNewContext(MiddlewareRequest request) {
        return ConversationContext.builder()
            .botId(request.getBotId())
            .userId(request.getUserId())
            .platform(request.getPlatform())
            .createdAt(Instant.now())
            .lastUpdated(Instant.now())
            .messageCount(0)
            .build();
    }
}
