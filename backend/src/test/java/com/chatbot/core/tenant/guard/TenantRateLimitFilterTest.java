package com.chatbot.core.tenant.guard;

import com.chatbot.core.tenant.infra.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantRateLimitFilterTest {

    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOps;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    private TenantRateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new TenantRateLimitFilter(redisTemplate);
        ReflectionTestUtils.setField(filter, "requestsPerMinute", 3);
        ReflectionTestUtils.setField(filter, "enabled", true);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void passesThroughWhenDisabled() throws Exception {
        ReflectionTestUtils.setField(filter, "enabled", false);
        TenantContext.setTenantId(1L);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(valueOps);
    }

    @Test
    void passesThroughForNonApiPath() throws Exception {
        when(request.getRequestURI()).thenReturn("/health");
        TenantContext.setTenantId(1L);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(valueOps);
    }

    @Test
    void passesThroughWhenNoTenantContext() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(valueOps);
    }

    @Test
    void allowsRequestsUnderLimit() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        TenantContext.setTenantId(1L);
        when(valueOps.increment("ratelimit:api:tenant:1")).thenReturn(2L);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    void setsExpiryOnFirstRequest() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        TenantContext.setTenantId(1L);
        when(valueOps.increment("ratelimit:api:tenant:1")).thenReturn(1L);

        filter.doFilter(request, response, filterChain);

        verify(redisTemplate).expire("ratelimit:api:tenant:1", Duration.ofMinutes(1));
    }

    @Test
    void returns429WhenLimitExceeded() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        TenantContext.setTenantId(1L);
        when(valueOps.increment("ratelimit:api:tenant:1")).thenReturn(4L);
        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(429);
        verify(response).setContentType("application/json");
        assertTrue(sw.toString().contains("RATE_LIMITED"));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void failsOpenWhenRedisUnavailable() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        TenantContext.setTenantId(1L);
        when(valueOps.increment(anyString())).thenThrow(new RuntimeException("redis down"));

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }
}
