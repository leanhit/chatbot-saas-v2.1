package com.chatbot.core.simplepayment.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimiterInterceptor implements HandlerInterceptor {

    private final SimplePaymentRateLimitConfig rateLimitConfig;
    
    // Simple in-memory rate limiter - in production, use Redis
    private final ConcurrentHashMap<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();
    private final java.util.concurrent.atomic.AtomicLong lastMapCleanup = new java.util.concurrent.atomic.AtomicLong(System.currentTimeMillis());
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!rateLimitConfig.isEnabled()) {
            return true;
        }
        
        String clientIp = getClientIp(request);
        String path = request.getRequestURI();
        
        // Determine if this is a public endpoint
        boolean isPublicEndpoint = path.startsWith("/api/public/simple-payment");
        
        int requestsPerMinute = isPublicEndpoint ? 
            rateLimitConfig.getPublicEndpoints().getRequestsPerMinute() :
            rateLimitConfig.getAuthenticatedEndpoints().getRequestsPerMinute();
        
        int burstCapacity = isPublicEndpoint ? 
            rateLimitConfig.getPublicEndpoints().getBurstCapacity() :
            rateLimitConfig.getAuthenticatedEndpoints().getBurstCapacity();
        
        long currentTime = System.currentTimeMillis();
        long oneMinuteAgo = currentTime - 60000;
        
        // Map-wide cleanup every 5 minutes to prevent memory leaks
        if (currentTime - lastMapCleanup.get() > 300000) {
            if (lastMapCleanup.compareAndSet(lastMapCleanup.get(), currentTime)) {
                rateLimitMap.entrySet().removeIf(entry -> currentTime - entry.getValue().getLastAccessTime() > 60000);
            }
        }
        
        // Check rate limit
        RateLimitInfo info = rateLimitMap.computeIfAbsent(clientIp, k -> new RateLimitInfo());
        
        // Clean up old requests
        info.cleanup(oneMinuteAgo);
        
        if (info.getRequestCount() >= requestsPerMinute) {
            log.warn("Rate limit exceeded for IP: {}, Path: {}, Count: {}", clientIp, path, info.getRequestCount());
            response.setStatus(429); // HTTP 429 Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Rate limit exceeded. Please try again later.\"}");
            return false;
        }

        if (info.getRequestCount() >= burstCapacity) {
            log.warn("Burst capacity exceeded for IP: {}, Path: {}, Count: {}", clientIp, path, info.getRequestCount());
            response.setStatus(429); // HTTP 429 Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests. Please slow down.\"}");
            return false;
        }
        
        info.addRequest(currentTime);
        return true;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        // Handle multiple IPs in X-Forwarded-For
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }
    
    private static class RateLimitInfo {
        private final AtomicInteger requestCount = new AtomicInteger(0);
        private volatile long lastCleanupTime = System.currentTimeMillis();
        private volatile long lastAccessTime = System.currentTimeMillis();
        
        public void addRequest(long timestamp) {
            requestCount.incrementAndGet();
            lastAccessTime = timestamp;
        }
        
        public int getRequestCount() {
            return requestCount.get();
        }
        
        public long getLastAccessTime() {
            return lastAccessTime;
        }
        
        public void cleanup(long cutoffTime) {
            long now = System.currentTimeMillis();
            // Only cleanup every 10 seconds to avoid performance impact
            if (now - lastCleanupTime < 10000) {
                return;
            }
            
            // In a real implementation, we would track individual request timestamps
            // For simplicity, we just reset the counter periodically
            if (now - lastCleanupTime >= 60000) {
                requestCount.set(0);
                lastCleanupTime = now;
            }
        }
    }
}
