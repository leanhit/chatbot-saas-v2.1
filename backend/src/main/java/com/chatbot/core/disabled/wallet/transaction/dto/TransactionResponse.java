package com.chatbot.core.wallet.transaction.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    
    private Long id;
    private Long walletId;
    private String transactionReference;
    private String transactionType;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String status;
    private String externalReference;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
