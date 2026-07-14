package com.chatbot.shared.penny.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * StructuredLogger - Structured logging for Penny Bot components
 * 
 * Provides consistent structured logging with context information
 * for better observability and debugging in production.
 */
@Component
@Slf4j
public class StructuredLogger {

    /**
     * Log embedding generation
     */
    public void logEmbeddingGenerated(String botId, String tenantId, int textLength, int dimensions, long durationMs) {
        Map<String, Object> context = new HashMap<>();
        context.put("botId", botId);
        context.put("tenantId", tenantId);
        context.put("textLength", textLength);
        context.put("dimensions", dimensions);
        context.put("durationMs", durationMs);
        context.put("eventType", "embedding_generated");
        
        log.info("Embedding generated: {}", context);
    }

    /**
     * Log embedding cache hit
     */
    public void logEmbeddingCacheHit(String botId, String tenantId, int textLength) {
        Map<String, Object> context = new HashMap<>();
        context.put("botId", botId);
        context.put("tenantId", tenantId);
        context.put("textLength", textLength);
        context.put("eventType", "embedding_cache_hit");
        
        log.debug("Embedding cache hit: {}", context);
    }

    /**
     * Log embedding generation error
     */
    public void logEmbeddingError(String botId, String tenantId, String error, Exception e) {
        Map<String, Object> context = new HashMap<>();
        context.put("botId", botId);
        context.put("tenantId", tenantId);
        context.put("error", error);
        context.put("eventType", "embedding_error");
        
        log.error("Embedding generation failed: {}", context, e);
    }

    /**
     * Log RAG search
     */
    public void logRagSearch(String botId, String tenantId, String query, int resultsCount, long durationMs) {
        Map<String, Object> context = new HashMap<>();
        context.put("botId", botId);
        context.put("tenantId", tenantId);
        context.put("queryLength", query.length());
        context.put("resultsCount", resultsCount);
        context.put("durationMs", durationMs);
        context.put("eventType", "rag_search");
        
        log.info("RAG search completed: {}", context);
    }

    /**
     * Log escalation ticket created
     */
    public void logEscalationCreated(String botId, String tenantId, String userId, String reason, String priority) {
        Map<String, Object> context = new HashMap<>();
        context.put("botId", botId);
        context.put("tenantId", tenantId);
        context.put("userId", userId);
        context.put("reason", reason);
        context.put("priority", priority);
        context.put("eventType", "escalation_created");
        
        log.info("Escalation ticket created: {}", context);
    }

    /**
     * Log escalation ticket resolved
     */
    public void logEscalationResolved(String botId, String tenantId, String ticketId, String agentId, long resolutionTimeMs) {
        Map<String, Object> context = new HashMap<>();
        context.put("botId", botId);
        context.put("tenantId", tenantId);
        context.put("ticketId", ticketId);
        context.put("agentId", agentId);
        context.put("resolutionTimeMs", resolutionTimeMs);
        context.put("eventType", "escalation_resolved");
        
        log.info("Escalation ticket resolved: {}", context);
    }

    /**
     * Log circuit breaker state change
     */
    public void logCircuitBreakerState(String service, String fromState, String toState) {
        Map<String, Object> context = new HashMap<>();
        context.put("service", service);
        context.put("fromState", fromState);
        context.put("toState", toState);
        context.put("eventType", "circuit_breaker_state_change");
        
        log.warn("Circuit breaker state changed: {}", context);
    }

    /**
     * Log API call with duration
     */
    public void logApiCall(String provider, String operation, boolean success, long durationMs) {
        Map<String, Object> context = new HashMap<>();
        context.put("provider", provider);
        context.put("operation", operation);
        context.put("success", success);
        context.put("durationMs", durationMs);
        context.put("eventType", "api_call");
        
        log.info("API call completed: {}", context);
    }

    /**
     * Log knowledge base article created
     */
    public void logKbArticleCreated(String botId, String tenantId, String articleId, String category, boolean embeddingGenerated) {
        Map<String, Object> context = new HashMap<>();
        context.put("botId", botId);
        context.put("tenantId", tenantId);
        context.put("articleId", articleId);
        context.put("category", category);
        context.put("embeddingGenerated", embeddingGenerated);
        context.put("eventType", "kb_article_created");
        
        log.info("Knowledge base article created: {}", context);
    }

    /**
     * Log rate limit exceeded
     */
    public void logRateLimitExceeded(String botId, String tenantId, String userId, String limitType) {
        Map<String, Object> context = new HashMap<>();
        context.put("botId", botId);
        context.put("tenantId", tenantId);
        context.put("userId", userId);
        context.put("limitType", limitType);
        context.put("eventType", "rate_limit_exceeded");
        
        log.warn("Rate limit exceeded: {}", context);
    }
}
