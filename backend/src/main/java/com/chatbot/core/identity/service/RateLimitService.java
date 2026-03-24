package com.chatbot.core.identity.service;

import com.chatbot.core.identity.constants.IdentityConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RateLimitService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public RateLimitService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * Check if user has exceeded rate limit for login attempts
     */
    public boolean isLoginAllowed(String email) {
        String key = IdentityConstants.RATE_LIMIT_PREFIX + "login:" + email;
        
        try {
            String attempts = redisTemplate.opsForValue().get(key);
            if (attempts == null) {
                // First attempt, set counter
                redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(IdentityConstants.LOGIN_ATTEMPT_WINDOW_MINUTES));
                return true;
            }
            
            int currentAttempts = Integer.parseInt(attempts);
            if (currentAttempts >= IdentityConstants.MAX_LOGIN_ATTEMPTS) {
                log.warn("User {} exceeded rate limit with {} attempts", email, currentAttempts);
                return false;
            }
            
            // Increment counter
            redisTemplate.opsForValue().increment(key);
            return true;
            
        } catch (Exception e) {
            log.error("Error checking rate limit for user: {}", email, e);
            // Allow login if Redis fails
            return true;
        }
    }
    
    /**
     * Reset rate limit for user (called on successful login)
     */
    public void resetLoginAttempts(String email) {
        String key = IdentityConstants.RATE_LIMIT_PREFIX + "login:" + email;
        redisTemplate.delete(key);
    }
    
    /**
     * Get remaining attempts for user
     */
    public int getRemainingAttempts(String email) {
        String key = IdentityConstants.RATE_LIMIT_PREFIX + "login:" + email;
        
        try {
            String attempts = redisTemplate.opsForValue().get(key);
            if (attempts == null) {
                return IdentityConstants.MAX_LOGIN_ATTEMPTS;
            }
            
            int currentAttempts = Integer.parseInt(attempts);
            return Math.max(0, IdentityConstants.MAX_LOGIN_ATTEMPTS - currentAttempts);
            
        } catch (Exception e) {
            log.error("Error getting remaining attempts for user: {}", email, e);
            return IdentityConstants.MAX_LOGIN_ATTEMPTS;
        }
    }
    
    /**
     * Get time until rate limit resets
     */
    public long getTimeUntilReset(String email) {
        String key = IdentityConstants.RATE_LIMIT_PREFIX + "login:" + email;
        
        try {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.MINUTES);
            return ttl != null && ttl > 0 ? ttl : 0;
        } catch (Exception e) {
            log.error("Error getting TTL for user: {}", email, e);
            return 0;
        }
    }
}
