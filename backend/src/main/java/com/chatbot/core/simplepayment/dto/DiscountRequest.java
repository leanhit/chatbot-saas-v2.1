package com.chatbot.core.simplepayment.dto;

import com.chatbot.core.simplepayment.model.Discount;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DiscountRequest {
    
    @NotBlank(message = "Discount code is required")
    private String code;
    
    @NotBlank(message = "Discount name is required")
    private String name;
    
    @NotNull(message = "Discount type is required")
    private Discount.DiscountType discountType;
    
    @NotNull(message = "Discount value is required")
    @Positive(message = "Discount value must be positive")
    private BigDecimal discountValue;
    
    private BigDecimal minimumAmount = BigDecimal.ZERO;
    
    private BigDecimal maximumDiscount;
    
    private Integer usageLimit;
    
    private Integer usageLimitPerUser;
    
    private Boolean isActive = true;
    
    private LocalDateTime validFrom;
    
    private LocalDateTime validUntil;
    
    private String description;
    
    private String applicablePackageId; // null = applicable to all packages
}
