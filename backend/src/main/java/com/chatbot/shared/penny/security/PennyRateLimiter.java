package com.chatbot.shared.penny.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * PennyRateLimiter — Rate limiting service for Penny Bot endpoints
 *
 * Uses Bucket4j with Redis backend for distributed rate limiting.
 * Enforces limits per user, per tenant, and per IP for public endpoints.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "penny.ratelimit.enabled", havingValue = "true", matchIfMissing = true)
public class PennyRateLimiter {

    @Value("${penny.ratelimit.user.messages-per-minute:60}")
    private int userMessagesPerMinute;

    @Value("${penny.ratelimit.tenant.messages-per-minute:1000}")
    private int tenantMessagesPerMinute;

    @Value("${penny.ratelimit.public.messages-per-minute:10}")
    private int publicMessagesPerMinute;

    @Value("${penny.ratelimit.user.tokens-per-refill:60}")
    private int userTokensPerRefill;

    @Value("${penny.ratelimit.tenant.tokens-per-refill:1000}")
    private int tenantTokensPerRefill;

    @Value("${penny.ratelimit.public.tokens-per-refill:10}")
    private int publicTokensPerRefill;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Check if user is allowed to send a message
     * 
     * @param userId User identifier (e.g., Facebook PSID)
     * @return true if allowed, false if rate limited
     */
    public boolean allowUser(String userId) {
        if (userId == null || userId.isBlank()) {
            return false;
        }

        try {
            Bucket bucket = getUserBucket(userId);
            boolean allowed = bucket.tryConsume(1);
            
            if (!allowed) {
                log.warn("⚠️ Rate limit exceeded for user: {}", userId);
            }
            
            return allowed;
        } catch (Exception e) {
            log.error("❌ Error checking rate limit for user {}: {}", userId, e.getMessage());
            // Fail open - allow request if rate limiter fails
            return true;
        }
    }

    /**
     * Check if tenant is allowed to process a message
     * 
     * @param tenantId Tenant ID
     * @return true if allowed, false if rate limited
     */
    public boolean allowTenant(Long tenantId) {
        if (tenantId == null) {
            return false;
        }

        try {
            Bucket bucket = getTenantBucket(tenantId);
            boolean allowed = bucket.tryConsume(1);
            
            if (!allowed) {
                log.warn("⚠️ Rate limit exceeded for tenant: {}", tenantId);
            }
            
            return allowed;
        } catch (Exception e) {
            log.error("❌ Error checking rate limit for tenant {}: {}", tenantId, e.getMessage());
            return true;
        }
    }

    /**
     * Check if public IP is allowed to send a message
     * 
     * @param ipAddress Client IP address
     * @return true if allowed, false if rate limited
     */
    public boolean allowPublic(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return false;
        }

