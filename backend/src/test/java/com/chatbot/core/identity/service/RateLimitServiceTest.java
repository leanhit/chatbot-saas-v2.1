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
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOps;

    private RateLimitService service;

    @BeforeEach
    void setUp() {
        service = new RateLimitService(redisTemplate);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    private String key(String email) {
        return IdentityConstants.RATE_LIMIT_PREFIX + "login:" + email;
    }

    @Test
    void firstAttemptAllowedAndSetsCounter() {
        when(valueOps.get(key("u@t.com"))).thenReturn(null);

        assertTrue(service.isLoginAllowed("u@t.com"));
        verify(valueOps).set(key("u@t.com"), "1",
            Duration.ofMinutes(IdentityConstants.LOGIN_ATTEMPT_WINDOW_MINUTES));
    }

    @Test
    void attemptBelowMaxIncrementsAndAllows() {
        when(valueOps.get(key("u@t.com"))).thenReturn("3");

        assertTrue(service.isLoginAllowed("u@t.com"));
        verify(valueOps).increment(key("u@t.com"));
    }

    @Test
    void attemptAtMaxRejected() {
        when(valueOps.get(key("u@t.com"))).thenReturn(String.valueOf(IdentityConstants.MAX_LOGIN_ATTEMPTS));

        assertFalse(service.isLoginAllowed("u@t.com"));
        verify(valueOps, never()).increment(anyString());
    }

    @Test
    void redisFailureAllowsLogin() {
        when(valueOps.get(anyString())).thenThrow(new RuntimeException("redis down"));

        assertTrue(service.isLoginAllowed("u@t.com"));
    }

    @Test
    void resetLoginAttemptsDeletesKey() {
        service.resetLoginAttempts("u@t.com");
        verify(redisTemplate).delete(key("u@t.com"));
    }

    @Test
    void getRemainingAttemptsNoKeyReturnsMax() {
        when(valueOps.get(key("u@t.com"))).thenReturn(null);
        assertEquals(IdentityConstants.MAX_LOGIN_ATTEMPTS, service.getRemainingAttempts("u@t.com"));
    }

    @Test
    void getRemainingAttemptsComputesRemaining() {
        when(valueOps.get(key("u@t.com"))).thenReturn("3");
        assertEquals(IdentityConstants.MAX_LOGIN_ATTEMPTS - 3, service.getRemainingAttempts("u@t.com"));
    }

    @Test
    void getRemainingAttemptsNeverNegative() {
        when(valueOps.get(key("u@t.com"))).thenReturn("99");
        assertEquals(0, service.getRemainingAttempts("u@t.com"));
    }

    @Test
    void getRemainingAttemptsRedisFailureReturnsMax() {
        when(valueOps.get(anyString())).thenThrow(new RuntimeException("down"));
        assertEquals(IdentityConstants.MAX_LOGIN_ATTEMPTS, service.getRemainingAttempts("u@t.com"));
    }

    @Test
    void getTimeUntilResetReturnsTtl() {
        when(redisTemplate.getExpire(key("u@t.com"), TimeUnit.MINUTES)).thenReturn(10L);
        assertEquals(10L, service.getTimeUntilReset("u@t.com"));
    }

    @Test
    void getTimeUntilResetReturnsZeroWhenNoTtl() {
        when(redisTemplate.getExpire(anyString(), any())).thenReturn(null);
        assertEquals(0L, service.getTimeUntilReset("u@t.com"));
    }

    @Test
    void getTimeUntilResetReturnsZeroOnError() {
        when(redisTemplate.getExpire(anyString(), any())).thenThrow(new RuntimeException("down"));
        assertEquals(0L, service.getTimeUntilReset("u@t.com"));
    }
}
