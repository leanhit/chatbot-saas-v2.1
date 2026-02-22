package com.chatbot.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${rate.limit.requests:100}")
    private int requestLimit;

    @Value("${rate.limit.window:60}")
    private int windowSeconds;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String clientId = getClientId(request);
        String key = "rate_limit:" + clientId;

        try {
            String currentCount = redisTemplate.opsForValue().get(key);
            
            if (currentCount == null) {
                redisTemplate.opsForValue().set(key, "1", windowSeconds, TimeUnit.SECONDS);
            } else {
                int count = Integer.parseInt(currentCount);
                if (count >= requestLimit) {
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    response.getWriter().write("Rate limit exceeded");
                    return;
                }
                redisTemplate.opsForValue().increment(key);
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
        }
    }

    private String getClientId(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/") || 
               path.startsWith("/health") ||
               path.startsWith("/metrics");
    }
}
