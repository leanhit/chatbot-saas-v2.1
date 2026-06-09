package com.chatbot.core.simplepayment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "simplepayment.rate-limit")
@Data
public class SimplePaymentRateLimitConfig {
    
    private boolean enabled = true;
    
    private EndpointConfig publicEndpoints = new EndpointConfig();
    private EndpointConfig authenticatedEndpoints = new EndpointConfig();
    
    @Data
    public static class EndpointConfig {
        private int requestsPerMinute = 60;
        private int burstCapacity = 100;
    }
}
