package com.chatbot.core.billing.invoice.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * Invoice Line Item Entity
 * Represents individual items in an invoice
 */
@Entity
@Table(name = "invoice_line_items", 
       indexes = {
           @Index(name = "idx_line_item_invoice", columnList = "invoice_id"),
           @Index(name = "idx_line_item_subscription", columnList = "subscription_id")
       })
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class InvoiceLineItem extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "item_number", nullable = false)
    private Integer itemNumber;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "unit_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(name = "tax_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxPercentage = BigDecimal.ZERO;

    @Column(name = "line_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal lineTotal;

    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Column(name = "period_start")
    private java.time.LocalDateTime periodStart;

    @Column(name = "period_end")
    private java.time.LocalDateTime periodEnd;

    @Column(name = "metadata", columnDefinition = "JSON")
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
    @PreUpdate
    protected void calculateTotals() {
        super.onCreate();
        // Calculate line total
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        
        // Apply discount
        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discountAmount = subtotal.multiply(discountPercentage.divide(BigDecimal.valueOf(100)));
            subtotal = subtotal.subtract(discountAmount);
        }
        
        // Apply tax
        if (taxPercentage != null && taxPercentage.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal taxAmount = subtotal.multiply(taxPercentage.divide(BigDecimal.valueOf(100)));
            subtotal = subtotal.add(taxAmount);
        }
        
        this.lineTotal = subtotal;
    }

    // Helper methods
    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal getDiscountAmount() {
        if (discountPercentage == null || discountPercentage.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getSubtotal().multiply(discountPercentage.divide(BigDecimal.valueOf(100)));
    }

    public BigDecimal getTaxAmount() {
        BigDecimal discountedSubtotal = getSubtotal().subtract(getDiscountAmount());
        if (taxPercentage == null || taxPercentage.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return discountedSubtotal.multiply(taxPercentage.divide(BigDecimal.valueOf(100)));
    }
}
