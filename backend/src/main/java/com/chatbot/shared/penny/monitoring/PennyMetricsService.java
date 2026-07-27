package com.chatbot.shared.penny.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Penny Metrics Service - Integration with Micrometer for monitoring
 * Provides metrics for Prometheus/Grafana monitoring
 */
@Service
@Slf4j
public class PennyMetricsService {

    private final MeterRegistry meterRegistry;
    
    // Counters
    private final Counter messageProcessedCounter;
    private final Counter messageErrorCounter;
    private final Counter customRuleMatchedCounter;
    private final Counter templateMatchedCounter;
    private final Counter providerFallbackCounter;
    
    // Timers
    private final Timer messageProcessingTimer;
    private final Timer intentAnalysisTimer;
    private final Timer providerSelectionTimer;
    
    // Gauges
    private final AtomicLong activeConversations = new AtomicLong(0);
    private final AtomicLong activeBots = new AtomicLong(0);
    
    // Provider-specific metrics
    private final ConcurrentHashMap<String, AtomicLong> providerUsage = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> providerTimers = new ConcurrentHashMap<>();

    public PennyMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize counters
        this.messageProcessedCounter = Counter.builder("penny.messages.processed")
            .description("Total number of messages processed")
            .register(meterRegistry);
            
        this.messageErrorCounter = Counter.builder("penny.messages.errors")
            .description("Total number of message processing errors")
            .register(meterRegistry);
            
        this.customRuleMatchedCounter = Counter.builder("penny.rules.matched")
            .description("Total number of custom rules matched")
            .register(meterRegistry);
            
        this.templateMatchedCounter = Counter.builder("penny.templates.matched")
            .description("Total number of response templates matched")
            .register(meterRegistry);
            
        this.providerFallbackCounter = Counter.builder("penny.providers.fallback")
            .description("Total number of provider fallbacks")
            .register(meterRegistry);
        
        // Initialize timers
        this.messageProcessingTimer = Timer.builder("penny.message.processing.time")
            .description("Message processing time")
            .register(meterRegistry);
            
        this.intentAnalysisTimer = Timer.builder("penny.intent.analysis.time")
            .description("Intent analysis time")
            .register(meterRegistry);
            
        this.providerSelectionTimer = Timer.builder("penny.provider.selection.time")
            .description("Provider selection time")
            .register(meterRegistry);
        
        // Initialize gauges
        Gauge.builder("penny.conversations.active", activeConversations, AtomicLong::get)
            .description("Number of active conversations")
            .register(meterRegistry);
            
        Gauge.builder("penny.bots.active", activeBots, AtomicLong::get)
            .description("Number of active bots")
            .register(meterRegistry);
        
