package com.chatbot.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.HashMap;

/**
 * Structured Logging Helper
 * 
 * Provides consistent structured logging with key-value pairs for ELK parsing.
 * Automatically includes correlation ID, trace ID, span ID, and tenant ID from MDC.
 * 
 * Usage:
 * <pre>
 * StructuredLogging.info("User logged in", "userId", "123", "email", "user@example.com");
 * StructuredLogging.error("Payment failed", "orderId", "456", "error", "Insufficient funds");
 * StructuredLogging.debug("Cache hit", "key", "user:123");
 * </pre>
 */
@Component
@Slf4j
public class StructuredLogging {

    // MDC keys
    private static final String CORRELATION_ID = "correlationId";
    private static final String TRACE_ID = "traceId";
    private static final String SPAN_ID = "spanId";
    private static final String TENANT_ID = "tenantId";

    /**
     * Log info level with structured key-value pairs
     */
    public static void info(String message, Object... keyValuePairs) {
        if (log.isInfoEnabled()) {
            Map<String, Object> structuredData = buildStructuredData(keyValuePairs);
            log.info("{} {}", message, structuredData);
        }
    }

    /**
     * Log warn level with structured key-value pairs
     */
    public static void warn(String message, Object... keyValuePairs) {
        if (log.isWarnEnabled()) {
            Map<String, Object> structuredData = buildStructuredData(keyValuePairs);
            log.warn("{} {}", message, structuredData);
        }
    }

    /**
     * Log error level with structured key-value pairs
     */
    public static void error(String message, Object... keyValuePairs) {
        if (log.isErrorEnabled()) {
            Map<String, Object> structuredData = buildStructuredData(keyValuePairs);
            log.error("{} {}", message, structuredData);
        }
    }

    /**
     * Log error level with exception and structured key-value pairs
     */
    public static void error(String message, Throwable throwable, Object... keyValuePairs) {
        if (log.isErrorEnabled()) {
            Map<String, Object> structuredData = buildStructuredData(keyValuePairs);
            log.error("{} {} - Error: {}", message, structuredData, throwable.getMessage(), throwable);
        }
    }

    /**
     * Log debug level with structured key-value pairs (conditional)
     * Only logs if debug level is enabled to avoid string concatenation overhead
     */
    public static void debug(String message, Object... keyValuePairs) {
        if (log.isDebugEnabled()) {
            Map<String, Object> structuredData = buildStructuredData(keyValuePairs);
            log.debug("{} {}", message, structuredData);
        }
    }

    /**
     * Log debug level with conditional check for high-frequency operations
     * Use this for operations that may be called many times per second
     */
    public static void debugConditional(String message, Object... keyValuePairs) {
        // Only log if debug is enabled AND correlation ID is present (for debugging specific requests)
        if (log.isDebugEnabled() && MDC.get(CORRELATION_ID) != null) {
            Map<String, Object> structuredData = buildStructuredData(keyValuePairs);
            log.debug("{} {}", message, structuredData);
        }
    }

    /**
     * Build structured data map from key-value pairs
     * Automatically includes MDC values (correlationId, traceId, spanId, tenantId)
     */
    private static Map<String, Object> buildStructuredData(Object... keyValuePairs) {
        Map<String, Object> structuredData = new HashMap<>();

        // Add MDC values if present
        String correlationId = MDC.get(CORRELATION_ID);
        if (correlationId != null) {
            structuredData.put(CORRELATION_ID, correlationId);
        }

        String traceId = MDC.get(TRACE_ID);
        if (traceId != null) {
            structuredData.put(TRACE_ID, traceId);
        }

        String spanId = MDC.get(SPAN_ID);
        if (spanId != null) {
            structuredData.put(SPAN_ID, spanId);
        }

        String tenantId = MDC.get(TENANT_ID);
        if (tenantId != null) {
            structuredData.put(TENANT_ID, tenantId);
        }

        // Add custom key-value pairs
        if (keyValuePairs != null && keyValuePairs.length > 0) {
            for (int i = 0; i < keyValuePairs.length; i += 2) {
                if (i + 1 < keyValuePairs.length) {
                    String key = keyValuePairs[i].toString();
                    Object value = keyValuePairs[i + 1];
                    structuredData.put(key, value);
                }
            }
        }

        return structuredData;
    }

    /**
     * Get current correlation ID from MDC
     */
    public static String getCorrelationId() {
        return MDC.get(CORRELATION_ID);
    }

    /**
     * Get current trace ID from MDC
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }

    /**
     * Get current tenant ID from MDC
     */
    public static String getTenantId() {
        return MDC.get(TENANT_ID);
    }
}
