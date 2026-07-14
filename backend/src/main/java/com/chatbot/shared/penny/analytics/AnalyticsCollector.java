package com.chatbot.shared.penny.analytics;

import com.chatbot.shared.penny.core.config.PennyProperties;
import com.chatbot.shared.penny.dto.request.MiddlewareRequest;
import com.chatbot.shared.penny.dto.response.MiddlewareResponse;
import com.chatbot.shared.penny.routing.dto.IntentAnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Analytics Collector - Thu thập và phân tích dữ liệu sử dụng
 */
@Service
@Slf4j
public class AnalyticsCollector {
    
    private final PennyProperties properties;
    private final Queue<com.chatbot.shared.penny.analytics.AnalyticsEvent> eventQueue;
    private final AtomicLong totalProcessed;
    private final AtomicLong totalErrors;
    private final Map<String, AtomicLong> providerUsage;
    private final Map<String, AtomicLong> intentCounts;
    private final AtomicLong totalProcessingTime;
    private final AnalyticsEventRepository analyticsEventRepository;
    
    public AnalyticsCollector(PennyProperties properties, AnalyticsEventRepository analyticsEventRepository) {
        this.properties = properties;
        this.analyticsEventRepository = analyticsEventRepository;
        this.eventQueue = new ConcurrentLinkedQueue<>();
        this.totalProcessed = new AtomicLong(0);
        this.totalErrors = new AtomicLong(0);
        this.providerUsage = new HashMap<>();
        this.intentCounts = new HashMap<>();
        this.totalProcessingTime = new AtomicLong(0);
    }
    
    /**
     * Collect metrics from message processing
     */
    public void collectMetrics(MiddlewareRequest request, 
                             MiddlewareResponse response, 
                             IntentAnalysisResult analysis, 
                             long startTime) {
        
        if (!properties.getAnalytics().isEnabled()) {
            return;
        }
        
        try {
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Update counters
            totalProcessed.incrementAndGet();
            totalProcessingTime.addAndGet(processingTime);
            
            // Track provider usage
            if (response.getProviderUsed() != null) {
                providerUsage.computeIfAbsent(response.getProviderUsed(), k -> new AtomicLong(0))
                    .incrementAndGet();
            }
            
            // Track intent counts
            if (analysis != null && analysis.getPrimaryIntent() != null) {
                intentCounts.computeIfAbsent(analysis.getPrimaryIntent(), k -> new AtomicLong(0))
                    .incrementAndGet();
            }
            
            // Track errors
            if (response.hasError()) {
                totalErrors.incrementAndGet();
            }
            
            // Create analytics event
            com.chatbot.shared.penny.analytics.AnalyticsEvent event = 
                com.chatbot.shared.penny.analytics.AnalyticsEvent.builder()
                    .botId(UUID.fromString(request.getBotId()))
                    .eventType("message_processed")
                    .timestamp(Instant.now())
                    .data(buildEventData(request, response, analysis, processingTime))
                    .userId(request.getUserId())
                    .platform(request.getPlatform())
                    .intent(analysis != null ? analysis.getPrimaryIntent() : null)
                    .providerUsed(response.getProviderUsed())
                    .processingTimeMs(processingTime)
                    .hasError(response.hasError())
                    .build();
            
            // Add to queue for batch processing
            eventQueue.offer(event);
            
            // Process batch if needed
            if (eventQueue.size() >= properties.getAnalytics().getBatchSize()) {
                flushEvents();
            }
            
            log.debug("📊 Analytics collected - Provider: {}, Intent: {}, Time: {}ms", 
                response.getProviderUsed(), analysis.getPrimaryIntent(), processingTime);
            
        } catch (Exception e) {
            log.error("❌ Error collecting analytics: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Get engine metrics
     */
    public EngineMetrics getEngineMetrics() {
        long total = totalProcessed.get();
        long errors = totalErrors.get();
        long totalTime = totalProcessingTime.get();
        
        EngineMetrics metrics = new EngineMetrics();
        metrics.setTotalProcessed(total);
        metrics.setTotalErrors(errors);
        metrics.setErrorRate(total > 0 ? (double) errors / total : 0.0);
        metrics.setAverageProcessingTime(total > 0 ? (double) totalTime / total : 0.0);
        metrics.setProviderUsage(new HashMap<>(providerUsage.entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().get()
            ))));
        metrics.setIntentCounts(new HashMap<>(intentCounts.entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().get()
            ))));
        metrics.setTimestamp(Instant.now());
        
