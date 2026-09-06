package com.chatbot.core.payment.merchant.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "merchant_api_keys")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantApiKey {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long tenantId;
    
    @Column(nullable = false, unique = true, length = 100)
    private String apiKey; // Public API key for authentication
    
    @Column(nullable = false, length = 100)
    private String apiSecret; // Secret key for HMAC signature
    
    @Column(nullable = false, length = 100)
    private String name; // Merchant's name/website name
    
    @Column(length = 500)
    private String description;
    
    @Column(length = 500)
    private String webhookUrl; // Merchant's webhook URL for payment notifications
    
    @Column(nullable = false, length = 50)
    private String webhookSecret; // Secret for webhook signature verification
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "rate_limit_per_minute")
    @Builder.Default
    private Integer rateLimitPerMinute = 100;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @Column(name = "usage_count")
    @Builder.Default
    private Long usageCount = 0L;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (apiKey == null) {
            apiKey = generateApiKey();
        }
        if (apiSecret == null) {
            apiSecret = generateApiSecret();
        }
        if (webhookSecret == null) {
            webhookSecret = generateWebhookSecret();
        }
    }
    
    /**
     * Generate a random API key
     * Format: pk_live_xxxxxxxxxxxxxxxx
     */
    private String generateApiKey() {
        String prefix = "pk_live_";
        String random = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        return prefix + random;
    }
    
    /**
     * Generate a random API secret
     * Format: sk_live_xxxxxxxxxxxxxxxx
     */
    private String generateApiSecret() {
        String prefix = "sk_live_";
        String random = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        return prefix + random;
    }
    
    /**
     * Generate a random webhook secret
     */
    private String generateWebhookSecret() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * Check if API key is valid and active
     */
    public boolean isValid() {
        if (!isActive) {
            return false;
        }
        
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Record API key usage
     */
    public void recordUsage() {
        this.usageCount++;
        this.lastUsedAt = LocalDateTime.now();
    }
}
