package com.chatbot.core.payment.gateway.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Dead letter queue for failed webhooks.
 * Stores webhooks that failed after max retries for manual inspection and reprocessing.
 */
@Entity
@Table(name = "webhook_dead_letters")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookDeadLetter {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "webhook_id", nullable = false)
    private Long webhookId;
    
    @Column(name = "webhook_name", nullable = false, length = 100)
    private String webhookName;
    
    @Column(name = "webhook_url", nullable = false, length = 500)
    private String webhookUrl;
    
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;
    
    @Column(name = "payment_reference_code", nullable = false, length = 100)
    private String paymentReferenceCode;
    
    @Column(name = "retry_attempts", nullable = false)
    private Integer retryAttempts;
    
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;
    
    @Column(name = "last_error", nullable = false, columnDefinition = "TEXT")
    private String lastError;
    
    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, PROCESSED, DISCARDED
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "processed_by")
    private String processedBy;
    
    @Column(name = "processing_notes", columnDefinition = "TEXT")
    private String processingNotes;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = "PENDING";
        }
    }
    
    public void markAsProcessed(String processedBy, String notes) {
        this.status = "PROCESSED";
        this.processedAt = LocalDateTime.now();
        this.processedBy = processedBy;
        this.processingNotes = notes;
    }
    
    public void markAsDiscarded(String notes) {
        this.status = "DISCARDED";
        this.processedAt = LocalDateTime.now();
        this.processingNotes = notes;
    }
}
