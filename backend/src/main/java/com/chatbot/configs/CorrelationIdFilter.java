package com.chatbot.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Try to get correlation ID from request header
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        
        // If not present, generate a new one
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = generateCorrelationId();
        }
        
        // Add to MDC for logging
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        
        try {
            // Add correlation ID to response header
            response.setHeader(CORRELATION_ID_HEADER, correlationId);
            
            // Continue the filter chain
            filterChain.doFilter(request, response);
        } finally {
            // Clean up MDC to prevent memory leaks
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }

    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
