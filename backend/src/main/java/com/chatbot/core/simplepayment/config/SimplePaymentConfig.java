package com.chatbot.core.simplepayment.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableJpaAuditing
public class SimplePaymentConfig {
    // Configuration for simple payment system
    // Scheduling enabled for automatic payment checking
    // JPA Auditing enabled for automatic timestamps
    // Note: DataSource and JPA configuration handled by HubDatabaseConfig
}
