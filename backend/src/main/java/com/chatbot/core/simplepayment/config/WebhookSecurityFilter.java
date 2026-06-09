package com.chatbot.core.simplepayment.config;

import com.chatbot.core.simplepayment.service.WebhookSignatureService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookSecurityFilter extends OncePerRequestFilter {

    private final WebhookSignatureService webhookSignatureService;

    @Value("${simplepayment.webhook.enabled:true}")
    private boolean webhookEnabled;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Only apply to webhook endpoints
        String path = request.getRequestURI();
        if (!path.startsWith("/api/webhook/")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!webhookEnabled) {
            log.warn("Webhook is disabled, rejecting request");
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getWriter().write("{\"error\":\"Webhook is disabled\"}");
            return;
        }

        // Wrap request to cache body
        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);

        // Read request body for signature verification
        String body = new BufferedReader(new InputStreamReader(wrappedRequest.getInputStream(), StandardCharsets.UTF_8))
            .lines()
            .collect(Collectors.joining("\n"));

        // Verify signature
        boolean isValid = webhookSignatureService.verifySignature(wrappedRequest, body);

        if (!isValid) {
            log.warn("Invalid webhook signature for path: {}", path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Invalid webhook signature\"}");
            return;
        }

        // Pass the wrapped request through
        filterChain.doFilter(wrappedRequest, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/webhook/");
    }
}
