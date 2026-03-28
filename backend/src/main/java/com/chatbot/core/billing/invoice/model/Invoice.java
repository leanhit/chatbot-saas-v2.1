package com.chatbot.core.billing.invoice.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Invoice Entity
 * Manages billing invoices for tenants
 */
@Entity
@Table(name = "invoices", 
       indexes = {
           @Index(name = "idx_invoice_tenant", columnList = "tenant_id"),
           @Index(name = "idx_invoice_number", columnList = "invoice_number"),
           @Index(name = "idx_invoice_status", columnList = "status"),
           @Index(name = "idx_invoice_billing_account", columnList = "billing_account_id"),
           @Index(name = "idx_invoice_subscription", columnList = "subscription_id"),
           @Index(name = "idx_invoice_due_date", columnList = "due_date")
       })
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Invoice extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", unique = true, nullable = false)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_account_id", nullable = false)
    private com.chatbot.core.billing.account.model.BillingAccount billingAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private com.chatbot.core.billing.subscription.model.BillingSubscription subscription;

    @Column(name = "invoice_date", nullable = false)
    private LocalDateTime invoiceDate;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(name = "subtotal", precision = 15, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "payment_method_id")
    private Long paymentMethodId;

    @Column(name = "payment_transaction_id")
    private String paymentTransactionId;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InvoiceLineItem> lineItems;

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
        // Generate invoice number if not set
        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            invoiceNumber = "INV-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
        }
        // Set invoice date if not set
        if (invoiceDate == null) {
            invoiceDate = LocalDateTime.now();
        }
        // Set due date if not set (30 days from invoice date)
        if (dueDate == null) {
            dueDate = invoiceDate.plusDays(30);
        }
    }

    // Helper methods
    public boolean isPaid() {
        return status == InvoiceStatus.PAID;
    }

    public boolean isOverdue() {
        if (isPaid()) return false;
        return LocalDateTime.now().isAfter(dueDate);
    }

    public boolean isPartiallyPaid() {
        return paidAmount != null && paidAmount.compareTo(BigDecimal.ZERO) > 0 && 
               paidAmount.compareTo(totalAmount) < 0;
    }

    public BigDecimal getOutstandingAmount() {
        if (paidAmount == null) return totalAmount;
        return totalAmount.subtract(paidAmount);
    }

    public long getDaysOverdue() {
        if (!isOverdue()) return 0;
        return java.time.Duration.between(dueDate, LocalDateTime.now()).toDays();
    }

    public void markAsPaid(String transactionId) {
        this.status = InvoiceStatus.PAID;
        this.paidAt = LocalDateTime.now();
        this.paymentTransactionId = transactionId;
        this.paidAmount = this.totalAmount;
    }

    public void addPayment(BigDecimal amount, String transactionId) {
        if (paidAmount == null) {
            paidAmount = BigDecimal.ZERO;
        }
        paidAmount = paidAmount.add(amount);
        this.paymentTransactionId = transactionId;
        
        if (paidAmount.compareTo(totalAmount) >= 0) {
            markAsPaid(transactionId);
        } else {
            this.status = InvoiceStatus.PARTIALLY_PAID;
        }
    }
}
