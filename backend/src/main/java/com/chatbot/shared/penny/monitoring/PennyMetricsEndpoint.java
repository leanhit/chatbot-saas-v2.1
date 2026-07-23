package com.chatbot.shared.penny.monitoring;

import com.chatbot.shared.penny.kb.KnowledgeArticleRepository;
import com.chatbot.shared.penny.repository.PennyBotRepository;
import com.chatbot.shared.penny.routing.ProviderSelector;
import com.chatbot.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PennyMetricsEndpoint — Admin metrics API for Penny Bot monitoring
 *
 * Provides aggregated metrics about bot performance, provider usage,
 * knowledge base statistics, and system health.
 */
@RestController
@RequestMapping("/api/penny/admin/metrics")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'TENANT_ADMIN')")
@ConditionalOnProperty(name = "penny.analytics.enabled", havingValue = "true", matchIfMissing = true)
public class PennyMetricsEndpoint {

    private final PennyBotRepository pennyBotRepository;
    private final KnowledgeArticleRepository knowledgeArticleRepository;
    private final ProviderSelector providerSelector;
    private final PennyMetricsService pennyMetricsService;

    /**
     * Get overall system metrics
     * GET /api/penny/admin/metrics
     */
    @GetMapping
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
     * Get metrics for a specific bot
     * GET /api/penny/admin/metrics/bot/{botId}
     */
    @GetMapping("/bot/{botId}")
    public Map<String, Object> getBotMetrics(UUID botId) {
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
        Long tenantId = SecurityUtils.getCurrentTenantId().orElseThrow(
            () -> new IllegalStateException("Tenant ID not found in security context")
        );
        long kbCount = knowledgeArticleRepository.countByBotIdAndTenantIdAndIsActiveTrue(botId, tenantId);
        metrics.put("knowledgeArticleCount", kbCount);
        
        return metrics;
    }

    /**
     * Get provider health metrics
     * GET /api/penny/admin/metrics/providers
     */
    @GetMapping("/providers")
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
     * GET /api/penny/admin/metrics/providers/costs
     */
    @GetMapping("/providers/costs")
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
     * GET /api/penny/admin/metrics/knowledge-base
     */
    @GetMapping("/knowledge-base")
    public Map<String, Object> getKnowledgeBaseMetrics() {
        log.debug("📊 Fetching knowledge base metrics");

        Map<String, Object> metrics = new HashMap<>();
        
        // Total articles count (across all bots)
        // Note: This is a simplified version - should aggregate by tenant in production
        metrics.put("totalArticles", "N/A"); // Need proper aggregation query
        
        // RAG status
        metrics.put("ragEnabled", true); // Based on configuration
        
        return metrics;
    }

    /**
     * Get latency percentile metrics
     * GET /api/penny/admin/metrics/latency
     */
    @GetMapping("/latency")
    public Map<String, Object> getLatencyMetrics() {
        log.debug("📊 Fetching latency percentile metrics");

        PennyMetricsService.MetricsSummary summary = pennyMetricsService.getMetricsSummary();
        
        Map<String, Object> metrics = new HashMap<>();
        
        // Message processing percentiles
        Map<String, Object> processingLatency = new HashMap<>();
        processingLatency.put("p50", summary.getProcessingTimeP50());
        processingLatency.put("p90", summary.getProcessingTimeP90());
        processingLatency.put("p95", summary.getProcessingTimeP95());
        processingLatency.put("p99", summary.getProcessingTimeP99());
        processingLatency.put("average", summary.getAverageProcessingTime());
        metrics.put("messageProcessing", processingLatency);
        
        // Intent analysis percentiles
        Map<String, Object> intentLatency = new HashMap<>();
        intentLatency.put("p50", summary.getIntentAnalysisP50());
        intentLatency.put("p90", summary.getIntentAnalysisP90());
        intentLatency.put("p95", summary.getIntentAnalysisP95());
        intentLatency.put("p99", summary.getIntentAnalysisP99());
        intentLatency.put("average", summary.getAverageIntentAnalysisTime());
        metrics.put("intentAnalysis", intentLatency);
        
        // Provider selection percentiles
        Map<String, Object> selectionLatency = new HashMap<>();
        selectionLatency.put("p50", summary.getProviderSelectionP50());
        selectionLatency.put("p90", summary.getProviderSelectionP90());
        selectionLatency.put("p95", summary.getProviderSelectionP95());
        selectionLatency.put("p99", summary.getProviderSelectionP99());
        selectionLatency.put("average", summary.getAverageProviderSelectionTime());
        metrics.put("providerSelection", selectionLatency);
        
        return metrics;
    }

    // ─── Private helpers ───────────────────────────────────────────────────

    private Map<String, Object> getBotMetrics() {
        Map<String, Object> botMetrics = new HashMap<>();
        
        // Total bot count
        long totalBots = pennyBotRepository.count();
        botMetrics.put("total", totalBots);
        
        // Active bots count
        // Note: Need to add active count query to repository
        botMetrics.put("active", "N/A");
        
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
