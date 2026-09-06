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
@Table(name = "discounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Discount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String code; // Discount code (e.g., "WELCOME10", "SUMMER2024")
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false)
    private DiscountType discountType; // PERCENTAGE, FIXED_AMOUNT
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue; // Percentage (e.g., 10.00) or fixed amount (e.g., 50000.00)
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal minimumAmount; // Minimum order amount to apply discount
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal maximumDiscount; // Maximum discount amount (for percentage discounts)
    
    @Column(nullable = false)
    private Integer usageLimit; // Total usage limit (null = unlimited)
    
    @Column(nullable = false)
    private Integer usageCount; // Current usage count
    
    @Column(nullable = false)
    private Integer usageLimitPerUser; // Usage limit per user (null = unlimited)
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private LocalDateTime validFrom;
    
    @Column(nullable = false)
    private LocalDateTime validUntil;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(length = 50)
    private String applicablePackageId; // null = applicable to all packages
    
    @ElementCollection
    @CollectionTable(name = "discount_user_usage", joinColumns = @JoinColumn(name = "discount_id"))
    @Column(name = "user_id")
    private List<Long> usedByUserIds; // Track which users have used this discount
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (usageCount == null) {
            usageCount = 0;
        }
    }
    
    public enum DiscountType {
        PERCENTAGE,
        FIXED_AMOUNT
    }
    
    // Helper methods
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return isActive 
            && (validFrom == null || !validFrom.isAfter(now))
            && (validUntil == null || !validUntil.isBefore(now))
            && (usageLimit == null || usageCount < usageLimit);
    }
    
    public boolean canBeUsedByUser(Long userId) {
        if (usageLimitPerUser == null) {
            return true;
        }
        
        long userUsageCount = usedByUserIds == null ? 0 : usedByUserIds.stream()
            .filter(id -> id.equals(userId))
            .count();
        
        return userUsageCount < usageLimitPerUser;
    }
    
    public BigDecimal calculateDiscount(BigDecimal originalAmount) {
        if (discountType == DiscountType.PERCENTAGE) {
            BigDecimal discount = originalAmount.multiply(discountValue.divide(BigDecimal.valueOf(100)));
            if (maximumDiscount != null && discount.compareTo(maximumDiscount) > 0) {
                return maximumDiscount;
            }
            return discount;
        } else {
            return discountValue;
        }
    }
    
    public boolean isApplicableToPackage(String packageId) {
        return applicablePackageId == null || applicablePackageId.equals(packageId);
    }
}
