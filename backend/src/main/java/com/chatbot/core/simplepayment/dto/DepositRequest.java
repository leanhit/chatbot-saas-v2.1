package com.chatbot.core.simplepayment.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class DepositRequest {
    
    @NotNull(message = "Wallet ID is required")
    private Long walletId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    private String currency = "VND";
    
    private String description = "Nạp tiền vào ví";
}
