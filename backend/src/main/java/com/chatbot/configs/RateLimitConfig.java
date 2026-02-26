package com.chatbot.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Configuration
public class RateLimitConfig {

    private static final int RATE_LIMIT_REQUESTS = 100;
    private static final int RATE_LIMIT_WINDOW_MINUTES = 15;
    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> rateLimitStore = new ConcurrentHashMap<>();

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> rateLimitFilter() {
        FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RateLimitFilter());
        registration.addUrlPatterns("/api/auth/*", "/api/tenants", "/api/v1/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    public static class RateLimitFilter extends OncePerRequestFilter {
        
        @Override
        protected void doFilterInternal(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      FilterChain filterChain) throws ServletException, IOException {
            
            String clientIp = getClientIpAddress(request);
            String key = clientIp + ":" + request.getRequestURI();
            
            if (isRateLimited(key)) {
                response.setStatus(429);
                response.getWriter().write("{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded\"}");
                response.setContentType("application/json");
                return;
            }
            
            filterChain.doFilter(request, response);
        }
        
        private boolean isRateLimited(String key) {
            long currentTime = System.currentTimeMillis();
            long windowStart = currentTime - TimeUnit.MINUTES.toMillis(RATE_LIMIT_WINDOW_MINUTES);
            
            ConcurrentHashMap<String, Long> requests = rateLimitStore.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
            
            // Clean old requests
            requests.entrySet().removeIf(entry -> entry.getValue() < windowStart);
            
            // Check rate limit
            if (requests.size() >= RATE_LIMIT_REQUESTS) {
                return true;
            }
            
            // Add current request
            requests.put(String.valueOf(currentTime), currentTime);
            return false;
        }
        
        private String getClientIpAddress(HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
                return xForwardedFor.split(",")[0].trim();
            }
            
            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
                return xRealIp;
            }
            
            return request.getRemoteAddr();
        }
    }
}
