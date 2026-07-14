package com.chatbot.shared.penny.monitoring;

import com.chatbot.shared.penny.kb.KnowledgeArticleRepository;
import com.chatbot.shared.penny.repository.PennyBotRepository;
import com.chatbot.shared.penny.routing.ProviderSelector;
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
        // Note: tenantId should come from security context
        Long tenantId = 1L; // TODO: Get from security context
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
