package com.chatbot.shared.penny.service;

import com.chatbot.shared.penny.analytics.AnalyticsCollector;
import com.chatbot.shared.penny.core.config.PennyProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for collecting and managing Penny metrics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PennyMetricsService {

    private final AnalyticsCollector analyticsCollector;
    private final PennyProperties properties;

    // In-memory metrics cache
    private final Map<String, Object> metricsCache = new ConcurrentHashMap<>();

    /**
     * Get current metrics
     */
    public Map<String, Object> getCurrentMetrics() {
        log.debug("Collecting current Penny metrics");
        
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        
        // Get engine metrics from AnalyticsCollector
        AnalyticsCollector.EngineMetrics engineMetrics = analyticsCollector.getEngineMetrics();
        
        // Basic metrics
        metrics.put("totalProcessed", engineMetrics.getTotalProcessed());
        metrics.put("totalErrors", engineMetrics.getTotalErrors());
        metrics.put("errorRate", engineMetrics.getErrorRate());
        metrics.put("averageProcessingTime", engineMetrics.getAverageProcessingTime());
        
        // Provider metrics
        metrics.put("providerUsage", engineMetrics.getProviderUsage());
        
        // Intent metrics
        metrics.put("intentCounts", engineMetrics.getIntentCounts());
        
        // Health metrics
        metrics.put("engineHealth", getEngineHealth());
        
        // Performance metrics
        metrics.put("performanceMetrics", getPerformanceMetrics());
        
        return metrics;
    }

    /**
     * Get metrics for specific time range
     */
    public Map<String, Object> getMetricsForTimeRange(Instant startTime, Instant endTime) {
        log.debug("Collecting metrics for time range: {} to {}", startTime, endTime);
        
        Map<String, Object> metrics = getCurrentMetrics();
        metrics.put("timeRange", Map.of(
            "startTime", startTime,
            "endTime", endTime,
            "durationHours", ChronoUnit.HOURS.between(startTime, endTime)
        ));
        
        return metrics;
    }

    /**
     * Get provider-specific metrics
     */
    public Map<String, Object> getProviderMetrics(String providerType) {
        log.debug("Collecting metrics for provider: {}", providerType);
        
        AnalyticsCollector.EngineMetrics engineMetrics = analyticsCollector.getEngineMetrics();
        
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        metrics.put("providerType", providerType);
        metrics.put("usageCount", engineMetrics.getProviderUsage().getOrDefault(providerType, 0L));
        metrics.put("successRate", calculateProviderSuccessRate(providerType));
        metrics.put("averageResponseTime", getProviderAverageResponseTime(providerType));
        
        return metrics;
    }

    /**
     * Get bot-specific metrics
     */
    public Map<String, Object> getBotMetrics(java.util.UUID botId) {
        log.debug("Collecting metrics for bot: {}", botId);
        
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        metrics.put("botId", botId);
        metrics.put("totalMessages", getBotMessageCount(botId));
        metrics.put("activeConversations", getBotActiveConversations(botId));
        metrics.put("averageResponseTime", getBotAverageResponseTime(botId));
        metrics.put("successRate", calculateBotSuccessRate(botId));
        
        return metrics;
    }

    /**
     * Get tenant-specific metrics
     */
    public Map<String, Object> getTenantMetrics(Long tenantId) {
        log.debug("Collecting metrics for tenant: {}", tenantId);
        
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        metrics.put("tenantId", tenantId);
        metrics.put("totalBots", getTenantBotCount(tenantId));
        metrics.put("activeBots", getTenantActiveBotCount(tenantId));
        metrics.put("totalMessages", getTenantMessageCount(tenantId));
        metrics.put("totalConversations", getTenantConversationCount(tenantId));
        
        return metrics;
    }

    /**
     * Reset metrics
     */
    public void resetMetrics() {
        log.info("Resetting Penny metrics");
        // Note: AnalyticsCollector doesn't have resetMetrics method
        // This would need to be implemented in AnalyticsCollector
        metricsCache.clear();
    }

    /**
     * Export metrics in different formats
     */
    public String exportMetrics(String format) {
        Map<String, Object> metrics = getCurrentMetrics();
        
        switch (format.toLowerCase()) {
            case "json":
                return exportAsJson(metrics);
            case "csv":
                return exportAsCsv(metrics);
            case "xml":
                return exportAsXml(metrics);
            default:
                return exportAsJson(metrics);
        }
    }

    // Private helper methods

    private double calculateErrorRate() {
        AnalyticsCollector.EngineMetrics engineMetrics = analyticsCollector.getEngineMetrics();
        return engineMetrics.getErrorRate();
    }

    private Map<String, Object> getEngineHealth() {
        return Map.of(
            "status", "HEALTHY",
            "uptime", System.currentTimeMillis() - System.currentTimeMillis() / 2, // Placeholder
            "memoryUsage", getMemoryUsage(),
            "cpuUsage", getCpuUsage()
        );
    }

    private Map<String, Object> getPerformanceMetrics() {
        AnalyticsCollector.EngineMetrics engineMetrics = analyticsCollector.getEngineMetrics();
        return Map.of(
            "throughput", calculateThroughput(),
            "latency", engineMetrics.getAverageProcessingTime(),
            "concurrentRequests", getConcurrentRequests()
        );
    }

    private double calculateProviderSuccessRate(String providerType) {
        // Implementation would track provider-specific success/failure rates
        return 95.0; // Placeholder
    }

    private long getProviderAverageResponseTime(String providerType) {
        // Implementation would track provider-specific response times
        return 150; // Placeholder in milliseconds
    }

    private long getBotMessageCount(java.util.UUID botId) {
        // Implementation would query database or cache for bot-specific metrics
        return 1000; // Placeholder
    }

    private long getBotActiveConversations(java.util.UUID botId) {
        // Implementation would track active conversations per bot
        return 50; // Placeholder
    }

    private double getBotAverageResponseTime(java.util.UUID botId) {
        // Implementation would calculate average response time per bot
        return 200.0; // Placeholder
    }

    private double calculateBotSuccessRate(java.util.UUID botId) {
        // Implementation would calculate success rate per bot
        return 92.5; // Placeholder
    }

    private long getTenantBotCount(Long tenantId) {
        // Implementation would query database for tenant bot count
        return 5; // Placeholder
    }

    private long getTenantActiveBotCount(Long tenantId) {
        // Implementation would query database for active tenant bots
        return 3; // Placeholder
    }

    private long getTenantMessageCount(Long tenantId) {
        // Implementation would aggregate message count per tenant
        return 10000; // Placeholder
    }

    private long getTenantConversationCount(Long tenantId) {
        // Implementation would aggregate conversation count per tenant
        return 500; // Placeholder
    }

    private double calculateThroughput() {
        // Implementation would calculate messages per second
        return 25.5; // Placeholder
    }

    private int getConcurrentRequests() {
        // Implementation would track concurrent requests
        return 10; // Placeholder
    }

    private Map<String, Object> getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        return Map.of(
            "total", totalMemory,
            "used", usedMemory,
            "free", freeMemory,
            "percentage", (double) usedMemory / totalMemory * 100
        );
    }

    private double getCpuUsage() {
        // Implementation would use system monitoring library
        return 45.5; // Placeholder percentage
    }

    private String exportAsJson(Map<String, Object> metrics) {
        // Simple JSON export (in production, use proper JSON library)
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        
        metrics.forEach((key, value) -> {
            json.append("  \"").append(key).append("\": ");
            if (value instanceof Map) {
                json.append(((Map<?, ?>) value).toString());
            } else if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else {
                json.append(value);
            }
            json.append(",\n");
        });
        
        // Remove trailing comma
        if (json.charAt(json.length() - 2) == ',') {
            json.deleteCharAt(json.length() - 2);
        }
        
        json.append("}");
        return json.toString();
    }

    private String exportAsCsv(Map<String, Object> metrics) {
        StringBuilder csv = new StringBuilder();
        csv.append("Metric,Value\n");
        
        metrics.forEach((key, value) -> {
            csv.append(key).append(",").append(value).append("\n");
        });
        
        return csv.toString();
    }

    private String exportAsXml(Map<String, Object> metrics) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<metrics>\n");
        
        metrics.forEach((key, value) -> {
            xml.append("  <metric name=\"").append(key).append("\">");
            xml.append(value);
            xml.append("</metric>\n");
        });
        
        xml.append("</metrics>");
        return xml.toString();
    }
}
