package com.chatbot.modules.wallet.core.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * WalletTransaction entity - Immutable transaction record
 * 
 * v0.1: Simple transaction model
 * - Immutable ledger entries
 * - Positive amounts only
 * - Reference for audit trail
 */
@Entity
@Table(
    name = "wallet_transactions",
    indexes = {
        @Index(name = "idx_wallet_transactions_wallet_id", columnList = "wallet_id"),
        @Index(name = "idx_wallet_transactions_created_at", columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransaction {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TransactionType type;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "reference", length = 255)
    private String reference;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Transaction types for wallet operations
     */
    public enum TransactionType {
        TOPUP("Add money to wallet"),
        PURCHASE("Deduct money from wallet");

        private final String description;

        TransactionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
