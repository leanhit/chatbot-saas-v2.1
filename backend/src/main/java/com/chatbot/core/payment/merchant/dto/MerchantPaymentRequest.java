package com.chatbot.core.payment.merchant.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class MerchantPaymentRequest {
    
    @NotBlank(message = "Merchant order ID is required")
    private String merchantOrderId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    private String currency = "VND";
    
    private String description;
    
    private String returnUrl;
    
    private String cancelUrl;
    
    private String metadata; // JSON string
}
