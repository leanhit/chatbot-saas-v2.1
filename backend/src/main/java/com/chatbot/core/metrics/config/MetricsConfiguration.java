package com.chatbot.core.metrics.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Configuration for custom business metrics.
 * Provides metrics for message processing, response times, and error rates.
 */
@Configuration
public class MetricsConfiguration {

    @Bean
    public CustomBusinessMetrics customBusinessMetrics() {
        return new CustomBusinessMetrics();
    }

    @Bean
    public MeterBinder customBusinessMetricsBinder(CustomBusinessMetrics customBusinessMetrics) {
        return customBusinessMetrics;
    }

    public static class CustomBusinessMetrics implements MeterBinder {
        private final AtomicLong facebookMessagesReceived = new AtomicLong(0);
        private final AtomicLong facebookMessagesProcessed = new AtomicLong(0);
        private final AtomicLong botResponsesSent = new AtomicLong(0);
        private final AtomicLong agentMessagesSent = new AtomicLong(0);
        private final AtomicLong messageProcessingErrors = new AtomicLong(0);
        private final AtomicLong activeConversations = new AtomicLong(0);
        private final AtomicLong takeoverCount = new AtomicLong(0);

        @Override
        public void bindTo(MeterRegistry registry) {
            // Message Counters
            registry.gauge("chatbot.facebook.messages.received", facebookMessagesReceived);
            registry.gauge("chatbot.facebook.messages.processed", facebookMessagesProcessed);
            registry.gauge("chatbot.bot.responses.sent", botResponsesSent);
            registry.gauge("chatbot.agent.messages.sent", agentMessagesSent);
            registry.gauge("chatbot.message.errors", messageProcessingErrors);
            
            // Conversation Metrics
            registry.gauge("chatbot.conversations.active", activeConversations);
            registry.gauge("chatbot.conversations.takeover.count", takeoverCount);
        }

        // Increment methods for business logic
        public void incrementFacebookMessagesReceived() {
            facebookMessagesReceived.incrementAndGet();
        }

        public void incrementFacebookMessagesProcessed() {
            facebookMessagesProcessed.incrementAndGet();
        }

        public void incrementBotResponsesSent() {
            botResponsesSent.incrementAndGet();
        }

        public void incrementAgentMessagesSent() {
            agentMessagesSent.incrementAndGet();
        }

        public void incrementMessageProcessingErrors() {
            messageProcessingErrors.incrementAndGet();
        }

        public void setActiveConversations(long count) {
            activeConversations.set(count);
        }

        public void incrementTakeoverCount() {
            takeoverCount.incrementAndGet();
        }
    }
}
