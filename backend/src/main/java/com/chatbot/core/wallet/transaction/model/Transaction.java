package com.chatbot.core.wallet.transaction.model;

import com.chatbot.shared.infrastructure.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "transaction_reference", unique = true, nullable = false, length = 100)
    private String transactionReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @Column(name = "external_reference", length = 100)
    private String externalReference;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = TransactionStatus.PENDING;
        }
        if (transactionReference == null) {
            transactionReference = generateTransactionReference();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private String generateTransactionReference() {
        return "TXN" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
}
