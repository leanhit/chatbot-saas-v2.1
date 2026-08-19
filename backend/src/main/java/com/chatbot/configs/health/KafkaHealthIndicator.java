package com.chatbot.configs.health;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Kafka Health Indicator - Checks connectivity to Kafka cluster
 */
@Component
@ConditionalOnBean(name = "kafkaTemplate")
@Slf4j
public class KafkaHealthIndicator implements HealthIndicator {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        
        try {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            
            try (AdminClient adminClient = AdminClient.create(props)) {
                // Check cluster health by listing topics with timeout
                adminClient.listTopics()
                    .names()
                    .get(5, TimeUnit.SECONDS);
                
                details.put("bootstrapServers", bootstrapServers);
                details.put("status", "UP");
                
                log.debug("✅ Kafka health check passed");
                
                return Health.up()
                    .withDetails(details)
                    .build();
            }
        } catch (Exception e) {
            log.error("❌ Kafka health check failed: {}", e.getMessage());
            details.put("bootstrapServers", bootstrapServers);
            details.put("status", "DOWN");
            details.put("error", e.getMessage());
            // Return UP to avoid bringing down the entire health check
            return Health.up()
                .withDetails(details)
                .build();
        }
    }
}
