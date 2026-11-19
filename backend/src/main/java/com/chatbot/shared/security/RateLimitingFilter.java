package com.chatbot.shared.security;

import com.chatbot.shared.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    /**
     * Atomic INCR + EXPIRE: đặt TTL ngay trong cùng script để tránh key
     * không hết hạn nếu process chết giữa INCR và EXPIRE.
     */
    private static final DefaultRedisScript<Long> INCR_WITH_TTL = new DefaultRedisScript<>(
            "local c = redis.call('INCR', KEYS[1]) " +
            "if c == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end " +
            "return c",
            Long.class);

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    // Public API rate limits (stricter for public endpoints)
    @Value("${rate.limit.public.requests:1000}")
    private int publicRequestLimit;

    @Value("${rate.limit.public.window:60}")
    private int publicWindowSeconds;

    // Webhook rate limits (higher for legitimate webhook traffic)
    @Value("${rate.limit.webhook.requests:5000}")
    private int webhookRequestLimit;

    @Value("${rate.limit.webhook.window:60}")
    private int webhookWindowSeconds;

    // Default rate limits for other endpoints
    @Value("${rate.limit.default.requests:2000}")
    private int defaultRequestLimit;

    @Value("${rate.limit.default.window:60}")
    private int defaultWindowSeconds;

    @Value("${rate.limit.trust-proxy-headers:true}")
    private boolean trustProxyHeaders;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Determine rate limit based on endpoint type
        RateLimitConfig config = getRateLimitConfig(path);
        
        String clientId = getClientId(request);
        String key = "rate_limit:" + config.getPrefix() + ":" + clientId;

        boolean allowed = true;
        try {
            // Atomic INCR+EXPIRE qua Lua: tránh race condition và key không TTL
            Long count = redisTemplate.execute(
                    INCR_WITH_TTL,
                    List.of(key),
                    String.valueOf(config.getWindowSeconds()));
            if (count != null && count > config.getRequestLimit()) {
                allowed = false;
            }
        } catch (Exception e) {
            // Fail-open: Redis lỗi không được chặn request
            log.error("Error in rate limiting, allowing request: {}", e.getMessage());
        }

        if (!allowed) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            ErrorResponse error = ErrorResponse.fromRateLimit(
                    "Rate limit exceeded for " + config.getPrefix() + " endpoints");
            error.setPath(path);
            objectMapper.writeValue(response.getWriter(), error);
            log.warn("Rate limit exceeded for client {} on endpoint {}", clientId, path);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private RateLimitConfig getRateLimitConfig(String path) {
        // Public AI chatbot endpoints
        if (path.startsWith("/penny/bots/") && path.contains("/chat/public")) {
            return new RateLimitConfig("public_chat", publicRequestLimit, publicWindowSeconds);
        }
        
        // Other public endpoints
        if (path.startsWith("/public/")) {
            return new RateLimitConfig("public", publicRequestLimit, publicWindowSeconds);
        }
        
        // Facebook webhook endpoints
        if (path.startsWith("/api/v1/facebook/webhook/")) {
            return new RateLimitConfig("webhook", webhookRequestLimit, webhookWindowSeconds);
        }
        
        // Default rate limit
        return new RateLimitConfig("default", defaultRequestLimit, defaultWindowSeconds);
    }

    private String getClientId(HttpServletRequest request) {
        if (trustProxyHeaders) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }

            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.trim().isEmpty()) {
                return xRealIp;
            }
        }

        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip rate limiting for actuator, health, metrics, and internal endpoints
        return path.startsWith("/actuator/") || 
               path.startsWith("/health") ||
               path.startsWith("/metrics") ||
               path.startsWith("/ws/") ||  // WebSocket endpoints
               path.startsWith("/api/internal/");  // Internal API endpoints
    }

    private static class RateLimitConfig {
        private final String prefix;
        private final int requestLimit;
        private final int windowSeconds;

        public RateLimitConfig(String prefix, int requestLimit, int windowSeconds) {
            this.prefix = prefix;
            this.requestLimit = requestLimit;
            this.windowSeconds = windowSeconds;
        }

        public String getPrefix() {
            return prefix;
        }

        public int getRequestLimit() {
            return requestLimit;
        }

        public int getWindowSeconds() {
            return windowSeconds;
        }
    }
}
