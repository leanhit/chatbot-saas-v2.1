package com.chatbot.modules.wallet.core.dto;

import com.chatbot.modules.wallet.core.model.WalletTransaction;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Wallet transaction response DTO for v0.1
 * Transaction information for API responses
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransactionResponse {

    private UUID id;
    private UUID walletId;
    private WalletTransaction.TransactionType type;
    private BigDecimal amount;
    private String reference;
    private LocalDateTime createdAt;
}
