package com.chatbot.modules.penny.context.storage;

import com.chatbot.modules.penny.context.ConversationContext;
import com.chatbot.modules.penny.dto.request.MiddlewareRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis Context Storage - Lưu trữ context trong Redis cho performance cao
 */
@Service
@Slf4j
public class RedisContextStorage {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String CONTEXT_KEY_PREFIX = "penny:ctx:";
    private static final String METADATA_KEY_PREFIX = "penny:meta:";
    private static final String STATS_KEY_PREFIX = "penny:stats:";
    
    public RedisContextStorage(RedisTemplate<String, Object> redisTemplate, 
                             ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Load context from Redis
     */
    public ConversationContext loadContext(String contextKey) {
        try {
            String redisKey = buildRedisKey(contextKey);
            Object contextObj = redisTemplate.opsForValue().get(redisKey);
            
            if (contextObj == null) {
                log.debug("🔍 Context not found in Redis: {}", contextKey);
                return null;
            }
            
            ConversationContext context;
            if (contextObj instanceof String) {
                context = objectMapper.readValue((String) contextObj, ConversationContext.class);
            } else {
                context = objectMapper.convertValue(contextObj, ConversationContext.class);
            }
            
            log.debug("✅ Context loaded from Redis: {} (age: {} min)", 
                contextKey, context.getAgeInMinutes());
            
            return context;
            
        } catch (Exception e) {
            log.error("❌ Error loading context from Redis {}: {}", contextKey, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Save context to Redis
     */
    public void saveContext(String contextKey, ConversationContext context, Duration ttl) {
        try {
            String redisKey = buildRedisKey(contextKey);
            String contextJson = objectMapper.writeValueAsString(context);
            
            redisTemplate.opsForValue().set(redisKey, contextJson, ttl);
            
            // Also save metadata for quick access
            saveContextMetadata(contextKey, context);
            
            log.debug("💾 Context saved to Redis: {} (TTL: {} min)", 
                contextKey, ttl.toMinutes());
            
        } catch (Exception e) {
            log.error("❌ Error saving context to Redis {}: {}", contextKey, e.getMessage(), e);
        }
    }
    
    /**
     * Update context in Redis
     */
    public void updateContext(String contextKey, ConversationContext context) {
        try {
            String redisKey = buildRedisKey(contextKey);
            String contextJson = objectMapper.writeValueAsString(context);
            
            redisTemplate.opsForValue().set(redisKey, contextJson);
            
            // Update metadata
            saveContextMetadata(contextKey, context);
            
            log.debug("🔄 Context updated in Redis: {}", contextKey);
            
        } catch (Exception e) {
            log.error("❌ Error updating context in Redis {}: {}", contextKey, e.getMessage(), e);
        }
    }
    
    /**
     * Clear context from Redis
     */
    public void clearContext(String contextKey) {
        try {
            String redisKey = buildRedisKey(contextKey);
            String metadataKey = buildMetadataKey(contextKey);
            
            redisTemplate.delete(redisKey);
            redisTemplate.delete(metadataKey);
            
            log.debug("🗑️ Context cleared from Redis: {}", contextKey);
            
        } catch (Exception e) {
            log.error("❌ Error clearing context from Redis {}: {}", contextKey, e.getMessage(), e);
        }
    }
    
    /**
     * Check if context exists in Redis
     */
    public boolean contextExists(String contextKey) {
        try {
            String redisKey = buildRedisKey(contextKey);
            return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
        } catch (Exception e) {
            log.error("❌ Error checking context existence in Redis {}: {}", contextKey, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Get context TTL
     */
    public Duration getContextTTL(String contextKey) {
        try {
            String redisKey = buildRedisKey(contextKey);
            Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            return ttl != null && ttl > 0 ? Duration.ofSeconds(ttl) : Duration.ZERO;
        } catch (Exception e) {
            log.error("❌ Error getting context TTL from Redis {}: {}", contextKey, e.getMessage(), e);
            return Duration.ZERO;
        }
    }
    
    /**
     * Extend context TTL
     */
    public void extendContextTTL(String contextKey, Duration additionalTTL) {
        try {
            String redisKey = buildRedisKey(contextKey);
            redisTemplate.expire(redisKey, additionalTTL);
            
            log.debug("⏰ Context TTL extended: {} (+{} min)", 
                contextKey, additionalTTL.toMinutes());
            
        } catch (Exception e) {
            log.error("❌ Error extending context TTL in Redis {}: {}", contextKey, e.getMessage(), e);
        }
    }
    
    /**
     * Get all active context keys for tenant
     */
    public List<String> getActiveContextKeys(String tenantId) {
        try {
            String pattern = CONTEXT_KEY_PREFIX + tenantId + ":*";
            java.util.Set<String> keys = redisTemplate.keys(pattern);
            
            return keys.stream()
                .map(this::extractContextKey)
                .toList();
                
        } catch (Exception e) {
            log.error("❌ Error getting active context keys for tenant {}: {}", tenantId, e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Get context count for tenant
     */
    public long getContextCount(String tenantId) {
        try {
            String pattern = CONTEXT_KEY_PREFIX + tenantId + ":*";
            java.util.Set<String> keys = redisTemplate.keys(pattern);
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            log.error("❌ Error getting context count for tenant {}: {}", tenantId, e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * Cleanup expired contexts
     */
    public void cleanupExpiredContexts() {
        try {
            // Redis automatically handles expired keys
            // This method can be used for additional cleanup if needed
            log.debug("🧹 Redis cleanup completed (expired keys auto-removed by Redis)");
        } catch (Exception e) {
            log.error("❌ Error during Redis cleanup: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Save context statistics
     */
    public void saveStatistics(String tenantId, ContextStatistics stats) {
        try {
            String statsKey = buildStatsKey(tenantId);
            String statsJson = objectMapper.writeValueAsString(stats);
            
            redisTemplate.opsForValue().set(statsKey, statsJson, Duration.ofMinutes(5));
            
            log.debug("📊 Statistics saved to Redis: {}", tenantId);
            
        } catch (Exception e) {
            log.error("❌ Error saving statistics to Redis for tenant {}: {}", tenantId, e.getMessage(), e);
        }
    }
    
    /**
     * Get context statistics
     */
    public ContextStatistics getStatistics(String tenantId) {
        try {
            String statsKey = buildStatsKey(tenantId);
            Object statsObj = redisTemplate.opsForValue().get(statsKey);
            
            if (statsObj == null) {
                return ContextStatistics.empty();
            }
            
            if (statsObj instanceof String) {
                return objectMapper.readValue((String) statsObj, ContextStatistics.class);
            } else {
                return objectMapper.convertValue(statsObj, ContextStatistics.class);
            }
            
        } catch (Exception e) {
            log.error("❌ Error getting statistics from Redis for tenant {}: {}", tenantId, e.getMessage(), e);
            return ContextStatistics.empty();
        }
    }
    
    // Private helper methods
    
    private String buildRedisKey(String contextKey) {
        return CONTEXT_KEY_PREFIX + contextKey;
    }
    
    private String buildMetadataKey(String contextKey) {
        return METADATA_KEY_PREFIX + contextKey;
    }
    
    private String buildStatsKey(String tenantId) {
        return STATS_KEY_PREFIX + tenantId;
    }
    
    private String extractContextKey(String redisKey) {
        if (redisKey.startsWith(CONTEXT_KEY_PREFIX)) {
            return redisKey.substring(CONTEXT_KEY_PREFIX.length());
        }
        return redisKey;
    }
    
    private void saveContextMetadata(String contextKey, ConversationContext context) {
        try {
            String metadataKey = buildMetadataKey(contextKey);
            
            java.util.Map<String, Object> metadata = new java.util.HashMap<>();
            metadata.put("contextId", context.getContextId());
            metadata.put("userId", context.getUserId());
            metadata.put("platform", context.getPlatform());
            metadata.put("status", context.getStatus());
            metadata.put("lastActivity", context.getLastActivity().toString());
            metadata.put("messageCount", context.getMessageCount());
            metadata.put("lastIntent", context.getLastIntent());
            metadata.put("lastProvider", context.getLastProvider());
            
            redisTemplate.opsForHash().putAll(metadataKey, metadata);
            redisTemplate.expire(metadataKey, Duration.ofHours(25)); // Slightly longer than context
            
        } catch (Exception e) {
            log.error("❌ Error saving context metadata for {}: {}", contextKey, e.getMessage(), e);
        }
    }
    
    // Inner class for statistics
    public static class ContextStatistics {
        private long totalContexts;
        private long activeContexts;
        private double averageMessagesPerContext;
        private long totalMessages;
        private String mostUsedProvider;
        private String mostCommonIntent;
        private java.time.Instant lastUpdated;
        
        public static ContextStatistics empty() {
            return new ContextStatistics();
        }
        
        // Getters and setters
        public long getTotalContexts() { return totalContexts; }
        public void setTotalContexts(long totalContexts) { this.totalContexts = totalContexts; }
        
        public long getActiveContexts() { return activeContexts; }
        public void setActiveContexts(long activeContexts) { this.activeContexts = activeContexts; }
        
        public double getAverageMessagesPerContext() { return averageMessagesPerContext; }
        public void setAverageMessagesPerContext(double averageMessagesPerContext) { this.averageMessagesPerContext = averageMessagesPerContext; }
        
        public long getTotalMessages() { return totalMessages; }
        public void setTotalMessages(long totalMessages) { this.totalMessages = totalMessages; }
        
        public String getMostUsedProvider() { return mostUsedProvider; }
        public void setMostUsedProvider(String mostUsedProvider) { this.mostUsedProvider = mostUsedProvider; }
        
        public String getMostCommonIntent() { return mostCommonIntent; }
        public void setMostCommonIntent(String mostCommonIntent) { this.mostCommonIntent = mostCommonIntent; }
        
        public java.time.Instant getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(java.time.Instant lastUpdated) { this.lastUpdated = lastUpdated; }
    }
}
