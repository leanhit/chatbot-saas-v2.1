package com.chatbot.core.tenant.guard;

import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.tenant.model.TenantApiKey;
import com.chatbot.core.tenant.repository.TenantApiKeyRepository;
import com.chatbot.core.tenant.service.TenantApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Authenticates tenant API keys (X-API-Key header) for the /api/tenant-api/** surface.
 * Sets TenantContext so downstream tenant-scoped repositories work unchanged.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantApiKeyAuthFilter extends OncePerRequestFilter {

    private final TenantApiKeyRepository apiKeyRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/tenant-api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String rawKey = request.getHeader("X-API-Key");
        if (rawKey == null || rawKey.isBlank()) {
            sendUnauthorized(response, "Missing X-API-Key header");
            return;
        }

        TenantApiKey key = apiKeyRepository.findByKeyHash(TenantApiKeyService.hashKey(rawKey.trim()))
                .orElse(null);
        if (key == null || !key.isValid()) {
            sendUnauthorized(response, "Invalid or expired API key");
            return;
        }

        TenantContext.setTenantId(key.getTenantId());
        request.setAttribute("tenantApiKeyId", key.getId());
        request.setAttribute("tenantApiKeyRole", key.getRole());

        // Best-effort usage tracking (async-safe: save in new thread is overkill; direct save ok)
        try {
            key.recordUsage();
            apiKeyRepository.save(key);
        } catch (Exception e) {
            log.debug("Failed to record API key usage: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
