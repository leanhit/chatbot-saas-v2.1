package com.chatbot.shared.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, String> redisTemplate;

    // Public API rate limits (stricter for public endpoints)
    @Value("${rate.limit.public.requests:50}")
    private int publicRequestLimit;

    @Value("${rate.limit.public.window:60}")
    private int publicWindowSeconds;

    // Webhook rate limits (higher for legitimate webhook traffic)
    @Value("${rate.limit.webhook.requests:200}")
    private int webhookRequestLimit;

    @Value("${rate.limit.webhook.window:60}")
    private int webhookWindowSeconds;

    // Default rate limits for other endpoints
    @Value("${rate.limit.default.requests:100}")
    private int defaultRequestLimit;

    @Value("${rate.limit.default.window:60}")
    private int defaultWindowSeconds;

    @Value("${rate.limit.trust-proxy-headers:false}")
    private boolean trustProxyHeaders;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Determine rate limit based on endpoint type
        RateLimitConfig config = getRateLimitConfig(path);
        
        String clientId = getClientId(request);
        String key = "rate_limit:" + config.getPrefix() + ":" + clientId;

        try {
            String currentCount = redisTemplate.opsForValue().get(key);
            
            if (currentCount == null) {
                redisTemplate.opsForValue().set(key, "1", config.getWindowSeconds(), TimeUnit.SECONDS);
            } else {
                int count = Integer.parseInt(currentCount);
                if (count >= config.getRequestLimit()) {
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded for " + config.getPrefix() + " endpoints\"}");
                    log.warn("Rate limit exceeded for client {} on endpoint {}", clientId, path);
                    return;
                }
                redisTemplate.opsForValue().increment(key);
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Error in rate limiting: {}", e.getMessage());
            filterChain.doFilter(request, response);
        }
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
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            
            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
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
