package com.chatbot.core.identity.service;

import com.chatbot.core.identity.constants.IdentityConstants;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public TokenBlacklistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * Add token to blacklist
     */
    public void blacklistToken(String token) {
        String key = IdentityConstants.BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "true", Duration.ofHours(24));
    }
    
    /**
     * Check if token is blacklisted
     */
    public boolean isTokenBlacklisted(String token) {
        String key = IdentityConstants.BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    /**
     * Remove token from blacklist
     */
    public void removeFromBlacklist(String token) {
        String key = IdentityConstants.BLACKLIST_PREFIX + token;
        redisTemplate.delete(key);
    }
    
    /**
     * Blacklist all tokens for a user (used on password change)
     */
    public void blacklistAllUserTokens(String userEmail) {
        // This would require storing user-token mappings
        // For now, we'll use a user-specific blacklist entry
        String key = IdentityConstants.BLACKLIST_PREFIX + "user:" + userEmail;
        redisTemplate.opsForValue().set(key, "true", Duration.ofHours(24));
    }
    
    /**
     * Check if user's tokens are blacklisted
     */
    public boolean areUserTokensBlacklisted(String userEmail) {
        String key = IdentityConstants.BLACKLIST_PREFIX + "user:" + userEmail;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
