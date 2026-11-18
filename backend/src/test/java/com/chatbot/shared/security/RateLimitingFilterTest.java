package com.chatbot.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOps;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    private RateLimitingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitingFilter(redisTemplate, new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()));
        ReflectionTestUtils.setField(filter, "defaultRequestLimit", 2);
        ReflectionTestUtils.setField(filter, "defaultWindowSeconds", 60);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void callsDownstreamExactlyOnceWhenDownstreamThrows() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getRemoteAddr()).thenReturn("1.2.3.4");
        when(valueOps.get(anyString())).thenReturn(null);
        doThrow(new RuntimeException("downstream boom")).when(filterChain).doFilter(request, response);

        assertThrows(RuntimeException.class, () -> filter.doFilter(request, response, filterChain));

        // Regression: filter chain must be invoked exactly once even when downstream throws
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void returns429WithSharedErrorContractWhenLimitExceeded() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getRemoteAddr()).thenReturn("1.2.3.4");
        when(valueOps.get(anyString())).thenReturn("2");
        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(429);
        verify(response).setContentType("application/json");
        String body = sw.toString();
        assertTrue(body.contains("RATE_LIMIT_EXCEEDED"), "body should contain RATE_LIMIT_EXCEEDED: " + body);
        assertTrue(body.contains("/api/test"), "body should contain request path: " + body);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void failsOpenWhenRedisThrows() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getRemoteAddr()).thenReturn("1.2.3.4");
        when(valueOps.get(anyString())).thenThrow(new RuntimeException("redis down"));

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }
}
