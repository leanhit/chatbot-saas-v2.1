package com.chatbot.core.tenant.guard;

import com.chatbot.core.tenant.infra.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/**
 * General per-tenant API rate limit (Phase 6).
 * Sliding 1-minute window per tenant stored in Redis.
 * Runs after TenantContextInterceptor has resolved the tenant.
 * Fails open if Redis is unavailable.
 */
@Component
@Order(20)
@RequiredArgsConstructor
@Slf4j
public class TenantRateLimitFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.ratelimit.tenant.requests-per-minute:600}")
    private int requestsPerMinute;

    @Value("${app.ratelimit.enabled:true}")
    private boolean enabled;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!enabled || !request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String key = "ratelimit:api:tenant:" + tenantId;
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, Duration.ofMinutes(1));
            }
            if (count != null && count > requestsPerMinute) {
                log.warn("[RateLimit] Tenant {} exceeded API rate limit ({}/min)", tenantId, requestsPerMinute);
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"status\":\"error\",\"code\":\"RATE_LIMITED\",\"message\":\"Too many requests, please retry later\"}");
                return;
            }
        } catch (Exception e) {
            log.warn("[RateLimit] Redis unavailable, failing open for tenant {}: {}", tenantId, e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
