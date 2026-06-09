package com.chatbot.core.simplepayment.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String invoiceNumber; // INV-2024-000001
    
    @Column(name = "payment_id", nullable = false)
    private Long paymentId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @Column(nullable = false, length = 100)
    private String userEmail;
    
    @Column(nullable = false, length = 100)
    private String userName;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal discountAmount;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal taxAmount;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(nullable = false, length = 10)
    private String currency;
    
    @Column(length = 50)
    private String discountCode;
    
    @Column(length = 50)
    private String packageId;
    
    @Column(length = 100)
    private String packageName;
    
    @Column(nullable = false)
    private InvoiceStatus status;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(length = 20)
    private String paymentMethod; // BANK_TRANSFER, QR_CODE, etc.
    
    @Column(columnDefinition = "TEXT")
    private String paymentReference;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = InvoiceStatus.PAID;
        }
        if (taxAmount == null) {
            taxAmount = BigDecimal.ZERO;
        }
        if (discountAmount == null) {
            discountAmount = BigDecimal.ZERO;
        }
    }
    
    public enum InvoiceStatus {
        DRAFT,
        PENDING,
        PAID,
        OVERDUE,
        CANCELLED,
        REFUNDED
    }
    
    // Helper methods
    public String getFormattedInvoiceNumber() {
        return invoiceNumber != null ? invoiceNumber : "INV-UNKNOWN";
    }
    
    public String getFormattedTotal() {
        return String.format("%,.0f %s", totalAmount, currency);
    }
    
    public boolean isPaid() {
        return status == InvoiceStatus.PAID;
    }
}
