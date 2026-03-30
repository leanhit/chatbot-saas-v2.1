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
    
    @Column(unique = true, nullable = false, length = 50)
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
    
    @Column(nullable = false)
    private Integer messageLimit;
    
    @Column(nullable = false)
    private Integer chatbotLimit;
    
    @Column(nullable = false)
    private Boolean hasPrioritySupport;
    
    @Column(nullable = false)
    private Boolean hasAnalytics;
    
    @Column(nullable = false)
    private Boolean hasAdvancedAnalytics;
    
    @Column(nullable = false)
    private Boolean hasCustomIntegrations;
    
    @Column(nullable = false)
    private Boolean hasDedicatedSupport;
    
    @Column(nullable = false)
    private Boolean hasCustomFeatures;
    
    @Column(nullable = false)
    private Boolean hasSlaGuarantee;
    
    @Column(nullable = false)
    private Boolean isActive;
    
    @Column(nullable = false)
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
