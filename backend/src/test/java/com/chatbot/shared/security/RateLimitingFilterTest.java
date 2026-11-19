package com.chatbot.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    private RateLimitingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitingFilter(redisTemplate,
                new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()));
        ReflectionTestUtils.setField(filter, "defaultRequestLimit", 2);
        ReflectionTestUtils.setField(filter, "defaultWindowSeconds", 60);
        ReflectionTestUtils.setField(filter, "trustProxyHeaders", true);
    }

    @Test
    void callsDownstreamExactlyOnceWhenDownstreamThrows() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getRemoteAddr()).thenReturn("1.2.3.4");
        when(redisTemplate.execute(any(), anyList(), any())).thenReturn(1L);
        doThrow(new RuntimeException("downstream boom")).when(filterChain).doFilter(request, response);

        assertThrows(RuntimeException.class, () -> filter.doFilter(request, response, filterChain));

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void returns429WithSharedErrorContractWhenLimitExceeded() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getRemoteAddr()).thenReturn("1.2.3.4");
        when(redisTemplate.execute(any(), anyList(), any())).thenReturn(3L);
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
    void usesAtomicScriptWithExpireInside() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getRemoteAddr()).thenReturn("1.2.3.4");
        when(redisTemplate.execute(any(), anyList(), any())).thenReturn(1L);

        filter.doFilter(request, response, filterChain);

        // TTL phải nằm trong cùng Lua script với INCR — không tách rời
        org.mockito.ArgumentCaptor<org.springframework.data.redis.core.script.RedisScript> captor =
                org.mockito.ArgumentCaptor.forClass(org.springframework.data.redis.core.script.RedisScript.class);
        verify(redisTemplate).execute(captor.capture(), anyList(), any());
        String script = ((org.springframework.data.redis.core.script.DefaultRedisScript<?>) captor.getValue()).getScriptAsString();
        assertTrue(script.contains("INCR"), "script should INCR: " + script);
        assertTrue(script.contains("EXPIRE"), "script should EXPIRE atomically: " + script);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void failsOpenWhenExpireFailsInsideScript() throws Exception {
        // Script lỗi (vd Redis từ chối) → fail-open, không chặn request
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getRemoteAddr()).thenReturn("1.2.3.4");
        when(redisTemplate.execute(any(), anyList(), any())).thenThrow(new RuntimeException("expire failed"));

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    void failsOpenWhenRedisThrows() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getRemoteAddr()).thenReturn("1.2.3.4");
        when(redisTemplate.execute(any(), anyList(), any())).thenThrow(new RuntimeException("redis down"));

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    void ignoresProxyHeadersWhenTrustDisabled() throws Exception {
        ReflectionTestUtils.setField(filter, "trustProxyHeaders", false);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getRemoteAddr()).thenReturn("9.9.9.9");
        when(redisTemplate.execute(any(), anyList(), any())).thenReturn(1L);

        filter.doFilter(request, response, filterChain);

        verify(redisTemplate).execute(any(), eq(List.of("rate_limit:default:9.9.9.9")), any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void usesForwardedForWhenTrustEnabled() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("X-Forwarded-For")).thenReturn("1.1.1.1, 2.2.2.2");
        when(redisTemplate.execute(any(), anyList(), any())).thenReturn(1L);

        filter.doFilter(request, response, filterChain);

        verify(redisTemplate).execute(any(), eq(List.of("rate_limit:default:1.1.1.1")), any());
        verify(filterChain).doFilter(request, response);
    }
}
