package com.chatbot.core.payment.plan.dto;

import com.chatbot.core.payment.plan.model.Discount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountResponse {
    private Long id;
    private String code;
    private String name;
    private Discount.DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minimumAmount;
    private BigDecimal maximumDiscount;
    private Integer usageLimit;
    private Integer usageCount;
    private Integer usageLimitPerUser;
    private Boolean isActive;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private String description;
    private String applicablePackageId;

    public static DiscountResponse from(Discount discount) {
        return DiscountResponse.builder()
                .id(discount.getId())
                .code(discount.getCode())
                .name(discount.getName())
                .discountType(discount.getDiscountType())
                .discountValue(discount.getDiscountValue())
                .minimumAmount(discount.getMinimumAmount())
                .maximumDiscount(discount.getMaximumDiscount())
                .usageLimit(discount.getUsageLimit())
                .usageCount(discount.getUsageCount())
                .usageLimitPerUser(discount.getUsageLimitPerUser())
                .isActive(discount.getIsActive())
                .validFrom(discount.getValidFrom())
                .validUntil(discount.getValidUntil())
                .description(discount.getDescription())
                .applicablePackageId(discount.getApplicablePackageId())
                .build();
    }
}
