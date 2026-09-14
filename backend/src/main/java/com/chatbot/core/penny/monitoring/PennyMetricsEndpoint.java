package com.chatbot.core.penny.monitoring;

import com.chatbot.core.penny.kb.KnowledgeArticleRepository;
import com.chatbot.core.penny.repository.PennyBotRepository;
import com.chatbot.core.penny.routing.ProviderSelector;
import com.chatbot.shared.security.SecurityUtils;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PennyMetricsEndpoint — Admin metrics and circuit breaker API for Penny Bot monitoring
 *
 * Provides aggregated metrics about bot performance, provider usage,
 * knowledge base statistics, circuit breaker statuses, and system health.
 */
@RestController
@RequestMapping("/api/penny/admin")
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'TENANT_ADMIN', 'OWNER', 'EDITOR')")
public class PennyMetricsEndpoint {

    private final PennyBotRepository pennyBotRepository;
    private final KnowledgeArticleRepository knowledgeArticleRepository;
    private final ProviderSelector providerSelector;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    public PennyMetricsEndpoint(
            PennyBotRepository pennyBotRepository,
            KnowledgeArticleRepository knowledgeArticleRepository,
            ProviderSelector providerSelector,
            @Autowired(required = false) CircuitBreakerRegistry circuitBreakerRegistry) {
        this.pennyBotRepository = pennyBotRepository;
        this.knowledgeArticleRepository = knowledgeArticleRepository;
        this.providerSelector = providerSelector;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    /**
     * Get overall system metrics
     * GET /api/penny/admin/metrics or GET /api/penny/admin
     */
    @GetMapping({"/metrics", ""})
    public Map<String, Object> getSystemMetrics() {
        log.debug("📊 Fetching Penny system metrics");

        Map<String, Object> metrics = new HashMap<>();
        
        // Bot statistics
        metrics.put("bots", getBotMetrics());
        
        // Knowledge base statistics
        metrics.put("knowledgeBase", getKnowledgeBaseMetrics());
        
        // Provider health
        metrics.put("providers", getProviderMetrics());
        
        // System info
        metrics.put("system", getSystemInfo());
        
        return metrics;
    }

    /**
     * Get metrics summary
     * GET /api/penny/admin/metrics/summary or GET /api/penny/admin/summary
     */
    @GetMapping({"/metrics/summary", "/summary"})
    public Map<String, Object> getMetricsSummary() {
        log.debug("📊 Fetching Penny metrics summary");
        return getSystemMetrics();
    }

    /**
     * Get metrics for a specific bot
     * GET /api/penny/admin/metrics/bot/{botId} or GET /api/penny/admin/bot/{botId}
     */
    @GetMapping({"/metrics/bot/{botId}", "/bot/{botId}"})
    public Map<String, Object> getBotMetrics(@PathVariable UUID botId) {
        log.debug("📊 Fetching metrics for bot: {}", botId);

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("botId", botId);
        
        // Bot info
        pennyBotRepository.findById(botId).ifPresent(bot -> {
            metrics.put("botName", bot.getBotName());
            metrics.put("botType", bot.getBotType());
            metrics.put("isActive", bot.isActive());
            metrics.put("isEnabled", bot.isEnabled());
            metrics.put("lastUsedAt", bot.getLastUsedAt());
            metrics.put("confidenceThreshold", bot.getConfidenceThreshold());
        });
        
        // Knowledge base count for this bot
        Long tenantId = SecurityUtils.getCurrentTenantId().orElse(null);
        if (tenantId != null) {
            long kbCount = knowledgeArticleRepository.countByBotIdAndTenantIdAndIsActiveTrue(botId, tenantId);
            metrics.put("knowledgeArticleCount", kbCount);
        } else {
            metrics.put("knowledgeArticleCount", 0);
        }
        
        return metrics;
    }

    /**
     * Get provider health metrics
     * GET /api/penny/admin/metrics/providers or GET /api/penny/admin/providers
     */
    @GetMapping({"/metrics/providers", "/providers"})
    public Map<String, Object> getProviderMetrics() {
        log.debug("📊 Fetching provider health metrics");

        Map<String, Object> metrics = new HashMap<>();
        
        Map<String, ProviderSelector.ProviderHealth> healthMap = providerSelector.getAllProviderHealth();
        
        for (Map.Entry<String, ProviderSelector.ProviderHealth> entry : healthMap.entrySet()) {
            Map<String, Object> providerInfo = new HashMap<>();
            ProviderSelector.ProviderHealth health = entry.getValue();
            
            providerInfo.put("healthy", health.isHealthyStatus());
            providerInfo.put("lastMessage", health.getLastMessage());
            providerInfo.put("lastCheck", health.getLastCheck());
            providerInfo.put("consecutiveFailures", health.getConsecutiveFailures());
            
            metrics.put(entry.getKey(), providerInfo);
        }
        
        return metrics;
    }

    /**
     * Get provider cost metrics
     * GET /api/penny/admin/metrics/providers/costs or GET /api/penny/admin/providers/costs
     */
    @GetMapping({"/metrics/providers/costs", "/providers/costs"})
    public Map<String, Object> getProviderCostMetrics() {
        log.debug("📊 Fetching provider cost metrics");

        Map<String, Object> metrics = new HashMap<>();
        
        // Get cost metadata for all providers
        for (ProviderSelector.ProviderType type : ProviderSelector.ProviderType.values()) {
            Map<String, Object> costInfo = new HashMap<>();
            
            costInfo.put("displayName", type.getDisplayName());
            costInfo.put("costPer1kTokens", type.getCostPer1kTokens());
            costInfo.put("costPerRequest", type.getCostPerRequest());
            
            // Sample cost estimation for a 100-character message
            String sampleMessage = "a".repeat(100);
            ProviderSelector.ProviderCost sampleCost = providerSelector.estimateProviderCost(type, sampleMessage);
            costInfo.put("sampleCost100Chars", sampleCost.getEstimatedCost());
            costInfo.put("sampleTokens100Chars", sampleCost.getEstimatedTokens());
            
            metrics.put(type.name(), costInfo);
        }
        
        return metrics;
    }

    /**
     * Get knowledge base statistics
     * GET /api/penny/admin/metrics/knowledge-base or GET /api/penny/admin/knowledge-base
     */
    @GetMapping({"/metrics/knowledge-base", "/knowledge-base"})
    public Map<String, Object> getKnowledgeBaseMetrics() {
        log.debug("📊 Fetching knowledge base metrics");

        Map<String, Object> metrics = new HashMap<>();
        
        metrics.put("totalArticles", knowledgeArticleRepository.count());
        metrics.put("ragEnabled", true);
        
        return metrics;
    }

    /**
     * Get circuit breaker status
     * GET /api/penny/admin/circuit-breaker/status
     */
    @GetMapping("/circuit-breaker/status")
    public Map<String, Object> getCircuitBreakerStatus() {
        log.debug("📊 Fetching Circuit Breaker status");

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> breakers = new HashMap<>();

        if (circuitBreakerRegistry != null) {
            circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
                Map<String, Object> cbInfo = new HashMap<>();
                cbInfo.put("state", cb.getState().name());
                cbInfo.put("failureRate", cb.getMetrics().getFailureRate());
                cbInfo.put("slowCallRate", cb.getMetrics().getSlowCallRate());
                cbInfo.put("bufferedCalls", cb.getMetrics().getNumberOfBufferedCalls());
                cbInfo.put("failedCalls", cb.getMetrics().getNumberOfFailedCalls());
                cbInfo.put("successfulCalls", cb.getMetrics().getNumberOfSuccessfulCalls());
                cbInfo.put("notPermittedCalls", cb.getMetrics().getNumberOfNotPermittedCalls());
                breakers.put(cb.getName(), cbInfo);
            });
        }

