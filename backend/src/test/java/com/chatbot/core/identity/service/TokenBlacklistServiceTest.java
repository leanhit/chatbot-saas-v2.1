package com.chatbot.core.identity.service;

import com.chatbot.core.identity.constants.IdentityConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOps;

    private TokenBlacklistService service;

    @BeforeEach
    void setUp() {
        service = new TokenBlacklistService(redisTemplate);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void blacklistTokenSetsKeyWith24hTtl() {
        service.blacklistToken("jwt-123");
        verify(valueOps).set(
            eq(IdentityConstants.BLACKLIST_PREFIX + "jwt-123"),
            eq("true"),
            eq(Duration.ofHours(24)));
    }

    @Test
    void isTokenBlacklistedReturnsTrueWhenKeyExists() {
        when(redisTemplate.hasKey(IdentityConstants.BLACKLIST_PREFIX + "jwt-123")).thenReturn(true);
        assertTrue(service.isTokenBlacklisted("jwt-123"));
    }

    @Test
    void isTokenBlacklistedReturnsFalseWhenMissing() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        assertFalse(service.isTokenBlacklisted("jwt-123"));
    }

    @Test
    void isTokenBlacklistedFalseWhenHasKeyNull() {
        when(redisTemplate.hasKey(anyString())).thenReturn(null);
        assertFalse(service.isTokenBlacklisted("jwt-123"));
    }

    @Test
    void removeFromBlacklistDeletesKey() {
        service.removeFromBlacklist("jwt-123");
        verify(redisTemplate).delete(IdentityConstants.BLACKLIST_PREFIX + "jwt-123");
    }

    @Test
    void blacklistAllUserTokensUsesUserKey() {
        service.blacklistAllUserTokens("user@t.com");
        verify(valueOps).set(
            eq(IdentityConstants.BLACKLIST_PREFIX + "user:user@t.com"),
            eq("true"),
            eq(Duration.ofHours(24)));
    }

    @Test
    void areUserTokensBlacklistedChecksUserKey() {
        when(redisTemplate.hasKey(IdentityConstants.BLACKLIST_PREFIX + "user:u@t.com")).thenReturn(true);
        assertTrue(service.areUserTokensBlacklisted("u@t.com"));
    }
}
