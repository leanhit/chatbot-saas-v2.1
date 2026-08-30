package com.chatbot.core.logging.filter;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import java.io.IOException;
import java.util.UUID;

/**
 * Filter to add correlation ID to MDC for distributed tracing.
 * This correlation ID will be included in all log statements via the Logstash encoder.
 */
@Component
public class CorrelationIdFilter implements Filter {

    private static final String CORRELATION_ID_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        try {
            // Generate or retrieve correlation ID
            String correlationId = generateCorrelationId(request);
            MDC.put(CORRELATION_ID_KEY, correlationId);
            
            chain.doFilter(request, response);
        } finally {
            // Clean up MDC to prevent memory leaks
            MDC.remove(CORRELATION_ID_KEY);
        }
    }

    private String generateCorrelationId(ServletRequest request) {
        // Check if correlation ID is already present in headers
        jakarta.servlet.http.HttpServletRequest httpRequest = (jakarta.servlet.http.HttpServletRequest) request;
        String existingCorrelationId = httpRequest.getHeader("X-Correlation-ID");
        
        if (existingCorrelationId != null && !existingCorrelationId.isEmpty()) {
            return existingCorrelationId;
        }
        
        // Generate new correlation ID
        return UUID.randomUUID().toString();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization if needed
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}
