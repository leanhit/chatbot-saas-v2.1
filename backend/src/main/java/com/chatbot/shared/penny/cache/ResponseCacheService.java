package com.chatbot.shared.penny.cache;

import com.chatbot.shared.penny.core.config.PennyProperties;
import com.chatbot.shared.penny.dto.response.MiddlewareResponse;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Response Cache Service - Caches common query responses
 * Uses Caffeine cache for high-performance in-memory caching
 */
@Service
@Slf4j
public class ResponseCacheService {

    private final PennyProperties pennyProperties;
    private Cache<String, CachedResponse> responseCache;

    /**
     * Initialize cache on construction
     */
    public ResponseCacheService(PennyProperties pennyProperties) {
        this.pennyProperties = pennyProperties;
        initializeCache();
    }

    /**
     * Initialize Caffeine cache with configured settings
     */
    private void initializeCache() {
        PennyProperties.ResponseCache config = pennyProperties.getResponseCache();
        
        if (!config.isEnabled()) {
            log.info("⚠️ Response cache is disabled");
            return;
        }

        this.responseCache = Caffeine.newBuilder()
            .maximumSize(config.getMaxSize())
            .expireAfterWrite(config.getTtl())
            .recordStats()
            .build();

        log.info("✅ Response cache initialized - Max size: {}, TTL: {}", 
            config.getMaxSize(), config.getTtl());
    }

    /**
     * Get cached response for a message
     */
    public MiddlewareResponse getCachedResponse(String message, String intent) {
        if (!pennyProperties.getResponseCache().isEnabled() || responseCache == null) {
            return null;
        }

        String cacheKey = generateCacheKey(message, intent);
        CachedResponse cached = responseCache.getIfPresent(cacheKey);

        if (cached != null) {
            log.debug("📦 Cache hit for intent: {}", intent);
            return cached.toMiddlewareResponse();
        }

        return null;
    }

    /**
     * Cache a response for a message
     */
    public void cacheResponse(String message, String intent, MiddlewareResponse response) {
        if (!pennyProperties.getResponseCache().isEnabled() || responseCache == null) {
            return;
        }

        // Only cache common queries (greeting, simple inquiries)
        if (!pennyProperties.getResponseCache().isCacheCommonQueries()) {
            return;
        }

        if (!isCacheableIntent(intent)) {
            log.debug("⚠️ Intent not cacheable: {}", intent);
            return;
        }

        String cacheKey = generateCacheKey(message, intent);
        CachedResponse cached = new CachedResponse(response);
        responseCache.put(cacheKey, cached);

        log.debug("💾 Cached response for intent: {}", intent);
    }

    /**
     * Clear cache for a specific message
     */
    public void evict(String message, String intent) {
        if (responseCache == null) return;

        String cacheKey = generateCacheKey(message, intent);
        responseCache.invalidate(cacheKey);
        log.debug("🗑️ Evicted cache for key: {}", cacheKey);
    }

    /**
     * Clear all cached responses
     */
    public void clearAll() {
        if (responseCache == null) return;

        responseCache.invalidateAll();
        log.info("🗑️ Cleared all response cache");
    }

    /**
     * Get cache statistics
     */
    public CacheStats getStats() {
        if (responseCache == null) {
            return new CacheStats(0, 0, 0, 0);
        }

        com.github.benmanes.caffeine.cache.stats.CacheStats stats = responseCache.stats();
        return new CacheStats(
            stats.hitCount(),
            stats.missCount(),
            stats.hitRate(),
            responseCache.estimatedSize()
        );
    }

    /**
     * Generate cache key from message and intent
     */
    private String generateCacheKey(String message, String intent) {
        // Normalize message for cache key
        String normalized = message.toLowerCase().trim();
        return intent + ":" + normalized.hashCode();
    }

    /**
     * Check if intent is cacheable
     */
    private boolean isCacheableIntent(String intent) {
        return switch (intent) {
            case "greeting", "gratitude", "goodbye" -> true;
            case "product_inquiry", "price_inquiry" -> true;
            default -> false;
        };
    }

    /**
     * Cached response wrapper
     */
    private static class CachedResponse {
        private final String response;
        private final String providerUsed;
        private final long timestamp;

        public CachedResponse(MiddlewareResponse response) {
            this.response = response.getResponse();
            this.providerUsed = response.getProviderUsed();
            this.timestamp = System.currentTimeMillis();
        }

        public MiddlewareResponse toMiddlewareResponse() {
            return MiddlewareResponse.builder()
                .response(response)
                .providerUsed(providerUsed)
                .timestamp(java.time.Instant.ofEpochMilli(timestamp))
                .build();
        }
    }

    /**
     * Cache statistics record
     */
    public record CacheStats(long hitCount, long missCount, double hitRate, long size) {}
}
