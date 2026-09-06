package com.chatbot.core.payment.gateway.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhooks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Webhook {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, unique = true, length = 500)
    private String url;
    
    @Column(nullable = false, length = 50)
    private String secret; // Webhook secret for signature verification
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @ElementCollection
    @CollectionTable(name = "webhook_events", joinColumns = @JoinColumn(name = "webhook_id"))
    @Column(name = "event_type", length = 50)
    private java.util.Set<WebhookEventType> eventTypes;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer retryCount = 3;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer currentRetryAttempt = 0;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer timeoutSeconds = 10;
    
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;
    
    @Column(name = "last_error")
    private String lastError;
    
    @Column(name = "status")
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, FAILED, DISABLED
    
    @Column(name = "last_triggered_at")
    private LocalDateTime lastTriggeredAt;
    
    @Column(name = "success_count")
    @Builder.Default
    private Integer successCount = 0;
    
    @Column(name = "failure_count")
    @Builder.Default
    private Integer failureCount = 0;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (eventTypes == null) {
            eventTypes = java.util.EnumSet.allOf(WebhookEventType.class);
        }
    }
    
    public enum WebhookEventType {
        PAYMENT_CREATED,
        PAYMENT_COMPLETED,
        PAYMENT_FAILED,
        PAYMENT_CANCELLED,
        PAYMENT_EXPIRED,
        PAYMENT_REFUNDED,
        PACKAGE_UPGRADED,
        INVOICE_GENERATED,
        MERCHANT_PAYMENT_COMPLETED
    }
    
    // Helper methods
    public boolean shouldTriggerForEvent(WebhookEventType eventType) {
        return isActive && eventTypes != null && eventTypes.contains(eventType);
    }
    
    public void recordSuccess() {
        this.successCount++;
        this.lastTriggeredAt = LocalDateTime.now();
        this.currentRetryAttempt = 0;
        this.nextRetryAt = null;
        this.lastError = null;
        this.status = "ACTIVE";
    }
    
    public void recordFailure(String errorMessage) {
        this.failureCount++;
        this.lastTriggeredAt = LocalDateTime.now();
        this.currentRetryAttempt++;
        this.lastError = errorMessage;
        
        if (this.currentRetryAttempt >= this.retryCount) {
            this.status = "FAILED";
        } else {
            calculateNextRetryAt();
        }
    }
    
    public void calculateNextRetryAt() {
        // Exponential backoff: 2^attempt * initial_delay (1 second)
        long delayMs = (long) Math.pow(2, this.currentRetryAttempt) * 1000;
        long maxDelayMs = 60000; // Max 60 seconds
        delayMs = Math.min(delayMs, maxDelayMs);
        
        this.nextRetryAt = LocalDateTime.now().plus(java.time.Duration.ofMillis(delayMs));
    }
    
    public boolean canRetry() {
        return this.currentRetryAttempt < this.retryCount && !"DISABLED".equals(this.status);
    }
    
    public boolean isRetryDue() {
        return this.nextRetryAt != null && LocalDateTime.now().isAfter(this.nextRetryAt);
    }
}
