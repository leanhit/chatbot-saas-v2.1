package com.chatbot.core.metrics.test;

import com.chatbot.core.metrics.service.BusinessMetricsService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Test controller to verify metrics and monitoring configuration.
 * Access: GET /api/metrics/test
 */
@RestController
@RequestMapping("/api/metrics")
public class MetricsTestController {

    private final BusinessMetricsService metricsService;
    private final MeterRegistry meterRegistry;

    @Autowired
    public MetricsTestController(BusinessMetricsService metricsService, MeterRegistry meterRegistry) {
        this.metricsService = metricsService;
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/test")
    public Map<String, Object> testMetrics() {
        Map<String, Object> result = new HashMap<>();
        
        // Test business metrics
        metricsService.recordFacebookMessageReceived();
        metricsService.recordFacebookMessageProcessed();
        metricsService.recordBotResponseSent();
        metricsService.recordAgentMessageSent();
        
        // Test response time metrics
        metricsService.recordResponseTime("facebook_webhook", 150);
        metricsService.recordResponseTime("bot_processing", 300);
        
        // Test error metrics
        metricsService.recordError("api_timeout", "facebook_api");
        
        // Test throughput metrics
        metricsService.recordThroughput("message_processing", 10);
        
        result.put("status", "Metrics test completed");
        result.put("message", "Check /actuator/metrics and /actuator/prometheus for metrics output");
        result.put("endpoints", Map.of(
            "metrics", "/actuator/metrics",
            "prometheus", "/actuator/prometheus",
            "health", "/actuator/health"
        ));
        
        return result;
    }

    @GetMapping("/summary")
    public Map<String, Object> getMetricsSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Get custom business metrics
        try {
            Double facebookReceived = meterRegistry.get("chatbot.facebook.messages.received").gauge().value();
            Double facebookProcessed = meterRegistry.get("chatbot.facebook.messages.processed").gauge().value();
            Double botResponses = meterRegistry.get("chatbot.bot.responses.sent").gauge().value();
            Double agentMessages = meterRegistry.get("chatbot.agent.messages.sent").gauge().value();
            
            summary.put("business_metrics", Map.of(
                "facebook_messages_received", facebookReceived,
                "facebook_messages_processed", facebookProcessed,
                "bot_responses_sent", botResponses,
                "agent_messages_sent", agentMessages
            ));
        } catch (Exception e) {
            summary.put("business_metrics", "Metrics not yet initialized");
        }
        
        // Get JVM metrics
        try {
            Double jvmMemory = meterRegistry.get("jvm.memory.used").gauge().value();
            Double jvmCpu = meterRegistry.get("process.cpu.usage").gauge().value();
            
            summary.put("jvm_metrics", Map.of(
                "memory_used_mb", jvmMemory / (1024 * 1024),
                "cpu_usage_percent", jvmCpu * 100
            ));
        } catch (Exception e) {
            summary.put("jvm_metrics", "JVM metrics not available");
        }
        
        summary.put("status", "Metrics summary retrieved");
        return summary;
    }
}
