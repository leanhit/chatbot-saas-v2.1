package com.chatbot.core.billing.account.model;

import com.chatbot.core.billing.model.BillingType;
import com.chatbot.shared.infrastructure.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Billing Account Entity
 * Manages billing information for tenants
 */
@Entity
@Table(name = "billing_accounts", 
       indexes = {
           @Index(name = "idx_billing_account_tenant", columnList = "tenant_id"),
           @Index(name = "idx_billing_account_type", columnList = "billing_type")
       })
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingAccount extends BaseTenantEntity {

    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_type", nullable = false)
    @Builder.Default
    private BillingType billingType = BillingType.SUBSCRIPTION;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "tax_code")
    private String taxCode;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "billing_address")
    private String billingAddress;

    @Column(name = "currency", nullable = false)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "credit_limit", precision = 15, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "current_balance", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "auto_payment")
    @Builder.Default
    private Boolean autoPayment = false;

    @Column(name = "payment_method_token")
    private String paymentMethodToken;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;

    @Column(name = "suspension_reason")
    private String suspensionReason;

    // Audit fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        // Generate account number if not set
        if (accountNumber == null || accountNumber.isBlank()) {
            accountNumber = "BA-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    // Helper methods
    public boolean isSuspended() {
        return suspendedAt != null;
    }

    public boolean hasCreditLimit() {
        return creditLimit != null && creditLimit.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isOverCreditLimit() {
        if (!hasCreditLimit()) return false;
        return currentBalance.compareTo(creditLimit) > 0;
    }

    public BigDecimal getAvailableCredit() {
        if (!hasCreditLimit()) return BigDecimal.ZERO;
        return creditLimit.subtract(currentBalance);
    }
}