        // Also merge provider health status from providerSelector
        if (providerSelector != null) {
            Map<String, ProviderSelector.ProviderHealth> healthMap = providerSelector.getAllProviderHealth();
            for (Map.Entry<String, ProviderSelector.ProviderHealth> entry : healthMap.entrySet()) {
                if (!breakers.containsKey(entry.getKey())) {
                    Map<String, Object> cbInfo = new HashMap<>();
                    cbInfo.put("state", entry.getValue().isHealthyStatus() ? "CLOSED" : "OPEN");
                    cbInfo.put("consecutiveFailures", entry.getValue().getConsecutiveFailures());
                    cbInfo.put("lastMessage", entry.getValue().getLastMessage());
                    breakers.put(entry.getKey(), cbInfo);
                }
            }
        }

        response.put("status", "success");
        response.put("circuitBreakers", breakers);
        response.put("timestamp", LocalDateTime.now().toString());

        return response;
    }

    /**
     * Get analytics events
     * GET /api/penny/admin/analytics/events
     */
    @GetMapping("/analytics/events")
    public Map<String, Object> getAnalyticsEvents(
            @RequestParam(required = false) UUID botId,
            @RequestParam(defaultValue = "24h") String timeRange,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("📊 Fetching analytics events for bot: {}", botId);

        Map<String, Object> response = new HashMap<>();
        response.put("content", Collections.emptyList());
        response.put("events", Collections.emptyList());
        response.put("totalElements", 0);
        response.put("totalPages", 1);
        response.put("page", page);
        response.put("size", size);
        return response;
    }

    /**
     * Get analytics summary
     * GET /api/penny/admin/analytics/summary
     */
    @GetMapping("/analytics/summary")
    public Map<String, Object> getAnalyticsSummary(
            @RequestParam(required = false) UUID botId,
            @RequestParam(defaultValue = "24h") String timeRange) {
        log.debug("📊 Fetching analytics summary for bot: {}", botId);

        Map<String, Object> response = new HashMap<>();
        response.put("totalMessages", 0);
        response.put("totalRequests", 0);
        response.put("successfulRequests", 0);
        response.put("totalErrors", 0);
        response.put("failedRequests", 0);
        response.put("errorRate", 0.0);
        response.put("averageProcessingTime", 0.0);
        response.put("averageLatencyMs", 0.0);
        response.put("mostUsedProvider", "N/A");
        response.put("mostCommonIntent", "N/A");
        response.put("intentCounts", Collections.emptyMap());
        return response;
    }

    // ─── Private helpers ───────────────────────────────────────────────────

    private Map<String, Object> getBotMetrics() {
        Map<String, Object> botMetrics = new HashMap<>();
        
        long totalBots = pennyBotRepository.count();
        botMetrics.put("total", totalBots);
        botMetrics.put("active", totalBots);
        
        return botMetrics;
    }

    private Map<String, Object> getSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();
        
        systemInfo.put("timestamp", LocalDateTime.now());
        systemInfo.put("version", "1.0.0");
        systemInfo.put("environment", System.getenv().getOrDefault("SPRING_PROFILES_ACTIVE", "default"));
        
        return systemInfo;
    }
}

