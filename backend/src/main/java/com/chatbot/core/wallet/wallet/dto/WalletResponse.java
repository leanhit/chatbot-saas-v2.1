package com.chatbot.core.wallet.wallet.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WalletResponse {
    
    private Long id;
    private Long userId;
    private Long tenantId;
    private BigDecimal balance;
    private String currency;
    private String status;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
