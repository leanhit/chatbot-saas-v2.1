package com.chatbot.core.payment.merchant.model;

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
@Table(name = "merchant_payment_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantPaymentSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String sessionId; // Unique session ID for merchant payment
    
    @Column(nullable = false)
    private Long merchantId; // ID from merchant_api_keys table
    
    @Column(nullable = false)
    private Long tenantId;
    
    @Column(nullable = false, length = 100)
    private String merchantOrderId; // Merchant's internal order ID
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "VND";
    
    @Column(length = 500)
    private String description;
    
    @Column(length = 500)
    private String returnUrl; // URL to redirect after payment completion
    
    @Column(length = 500)
    private String cancelUrl; // URL to redirect if payment is cancelled
    
    @Column(length = 500)
    private String metadata; // JSON metadata from merchant
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SessionStatus status;
    
    @Column(length = 100)
    private String paymentReferenceCode; // Reference code from our payment system
    
    @Column(length = 100)
    private String bankTransactionId;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "failed_at")
    private LocalDateTime failedAt;
    
    @Column(length = 500)
    private String failureReason;
    
    @Column(name = "webhook_sent_at")
    private LocalDateTime webhookSentAt;
    
    @Column(name = "webhook_status")
    private String webhookStatus; // PENDING, SENT, FAILED
    
    @Column(name = "webhook_retry_count")
    @Builder.Default
    private Integer webhookRetryCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (sessionId == null) {
            sessionId = generateSessionId();
        }
        if (status == null) {
            status = SessionStatus.PENDING;
        }
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusHours(24); // Default 24 hours
        }
    }
    
    /**
     * Generate a unique session ID
     * Format: sess_xxxxxxxxxxxxxxxx
     */
    private String generateSessionId() {
        String prefix = "sess_";
        String random = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        return prefix + random;
    }
    
    /**
     * Check if session is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Check if session can be completed
     */
    public boolean canComplete() {
        return status == SessionStatus.PENDING && !isExpired();
    }
    
    /**
     * Check if session can be cancelled
     */
    public boolean canCancel() {
        return status == SessionStatus.PENDING && !isExpired();
    }
    
    public enum SessionStatus {
        PENDING,
        COMPLETED,
        CANCELLED,
        FAILED,
        EXPIRED
    }
}
