package com.chatbot.core.tenant.guard;

import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.shared.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    private final ObjectMapper objectMapper;

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
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                ErrorResponse error = ErrorResponse.fromRateLimit("Too many requests, please retry later");
                error.setPath(request.getRequestURI());
                objectMapper.writeValue(response.getWriter(), error);
                return;
            }
        } catch (Exception e) {
            log.warn("[RateLimit] Redis unavailable, failing open for tenant {}: {}", tenantId, e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
