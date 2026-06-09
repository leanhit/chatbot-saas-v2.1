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
}
