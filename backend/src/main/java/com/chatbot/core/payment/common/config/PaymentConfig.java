package com.chatbot.core.payment.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main configuration for the payment module
 * Enables scheduling and JPA auditing across all payment sub-domains
 */
@Configuration
@EnableScheduling
@EnableJpaAuditing
public class PaymentConfig {
    // Additional configuration can be added here
}