        log.info("✅ Penny metrics service initialized with Micrometer");
    }

    /**
     * Record message processed
     */
    public void recordMessageProcessed(String providerType) {
        messageProcessedCounter.increment();
        
        // Provider-specific counter
        providerUsage.computeIfAbsent(providerType, k -> {
            AtomicLong counter = new AtomicLong(0);
            Gauge.builder("penny.provider.usage", counter, AtomicLong::get)
                .tag("provider", providerType)
                .description("Provider usage count")
                .register(meterRegistry);
            return counter;
        }).incrementAndGet();
    }

    /**
     * Record message error
     */
    public void recordMessageError(String errorType) {
        Counter.builder("penny.messages.errors")
            .tag("error_type", errorType)
            .description("Message processing errors by type")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record custom rule matched
     */
    public void recordCustomRuleMatched(String ruleName) {
        Counter.builder("penny.rules.matched")
            .tag("rule_name", ruleName)
            .description("Custom rules matched by name")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record template matched
     */
    public void recordTemplateMatched(String templateName) {
        Counter.builder("penny.templates.matched")
            .tag("template_name", templateName)
            .description("Response templates matched by name")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record provider fallback
     */
    public void recordProviderFallback(String fromProvider, String toProvider) {
        Counter.builder("penny.providers.fallback")
            .tag("from", fromProvider)
            .tag("to", toProvider)
            .description("Provider fallbacks")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record message processing time
     */
    public Timer.Sample startMessageProcessingTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * Stop message processing timer
     */
    public void stopMessageProcessingTimer(Timer.Sample sample) {
        sample.stop(messageProcessingTimer);
    }

    /**
     * Record intent analysis time
     */
    public Timer.Sample startIntentAnalysisTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * Stop intent analysis timer
     */
    public void stopIntentAnalysisTimer(Timer.Sample sample) {
        sample.stop(intentAnalysisTimer);
    }

    /**
     * Record provider selection time
     */
    public Timer.Sample startProviderSelectionTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * Stop provider selection timer
     */
    public void stopProviderSelectionTimer(Timer.Sample sample) {
        sample.stop(providerSelectionTimer);
    }

    /**
     * Record provider-specific processing time
     */
    public Timer.Sample startProviderTimer(String providerType) {
        providerTimers.computeIfAbsent(providerType, k -> 
            Timer.builder("penny.provider.processing.time")
                .tag("provider", providerType)
                .description("Provider processing time")
                .register(meterRegistry)
        );
        return Timer.start(meterRegistry);
    }

    /**
     * Stop provider timer
     */
    public void stopProviderTimer(Timer.Sample sample, String providerType) {
        Timer timer = providerTimers.get(providerType);
        if (timer != null) {
            sample.stop(timer);
        }
    }

    /**
     * Increment active conversations
     */
    public void incrementActiveConversations() {
        activeConversations.incrementAndGet();
    }

    /**
     * Decrement active conversations
     */
    public void decrementActiveConversations() {
        activeConversations.decrementAndGet();
    }

    /**
     * Set active bots count
     */
    public void setActiveBots(long count) {
        activeBots.set(count);
    }

    /**
     * Record intent detected
     */
    public void recordIntentDetected(String intent) {
        Counter.builder("penny.intents.detected")
            .tag("intent", intent)
            .description("Intent detection count")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record entity extracted
     */
    public void recordEntityExtracted(String entityType) {
        Counter.builder("penny.entities.extracted")
            .tag("entity_type", entityType)
            .description("Entity extraction count")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record context cache hit
     */
    public void recordContextCacheHit() {
        Counter.builder("penny.context.cache.hits")
            .description("Context cache hits")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record context cache miss
     */
    public void recordContextCacheMiss() {
        Counter.builder("penny.context.cache.misses")
            .description("Context cache misses")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record circuit breaker state change
     */
    public void recordCircuitBreakerState(String provider, String state) {
        Gauge.builder("penny.circuitbreaker.state", new AtomicLong(state.equals("OPEN") ? 1 : 0), AtomicLong::get)
            .tag("provider", provider)
            .description("Circuit breaker state (1=OPEN, 0=CLOSED)")
            .register(meterRegistry);
    }

    /**
     * Get current metrics summary
     */
    public MetricsSummary getMetricsSummary() {
        return MetricsSummary.builder()
            .messagesProcessed(messageProcessedCounter.count())
            .messageErrors(messageErrorCounter.count())
            .customRulesMatched(customRuleMatchedCounter.count())
            .templatesMatched(templateMatchedCounter.count())
            .providerFallbacks(providerFallbackCounter.count())
            .activeConversations(activeConversations.get())
            .activeBots(activeBots.get())
            .averageProcessingTime(messageProcessingTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS))
            .averageIntentAnalysisTime(intentAnalysisTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS))
            .averageProviderSelectionTime(providerSelectionTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS))
            .build();
    }

    /**
     * Get current metrics (for PennyManagementController compatibility)
     */
    public Map<String, Object> getCurrentMetrics() {
        MetricsSummary summary = getMetricsSummary();
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        metrics.put("totalProcessed", summary.getMessagesProcessed());
        metrics.put("totalErrors", summary.getMessageErrors());
        metrics.put("errorRate", summary.getMessageErrors() / (summary.getMessagesProcessed() > 0 ? summary.getMessagesProcessed() : 1));
        metrics.put("averageProcessingTime", summary.getAverageProcessingTime());
        metrics.put("providerUsage", providerUsage);
        metrics.put("activeConversations", summary.getActiveConversations());
        metrics.put("activeBots", summary.getActiveBots());
        return metrics;
    }

    /**
     * Get metrics for time range (for PennyManagementController compatibility)
     */
    public Map<String, Object> getMetricsForTimeRange(Instant startTime, Instant endTime) {
        Map<String, Object> metrics = getCurrentMetrics();
        metrics.put("timeRange", Map.of(
            "startTime", startTime,
            "endTime", endTime,
            "durationHours", ChronoUnit.HOURS.between(startTime, endTime)
        ));
        return metrics;
    }

    /**
     * Get provider metrics (for PennyManagementController compatibility)
     */
    public Map<String, Object> getProviderMetrics(String providerType) {
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        metrics.put("providerType", providerType);
        metrics.put("usageCount", providerUsage.getOrDefault(providerType, new AtomicLong(0)).get());
        metrics.put("successRate", 95.0); // Placeholder
        metrics.put("averageResponseTime", 150); // Placeholder in milliseconds
        return metrics;
    }

    /**
     * Get bot metrics (for PennyManagementController compatibility)
     */
    public Map<String, Object> getBotMetrics(UUID botId) {
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        metrics.put("botId", botId);
        metrics.put("totalMessages", 1000); // Placeholder
        metrics.put("activeConversations", 50); // Placeholder
        metrics.put("averageResponseTime", 200.0); // Placeholder
        metrics.put("successRate", 92.5); // Placeholder
        return metrics;
    }

    /**
     * Get tenant metrics (for PennyManagementController compatibility)
     */
    public Map<String, Object> getTenantMetrics(Long tenantId) {
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        metrics.put("tenantId", tenantId);
        metrics.put("totalBots", 5); // Placeholder
        metrics.put("activeBots", 3); // Placeholder
        metrics.put("totalMessages", 10000); // Placeholder
        metrics.put("totalConversations", 500); // Placeholder
        return metrics;
    }

    /**
     * Export metrics (for PennyManagementController compatibility)
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

    /**
     * Reset metrics (for PennyManagementController compatibility)
     */
    public void resetMetrics() {
        log.info("Resetting Penny metrics");
        // Note: Micrometer counters cannot be reset, this is a placeholder
        // In production, you would need to recreate the MeterRegistry or use a different approach
    }

    // Private helper methods for export

    private String exportAsJson(Map<String, Object> metrics) {
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

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MetricsSummary {
        private double messagesProcessed;
        private double messageErrors;
        private double customRulesMatched;
        private double templatesMatched;
        private double providerFallbacks;
        private long activeConversations;
        private long activeBots;
        private double averageProcessingTime;
        private double averageIntentAnalysisTime;
        private double averageProviderSelectionTime;
    }
}
