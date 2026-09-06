package com.chatbot.core.payment.merchant.filter;

import com.chatbot.core.payment.merchant.model.MerchantApiKey;
import com.chatbot.core.payment.merchant.repository.MerchantApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class MerchantAuthFilter extends OncePerRequestFilter {

    private final MerchantApiKeyRepository merchantApiKeyRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.endsWith("/health") || path.contains("/public/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Only apply to merchant API endpoints
        String path = request.getRequestURI();
        if (!path.startsWith("/api/merchant/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract API key from header
        String apiKey = request.getHeader("X-API-Key");
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            sendUnauthorizedResponse(response, "Missing API key");
            return;
        }

        // Validate API key
        MerchantApiKey merchantKey = merchantApiKeyRepository.findByApiKey(apiKey)
                .orElse(null);

        if (merchantKey == null) {
            sendUnauthorizedResponse(response, "Invalid API key");
            return;
        }

        if (!merchantKey.isValid()) {
            sendUnauthorizedResponse(response, "API key is inactive or expired");
            return;
        }

        // Check rate limit
        if (merchantKey.getRateLimitPerMinute() != null) {
            // Rate limiting would be implemented here with Redis
            log.debug("Rate limit check for merchant: {}", merchantKey.getName());
        }

        // Record usage
        merchantKey.recordUsage();
        merchantApiKeyRepository.save(merchantKey);

        // Add merchant context to request
        request.setAttribute("merchantId", merchantKey.getId());
        request.setAttribute("tenantId", merchantKey.getTenantId());
        request.setAttribute("merchantName", merchantKey.getName());

        log.info("🔑 Merchant authenticated: {} (ID: {})", merchantKey.getName(), merchantKey.getId());

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        log.warn("⚠️ Merchant authentication failed: {}", message);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
