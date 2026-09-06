package com.chatbot.core.payment.plan.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DiscountRequest {
    
    @NotBlank(message = "Discount code is required")
    private String code;
    
    @NotBlank(message = "Discount name is required")
    private String name;
    
    @NotNull(message = "Discount type is required")
    private com.chatbot.core.payment.plan.model.Discount.DiscountType discountType;
    
    @NotNull(message = "Discount value is required")
    private BigDecimal discountValue;
    
    @NotNull(message = "Minimum amount is required")
    private BigDecimal minimumAmount;
    
    private BigDecimal maximumDiscount;
    
    private Integer usageLimit;
    
    private Integer usageLimitPerUser;
    
    private Boolean isActive = true;
    
    @NotNull(message = "Valid from date is required")
    private LocalDateTime validFrom;
    
    @NotNull(message = "Valid until date is required")
    private LocalDateTime validUntil;
    
    private String description;
    
    private String applicablePackageId;
}
