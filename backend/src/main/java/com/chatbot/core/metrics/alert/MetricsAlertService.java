package com.chatbot.core.metrics.alert;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for monitoring critical metrics and triggering alerts.
 * Checks metrics against configured thresholds and logs warnings.
 */
@Service
public class MetricsAlertService {

    private static final Logger log = LoggerFactory.getLogger(MetricsAlertService.class);

    private final MeterRegistry meterRegistry;

    @Value("${metrics.alerts.message.error.rate.threshold:5.0}")
    private double messageErrorRateThreshold;

    @Value("${metrics.alerts.response.time.threshold:5000}")
    private long responseTimeThreshold;

    @Value("${metrics.alerts.conversations.active.threshold:1000}")
    private long activeConversationsThreshold;

    @Value("${metrics.alerts.database.pool.threshold:80}")
    private double databasePoolThreshold;

    @Autowired
    public MetricsAlertService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Scheduled task to check critical metrics every minute.
     * Alerts if metrics exceed configured thresholds.
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    public void checkCriticalMetrics() {
        checkMessageErrorRate();
        checkResponseTimes();
        checkActiveConversations();
        checkDatabasePoolUsage();
    }

    private void checkMessageErrorRate() {
        try {
            double errorCount = meterRegistry.counter("chatbot.message.errors").count();
            double processedCount = meterRegistry.counter("chatbot.facebook.messages.processed").count();
            
            if (processedCount > 0) {
                double errorRate = (errorCount / processedCount) * 100;
                
                if (errorRate > messageErrorRateThreshold) {
                    log.warn("🚨 ALERT: Message error rate exceeds threshold: 当前={:.2f}%, 阈值={:.2f}%", 
                        errorRate, messageErrorRateThreshold);
                }
            }
        } catch (Exception e) {
            log.error("Error checking message error rate: {}", e.getMessage());
        }
    }

    private void checkResponseTimes() {
        try {
            meterRegistry.get("chatbot.operation.duration").timers().forEach(timer -> {
                double mean = timer.mean(TimeUnit.MILLISECONDS);
                String operation = timer.getId().getTag("operation");
                
                if (mean > responseTimeThreshold) {
                    log.warn("🚨 ALERT: Response time exceeds threshold for operation '{}': 当前={:.2f}ms, 阈值={}ms", 
                        operation, mean, responseTimeThreshold);
                }
            });
        } catch (Exception e) {
            log.error("Error checking response times: {}", e.getMessage());
        }
    }

    private void checkActiveConversations() {
        try {
            Double activeConversations = meterRegistry.get("chatbot.conversations.active").gauge().value();
            
            if (activeConversations != null && activeConversations > activeConversationsThreshold) {
                log.warn("🚨 ALERT: Active conversations exceed threshold: 当前={}, 阈值={}", 
                    activeConversations, activeConversationsThreshold);
            }
        } catch (Exception e) {
            log.error("Error checking active conversations: {}", e.getMessage());
        }
    }

    private void checkDatabasePoolUsage() {
        try {
            meterRegistry.get("hikaricp.connections.active").gauges().forEach(gauge -> {
                double activeConnections = gauge.value();
                
                meterRegistry.get("hikaricp.connections.max").gauges().forEach(maxGauge -> {
                    double maxConnections = maxGauge.value();
                    
                    if (maxConnections > 0) {
                        double poolUsage = (activeConnections / maxConnections) * 100;
                        
                        if (poolUsage > databasePoolThreshold) {
                            log.warn("🚨 ALERT: Database pool usage exceeds threshold: 当前={:.2f}%, 阈值={:.2f}%", 
                                poolUsage, databasePoolThreshold);
                        }
                    }
                });
            });
        } catch (Exception e) {
            log.error("Error checking database pool usage: {}", e.getMessage());
        }
    }
}
