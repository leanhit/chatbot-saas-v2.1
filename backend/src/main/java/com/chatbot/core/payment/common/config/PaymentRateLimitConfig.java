package com.chatbot.core.payment.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for rate limiting on payment endpoints
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "payment.ratelimit")
public class PaymentRateLimitConfig {

    private int publicRequestsPerMinute = 10;
    private int authenticatedRequestsPerMinute = 30;
    private int merchantRequestsPerMinute = 100;
    
    private boolean enabled = true;
    private long refillRateMs = 60000; // 1 minute
}