        return metrics;
    }
    
    /**
     * Flush events to storage
     */
    @Async
    @Transactional
    public void flushEvents() {
        if (!properties.getAnalytics().isEnabled() || eventQueue.isEmpty()) {
            return;
        }
        
        try {
            List<com.chatbot.shared.penny.analytics.AnalyticsEvent> events = new ArrayList<>();
            com.chatbot.shared.penny.analytics.AnalyticsEvent event;
            while ((event = eventQueue.poll()) != null && events.size() < properties.getAnalytics().getBatchSize()) {
                events.add(event);
            }
            
            if (!events.isEmpty()) {
                // Persist events to database
                log.debug("📊 Persisting {} analytics events to database", events.size());
                analyticsEventRepository.saveAll(events);
                log.info("✅ Successfully persisted {} analytics events", events.size());
            }
            
        } catch (Exception e) {
            log.error("❌ Error flushing analytics events: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Track custom event
     */
    public void trackEvent(String eventType, Map<String, Object> data) {
        if (!properties.getAnalytics().isEnabled()) {
            return;
        }
        
        try {
            com.chatbot.shared.penny.analytics.AnalyticsEvent event = 
                com.chatbot.shared.penny.analytics.AnalyticsEvent.builder()
                    .eventType(eventType)
                    .timestamp(Instant.now())
                    .data(data)
                    .build();
            
            eventQueue.offer(event);
            
        } catch (Exception e) {
            log.error("❌ Error tracking custom event {}: {}", eventType, e.getMessage(), e);
        }
    }
    
    /**
     * Track provider performance
     */
    public void trackProviderPerformance(String providerType, long processingTime, boolean success) {
        if (!properties.getAnalytics().getMetrics().isProviderUsageEnabled()) {
            return;
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("providerType", providerType);
        data.put("processingTime", processingTime);
        data.put("success", success);
        data.put("timestamp", Instant.now().toString());
        
        trackEvent("provider_performance", data);
    }
    
    /**
     * Track intent accuracy
     */
    public void trackIntentAccuracy(String predictedIntent, String actualIntent, double confidence) {
        if (!properties.getAnalytics().getMetrics().isIntentTrackingEnabled()) {
            return;
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("predictedIntent", predictedIntent);
        data.put("actualIntent", actualIntent);
        data.put("confidence", confidence);
        data.put("accurate", predictedIntent.equals(actualIntent));
        data.put("timestamp", Instant.now().toString());
        
        trackEvent("intent_accuracy", data);
    }
    
    /**
     * Track error
     */
    public void trackError(String errorType, String errorMessage, Map<String, Object> context) {
        if (!properties.getAnalytics().getMetrics().isErrorTrackingEnabled()) {
            return;
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("errorType", errorType);
        data.put("errorMessage", errorMessage);
        data.put("context", context);
        data.put("timestamp", Instant.now().toString());
        
        trackEvent("error", data);
    }
    
    /**
     * Get statistics summary
     */
    public AnalyticsSummary getSummary() {
        AnalyticsSummary summary = new AnalyticsSummary();
        summary.setTotalMessages(totalProcessed.get());
        summary.setTotalErrors(totalErrors.get());
        summary.setErrorRate(calculateErrorRate());
        summary.setAverageProcessingTime(calculateAverageProcessingTime());
        summary.setMostUsedProvider(getMostUsedProvider());
        summary.setMostCommonIntent(getMostCommonIntent());
        summary.setQueueSize(eventQueue.size());
        summary.setTimestamp(Instant.now());
        
        return summary;
    }
    
    // Private helper methods
    
    private Map<String, Object> buildEventData(MiddlewareRequest request, 
                                               MiddlewareResponse response, 
                                               IntentAnalysisResult analysis, 
                                               long processingTime) {
        Map<String, Object> data = new HashMap<>();
        
        // Request data
        data.put("requestId", request.getRequestId());
        data.put("userId", request.getUserId());
        data.put("platform", request.getPlatform());
        data.put("messageLength", request.getMessage() != null ? request.getMessage().length() : 0);
        data.put("language", request.getLanguage());
        
        // Response data
        data.put("providerUsed", response.getProviderUsed());
        data.put("responseLength", response.getResponse() != null ? response.getResponse().length() : 0);
        data.put("hasError", response.hasError());
        data.put("needsEscalation", response.needsEscalation());
        
        // Analysis data
        if (analysis != null) {
            data.put("intent", analysis.getPrimaryIntent());
            data.put("confidence", analysis.getConfidence());
            data.put("complexity", analysis.getComplexity());
            data.put("entitiesCount", analysis.getEntities() != null ? analysis.getEntities().size() : 0);
        }
        
        // Performance data
        data.put("processingTime", processingTime);
        data.put("timestamp", Instant.now().toString());
        
        return data;
    }
    
    private double calculateErrorRate() {
        long total = totalProcessed.get();
        return total > 0 ? (double) totalErrors.get() / total : 0.0;
    }
    
    private double calculateAverageProcessingTime() {
        long total = totalProcessed.get();
        long totalTime = totalProcessingTime.get();
        return total > 0 ? (double) totalTime / total : 0.0;
    }
    
    private String getMostUsedProvider() {
        return providerUsage.entrySet().stream()
            .max(Map.Entry.comparingByValue((a, b) -> Long.compare(a.get(), b.get())))
            .map(Map.Entry::getKey)
            .orElse("unknown");
    }
    
    private String getMostCommonIntent() {
        return intentCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue((a, b) -> Long.compare(a.get(), b.get())))
            .map(Map.Entry::getKey)
            .orElse("unknown");
    }
    
    // Inner classes
    
    public static class AnalyticsEvent {
        private String eventType;
        private Instant timestamp;
        private Map<String, Object> data;
        
        public AnalyticsEvent() {}
        
        public AnalyticsEvent(String eventType, Instant timestamp, Map<String, Object> data) {
            this.eventType = eventType;
            this.timestamp = timestamp;
            this.data = data;
        }
        
        // Getters and setters
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        
        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
        
        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
    }
    
    public static class EngineMetrics {
        private long totalProcessed;
        private long totalErrors;
        private double errorRate;
        private double averageProcessingTime;
        private Map<String, Long> providerUsage;
        private Map<String, Long> intentCounts;
        private Instant timestamp;
        
        public EngineMetrics() {}
        
        // Getters and setters
        public long getTotalProcessed() { return totalProcessed; }
        public void setTotalProcessed(long totalProcessed) { this.totalProcessed = totalProcessed; }
        
        public long getTotalErrors() { return totalErrors; }
        public void setTotalErrors(long totalErrors) { this.totalErrors = totalErrors; }
        
        public double getErrorRate() { return errorRate; }
        public void setErrorRate(double errorRate) { this.errorRate = errorRate; }
        
        public double getAverageProcessingTime() { return averageProcessingTime; }
        public void setAverageProcessingTime(double averageProcessingTime) { this.averageProcessingTime = averageProcessingTime; }
        
        public Map<String, Long> getProviderUsage() { return providerUsage; }
        public void setProviderUsage(Map<String, Long> providerUsage) { this.providerUsage = providerUsage; }
        
        public Map<String, Long> getIntentCounts() { return intentCounts; }
        public void setIntentCounts(Map<String, Long> intentCounts) { this.intentCounts = intentCounts; }
        
        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    }
    
    public static class AnalyticsSummary {
        private long totalMessages;
        private long totalErrors;
        private double errorRate;
        private double averageProcessingTime;
        private String mostUsedProvider;
        private String mostCommonIntent;
        private int queueSize;
        private Instant timestamp;
        
        public AnalyticsSummary() {}
        
        // Getters and setters
        public long getTotalMessages() { return totalMessages; }
        public void setTotalMessages(long totalMessages) { this.totalMessages = totalMessages; }
        
        public long getTotalErrors() { return totalErrors; }
        public void setTotalErrors(long totalErrors) { this.totalErrors = totalErrors; }
        
        public double getErrorRate() { return errorRate; }
        public void setErrorRate(double errorRate) { this.errorRate = errorRate; }
        
        public double getAverageProcessingTime() { return averageProcessingTime; }
        public void setAverageProcessingTime(double averageProcessingTime) { this.averageProcessingTime = averageProcessingTime; }
        
        public String getMostUsedProvider() { return mostUsedProvider; }
        public void setMostUsedProvider(String mostUsedProvider) { this.mostUsedProvider = mostUsedProvider; }
        
        public String getMostCommonIntent() { return mostCommonIntent; }
        public void setMostCommonIntent(String mostCommonIntent) { this.mostCommonIntent = mostCommonIntent; }
        
        public int getQueueSize() { return queueSize; }
        public void setQueueSize(int queueSize) { this.queueSize = queueSize; }
        
        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    }
    
    /**
     * Configure analytics for a new bot
     */
    public void configureBotAnalytics(String botId) {
        log.info("📊 Configuring analytics for Penny bot: {}", botId);
        
        try {
            // Initialize analytics tracking for this bot
            providerUsage.putIfAbsent(botId, new AtomicLong(0));
            intentCounts.putIfAbsent(botId, new AtomicLong(0));
            
            log.info("✅ Analytics configured for bot: {}", botId);
        } catch (Exception e) {
            log.error("❌ Failed to configure analytics for bot {}: {}", botId, e.getMessage(), e);
        }
    }
    
    /**
     * Cleanup analytics for a deleted bot
     */
    public void cleanupBotAnalytics(String botId) {
        log.info("🧹 Cleaning up analytics for Penny bot: {}", botId);
        
        try {
            // Remove analytics tracking for this bot
            providerUsage.remove(botId);
            intentCounts.remove(botId);
            
            log.info("✅ Analytics cleaned up for bot: {}", botId);
        } catch (Exception e) {
            log.error("❌ Failed to cleanup analytics for bot {}: {}", botId, e.getMessage(), e);
        }
    }
    
    /**
     * Check if bot analytics is healthy
     */
    public boolean isBotAnalyticsHealthy(String botId) {
        try {
            // Check if analytics tracking exists for this bot
            return providerUsage.containsKey(botId) && intentCounts.containsKey(botId);
        } catch (Exception e) {
            log.error("❌ Error checking analytics health for bot {}: {}", botId, e.getMessage());
            return false;
        }
    }
}
