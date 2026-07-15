package com.chatbot.shared.penny.security;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiter for Penny API endpoints
 * Uses Bucket4j token bucket algorithm for rate limiting
 */
@Component
@Slf4j
public class RateLimiter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    // Default rate limit: 100 requests per minute
    private static final int DEFAULT_CAPACITY = 100;
    private static final Duration DEFAULT_REFILL_DURATION = Duration.ofMinutes(1);

    /**
     * Check if request is allowed for given identifier (IP, user ID, etc.)
     */
    public boolean allowRequest(String identifier) {
        Bucket bucket = buckets.computeIfAbsent(identifier, this::createBucket);
        
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            return true;
        }
        
        log.warn("⚠️ Rate limit exceeded for identifier: {}", identifier);
        return false;
    }

    /**
     * Check if request is allowed with custom rate limit
     */
    public boolean allowRequest(String identifier, int capacity, Duration refillDuration) {
        String key = identifier + ":" + capacity + ":" + refillDuration;
        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket(capacity, refillDuration));
        
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            return true;
        }
        
        log.warn("⚠️ Rate limit exceeded for identifier: {} (capacity: {}, duration: {})", 
            identifier, capacity, refillDuration);
        return false;
    }

    /**
     * Get remaining tokens for identifier
     */
    public long getRemainingTokens(String identifier) {
        Bucket bucket = buckets.get(identifier);
        return bucket != null ? bucket.getAvailableTokens() : 0;
    }

    /**
     * Create default bucket
     */
    private Bucket createBucket(String identifier) {
        return createBucket(DEFAULT_CAPACITY, DEFAULT_REFILL_DURATION);
    }

    /**
     * Create bucket with custom capacity and refill duration
     */
    private Bucket createBucket(int capacity, Duration refillDuration) {
        return Bucket.builder()
            .addLimit(limit -> limit
                .capacity(capacity)
                .refillIntervally(capacity, refillDuration))
            .build();
    }

    /**
     * Extract identifier from request (IP address or user ID)
     */
    public String extractIdentifier(HttpServletRequest request) {
        // Try to get user ID from header first
        String userId = request.getHeader("X-User-ID");
        if (userId != null && !userId.isBlank()) {
            return "user:" + userId;
        }

        // Fall back to IP address
        String ipAddress = request.getRemoteAddr();
        return "ip:" + ipAddress;
    }

    /**
     * Reset bucket for identifier (for testing or admin purposes)
     */
    public void resetBucket(String identifier) {
        buckets.remove(identifier);
        log.debug("🗑️ Reset rate limit bucket for: {}", identifier);
    }

    /**
     * Clear all buckets (for testing or admin purposes)
     */
    public void clearAll() {
        buckets.clear();
        log.info("🗑️ Cleared all rate limit buckets");
    }
}
