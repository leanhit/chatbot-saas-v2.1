package com.chatbot.core.simplepayment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "simplepayment.bank")
@Data
public class SimplePaymentBankConfig {
    private String name = "Vietcombank";
    private String accountNumber = "1234567890";
    private String accountName = "CHATBOT SaaS";
    
    // Bank API Configuration
    private String provider = "mock";
    private String apiUrl = "http://localhost:3000/mock-bank";
    private String apiKey = "dev-mock-key-12345";
    private Integer timeout = 30000;
    private Integer retryAttempts = 3;
    private Integer retryDelay = 1000;
}
