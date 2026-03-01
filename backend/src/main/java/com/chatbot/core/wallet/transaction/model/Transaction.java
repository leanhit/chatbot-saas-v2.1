package com.chatbot.core.wallet.transaction.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
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
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction extends BaseTenantEntity {

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

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public Long getTenantId() {
        return super.getTenantId();
    }

    @Override
    public void setTenantId(Long tenantId) {
        super.setTenantId(tenantId);
    }

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (status == null) {
            status = TransactionStatus.PENDING;
        }
        if (transactionReference == null) {
            transactionReference = generateTransactionReference();
        }
    }

    private String generateTransactionReference() {
        return "TXN" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
}
