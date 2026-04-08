package com.chatbot.core.simplepayment.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class DepositRequest {
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    private String currency = "VND";
    
    private String description = "Nạp tiền vào tài khoản";
    
    private String targetPackageId; // Package to upgrade to after payment
}
