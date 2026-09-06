package com.chatbot.core.payment.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for bank details and mock bank API settings
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "payment.bank")
public class PaymentBankConfig {

    private String bankName = "Vietcombank";
    private String accountNumber = "1234567890";
    private String accountName = "CHATBOT SaaS";
    private String branch = "Ho Chi Minh City";
    
    // Mock bank API settings
    private String mockApiUrl = "http://localhost:8081/mock-bank";
    private boolean useMockApi = true;
    private int retryAttempts = 3;
    private long retryDelayMs = 1000;
}
