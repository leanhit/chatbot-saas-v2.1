package com.chatbot.config;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to populate Logback MDC with trace context from Micrometer Tracing
 * Ensures traceId and spanId are available in all log statements
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class TracingMdcFilter extends OncePerRequestFilter {

    private final Tracer tracer;

    public TracingMdcFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extract trace context from current span and populate MDC
            io.micrometer.tracing.Span currentSpan = tracer.currentSpan();
            if (currentSpan != null) {
                String traceId = currentSpan.context().traceId();
                String spanId = currentSpan.context().spanId();
                
                if (traceId != null) {
                    MDC.put("traceId", traceId);
                }
                if (spanId != null) {
                    MDC.put("spanId", spanId);
                }
            }

            // Continue filter chain
            filterChain.doFilter(request, response);

        } finally {
            // Clean up MDC to prevent memory leaks
            MDC.remove("traceId");
            MDC.remove("spanId");
        }
    }
}