        try {
            Bucket bucket = getPublicBucket(ipAddress);
            boolean allowed = bucket.tryConsume(1);
            
            if (!allowed) {
                log.warn("⚠️ Rate limit exceeded for public IP: {}", ipAddress);
            }
            
            return allowed;
        } catch (Exception e) {
            log.error("❌ Error checking rate limit for IP {}: {}", ipAddress, e.getMessage());
            return true;
        }
    }

    /**
     * Check combined limits (user + tenant)
     * Both must allow the request
     * 
     * @param userId User identifier
     * @param tenantId Tenant ID
     * @return true if allowed, false if rate limited
     */
    public boolean allowCombined(String userId, Long tenantId) {
        boolean userAllowed = allowUser(userId);
        boolean tenantAllowed = allowTenant(tenantId);
        
        return userAllowed && tenantAllowed;
    }

    /**
     * Get remaining tokens for a user
     * 
     * @param userId User identifier
     * @return Number of remaining tokens
     */
    public long getRemainingTokens(String userId) {
        try {
            Bucket bucket = getUserBucket(userId);
            return bucket.getAvailableTokens();
        } catch (Exception e) {
            log.error("❌ Error getting remaining tokens for user {}: {}", userId, e.getMessage());
            return 0;
        }
    }

    /**
     * Reset rate limit for a user (admin operation)
     * 
     * @param userId User identifier
     */
    public void resetUserLimit(String userId) {
        try {
            String key = "ratelimit:user:" + userId;
            redisTemplate.delete(key);
            log.info("🔄 Reset rate limit for user: {}", userId);
        } catch (Exception e) {
            log.error("❌ Error resetting rate limit for user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Reset rate limit for a tenant (admin operation)
     * 
     * @param tenantId Tenant ID
     */
    public void resetTenantLimit(Long tenantId) {
        try {
            String key = "ratelimit:tenant:" + tenantId;
            redisTemplate.delete(key);
            log.info("🔄 Reset rate limit for tenant: {}", tenantId);
        } catch (Exception e) {
            log.error("❌ Error resetting rate limit for tenant {}: {}", tenantId, e.getMessage());
        }
    }

    // ─── Private helpers ───────────────────────────────────────────────────

    /**
     * Get or create bucket for user rate limiting
     * Uses Redis-based distributed rate limiting for scalability
     */
    private Bucket getUserBucket(String userId) {
        try {
            // Try to use Redis-based distributed bucket
            String key = "ratelimit:user:" + userId;
            
            // Check if bucket exists in Redis
            Long remainingTokens = redisTemplate.opsForValue().increment(key);
            if (remainingTokens == null || remainingTokens == 1) {
                // First request, set expiration
                redisTemplate.expire(key, Duration.ofMinutes(1));
            }
            
            // Check if limit exceeded
            if (remainingTokens != null && remainingTokens > userTokensPerRefill) {
                // Rollback increment
                redisTemplate.opsForValue().decrement(key);
                return Bucket.builder()
                    .addLimit(Bandwidth.builder()
                        .capacity(0)
                        .refillIntervally(0, Duration.ofMinutes(1))
                        .build())
                    .build();
            }
            
            // Return bucket with remaining tokens
            return Bucket.builder()
                .addLimit(Bandwidth.builder()
                    .capacity(userTokensPerRefill)
                    .refillIntervally(userTokensPerRefill, Duration.ofMinutes(1))
                    .build())
                .build();
            
        } catch (Exception e) {
            log.error("❌ Error using Redis rate limiting for user {}, falling back to local bucket", userId, e);
            // Fallback to local bucket
            Bandwidth limit = Bandwidth.builder()
                .capacity(userTokensPerRefill)
                .refillIntervally(userTokensPerRefill, Duration.ofMinutes(1))
                .build();
            
            return Bucket.builder()
                .addLimit(limit)
                .build();
        }
    }

    /**
     * Get or create bucket for tenant rate limiting
     */
    private Bucket getTenantBucket(Long tenantId) {
        try {
            String key = "ratelimit:tenant:" + tenantId;
            
            Long remainingTokens = redisTemplate.opsForValue().increment(key);
            if (remainingTokens == null || remainingTokens == 1) {
                redisTemplate.expire(key, Duration.ofMinutes(1));
            }
            
            if (remainingTokens != null && remainingTokens > tenantTokensPerRefill) {
                redisTemplate.opsForValue().decrement(key);
                return Bucket.builder()
                    .addLimit(Bandwidth.builder()
                        .capacity(0)
                        .refillIntervally(0, Duration.ofMinutes(1))
                        .build())
                    .build();
            }
            
            return Bucket.builder()
                .addLimit(Bandwidth.builder()
                    .capacity(tenantTokensPerRefill)
                    .refillIntervally(tenantTokensPerRefill, Duration.ofMinutes(1))
                    .build())
                .build();
            
        } catch (Exception e) {
            log.error("❌ Error using Redis rate limiting for tenant {}, falling back to local bucket", tenantId, e);
            Bandwidth limit = Bandwidth.builder()
                .capacity(tenantTokensPerRefill)
                .refillIntervally(tenantTokensPerRefill, Duration.ofMinutes(1))
                .build();
            
            return Bucket.builder()
                .addLimit(limit)
                .build();
        }
    }

    /**
     * Get or create bucket for public IP rate limiting
     */
    private Bucket getPublicBucket(String ipAddress) {
        try {
            String key = "ratelimit:public:" + ipAddress;
            
            Long remainingTokens = redisTemplate.opsForValue().increment(key);
            if (remainingTokens == null || remainingTokens == 1) {
                redisTemplate.expire(key, Duration.ofMinutes(1));
            }
            
            if (remainingTokens != null && remainingTokens > publicTokensPerRefill) {
                redisTemplate.opsForValue().decrement(key);
                return Bucket.builder()
                    .addLimit(Bandwidth.builder()
                        .capacity(0)
                        .refillIntervally(0, Duration.ofMinutes(1))
                        .build())
                    .build();
            }
            
            return Bucket.builder()
                .addLimit(Bandwidth.builder()
                    .capacity(publicTokensPerRefill)
                    .refillIntervally(publicTokensPerRefill, Duration.ofMinutes(1))
                    .build())
                .build();
            
        } catch (Exception e) {
            log.error("❌ Error using Redis rate limiting for IP {}, falling back to local bucket", ipAddress, e);
            Bandwidth limit = Bandwidth.builder()
                .capacity(publicTokensPerRefill)
                .refillIntervally(publicTokensPerRefill, Duration.ofMinutes(1))
                .build();
            
            return Bucket.builder()
                .addLimit(limit)
                .build();
        }
    }

    /**
     * Get rate limit configuration
     */
    public RateLimitConfig getConfig() {
        return RateLimitConfig.builder()
            .userMessagesPerMinute(userMessagesPerMinute)
            .tenantMessagesPerMinute(tenantMessagesPerMinute)
            .publicMessagesPerMinute(publicMessagesPerMinute)
            .build();
    }

    /**
     * Configuration DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class RateLimitConfig {
        private int userMessagesPerMinute;
        private int tenantMessagesPerMinute;
        private int publicMessagesPerMinute;
    }
}
