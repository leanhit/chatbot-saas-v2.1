package com.chatbot.core.payment.plan.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "packages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Package {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "package_id", unique = true, nullable = false, length = 50)
    private String packageId; // free, pro, business, enterprise
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(nullable = false, length = 10)
    private String currency;
    
    @Column(nullable = false, length = 50)
    private String duration; // "1 month", "1 year"
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "message_limit", nullable = false)
    private Integer messageLimit;
    
    @Column(name = "chatbot_limit", nullable = false)
    private Integer chatbotLimit;
    
    @Column(name = "has_priority_support", nullable = false)
    private Boolean hasPrioritySupport;
    
    @Column(name = "has_analytics", nullable = false)
    private Boolean hasAnalytics;
    
    @Column(name = "has_advanced_analytics", nullable = false)
    private Boolean hasAdvancedAnalytics;
    
    @Column(name = "has_custom_integrations", nullable = false)
    private Boolean hasCustomIntegrations;
    
    @Column(name = "has_dedicated_support", nullable = false)
    private Boolean hasDedicatedSupport;
    
    @Column(name = "has_custom_features", nullable = false)
    private Boolean hasCustomFeatures;
    
    @Column(name = "has_sla_guarantee", nullable = false)
    private Boolean hasSlaGuarantee;
    
    @Column(name = "isactive", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
    
    @Column(length = 20)
    private String badge; // POPULAR, RECOMMENDED, null
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Helper methods
    public boolean isFree() {
        return price.compareTo(BigDecimal.ZERO) == 0;
    }
    
    public String getFormattedPrice() {
        if (isFree()) {
            return "Free";
        }
        return String.format("%,.0f ₫", price) + "/" + duration;
    }
}
