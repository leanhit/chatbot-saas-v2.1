package com.chatbot.core.payment.common.metrics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentHealthIndicator implements HealthIndicator {

    // Dependencies will be injected after migration
    // For now, this is a placeholder that will be updated after transaction and gateway migration

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        
        try {
            // Placeholder health check
            details.put("status", "MIGRATION_IN_PROGRESS");
            details.put("message", "Payment module is being refactored");
            
            return Health.up()
                .withDetails(details)
                .build();
            
        } catch (Exception e) {
            log.error("Payment health check failed", e);
            details.put("error", e.getMessage());
            return Health.up()
                .withDetails(details)
                .build();
        }
    }
}
