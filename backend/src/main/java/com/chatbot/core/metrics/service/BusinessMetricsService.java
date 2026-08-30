package com.chatbot.core.metrics.service;

import com.chatbot.core.metrics.config.MetricsConfiguration.CustomBusinessMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for tracking business metrics with timers and counters.
 * Provides methods to record message processing times and business events.
 */
@Service
public class BusinessMetricsService {

    private final CustomBusinessMetrics customMetrics;
    private final MeterRegistry meterRegistry;

    @Autowired
    public BusinessMetricsService(CustomBusinessMetrics customMetrics, MeterRegistry meterRegistry) {
        this.customMetrics = customMetrics;
        this.meterRegistry = meterRegistry;
    }

    // Message Processing Metrics
    public void recordFacebookMessageReceived() {
        customMetrics.incrementFacebookMessagesReceived();
    }

    public void recordFacebookMessageProcessed() {
        customMetrics.incrementFacebookMessagesProcessed();
    }

    public void recordBotResponseSent() {
        customMetrics.incrementBotResponsesSent();
    }

    public void recordAgentMessageSent() {
        customMetrics.incrementAgentMessagesSent();
    }

    public void recordMessageProcessingError() {
        customMetrics.incrementMessageProcessingErrors();
    }

    // Conversation Metrics
    public void recordActiveConversations(long count) {
        customMetrics.setActiveConversations(count);
    }

    public void recordTakeover() {
        customMetrics.incrementTakeoverCount();
    }

    // Timer Metrics for Response Times
    public Timer.Sample startTimer(String operation) {
        return Timer.start(meterRegistry);
    }

    public void stopTimer(Timer.Sample sample, String operation, String... tags) {
        sample.stop(Timer.builder("chatbot.operation.duration")
                .description("Duration of chatbot operations")
                .tag("operation", operation)
                .tags(tags)
                .register(meterRegistry));
    }

    // Convenience method for timed operations
    public <T> T recordTimedOperation(String operation, java.util.function.Supplier<T> supplier, String... tags) {
        Timer.Sample sample = startTimer(operation);
        try {
            T result = supplier.get();
            stopTimer(sample, operation, tags);
            return result;
        } catch (Exception e) {
            stopTimer(sample, operation, tags);
            recordMessageProcessingError();
            throw e;
        }
    }

    // Response Time Metrics
    public void recordResponseTime(String operation, long durationMs) {
        meterRegistry.timer("chatbot.response.time", 
            "operation", operation)
            .record(durationMs, TimeUnit.MILLISECONDS);
    }

    // Error Rate Metrics
    public void recordError(String errorType, String component) {
        meterRegistry.counter("chatbot.errors", 
            "type", errorType, 
            "component", component)
            .increment();
    }

    // Throughput Metrics
    public void recordThroughput(String operation, int count) {
        meterRegistry.counter("chatbot.throughput", 
            "operation", operation)
            .increment(count);
    }
}
