package com.chatbot.core.payment.plan.controller;

import com.chatbot.core.payment.plan.dto.DiscountRequest;
import com.chatbot.core.payment.plan.dto.DiscountResponse;
import com.chatbot.core.payment.plan.model.Discount;
import com.chatbot.core.payment.plan.service.DiscountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment/discounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Discount Management", description = "Discount management endpoints")
public class DiscountController {

    private final DiscountService discountService;

    /**
     * Get all active discounts
     */
    @GetMapping("/active")
    @Operation(
        summary = "Get active discounts",
        description = "Get all active discounts"
    )
    public ResponseEntity<List<DiscountResponse>> getActiveDiscounts() {
        log.info("🎟️ Fetching active discounts");
        
        List<Discount> discounts = discountService.getActiveDiscounts();
        List<DiscountResponse> responses = discounts.stream()
                .map(DiscountResponse::from)
                .toList();
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Validate discount
     */
    @PostMapping("/validate")
    @Operation(
        summary = "Validate discount",
        description = "Validate a discount code for a specific user and amount"
    )
    public ResponseEntity<Object> validateDiscount(
            @RequestBody Map<String, Object> request) {
        
        String code = (String) request.get("code");
        Long userId = Long.valueOf(request.get("userId").toString());
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String packageId = (String) request.get("packageId");
        
        log.info("🎟️ Validating discount: {} for user: {}, amount: {}", code, userId, amount);
        
        var result = discountService.validateDiscount(code, userId, amount, packageId);
        
        if (result.isValid()) {
            return ResponseEntity.ok(Map.of(
                "valid", true,
                "discountAmount", result.getDiscountAmount(),
                "finalAmount", result.getFinalAmount(),
                "discount", DiscountResponse.from(result.getDiscount())
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                "valid", false,
                "error", result.getErrorMessage()
            ));
        }
    }

    /**
     * Get discount by code
     */
    @GetMapping("/{code}")
    @Operation(
        summary = "Get discount by code",
        description = "Get a specific discount by its code"
    )
    public ResponseEntity<DiscountResponse> getDiscount(@PathVariable String code) {
        log.info("🎟️ Fetching discount: {}", code);
        
        return discountService.getDiscountByCode(code)
                .map(DiscountResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create new discount - Admin only
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create discount (Admin)",
        description = "Create a new discount - Admin only"
    )
    public ResponseEntity<DiscountResponse> createDiscount(@RequestBody DiscountRequest request) {
        log.info("🎟️ Creating new discount: {}", request.getCode());
        
        try {
            Discount discount = Discount.builder()
                    .code(request.getCode())
                    .name(request.getName())
                    .discountType(request.getDiscountType())
                    .discountValue(request.getDiscountValue())
                    .minimumAmount(request.getMinimumAmount())
                    .maximumDiscount(request.getMaximumDiscount())
                    .usageLimit(request.getUsageLimit())
                    .usageLimitPerUser(request.getUsageLimitPerUser())
                    .isActive(request.getIsActive())
                    .validFrom(request.getValidFrom())
                    .validUntil(request.getValidUntil())
                    .description(request.getDescription())
                    .applicablePackageId(request.getApplicablePackageId())
                    .build();
            
            Discount created = discountService.createDiscount(discount);
            return ResponseEntity.ok(DiscountResponse.from(created));
        } catch (Exception e) {
            log.error("❌ Failed to create discount: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update discount - Admin only
     */
    @PutMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update discount (Admin)",
        description = "Update an existing discount - Admin only"
    )
    public ResponseEntity<DiscountResponse> updateDiscount(
            @PathVariable String code,
            @RequestBody DiscountRequest request) {
        log.info("🎟️ Updating discount: {}", code);
        
        try {
            Discount discount = Discount.builder()
                    .name(request.getName())
                    .discountType(request.getDiscountType())
                    .discountValue(request.getDiscountValue())
                    .minimumAmount(request.getMinimumAmount())
                    .maximumDiscount(request.getMaximumDiscount())
                    .usageLimit(request.getUsageLimit())
                    .usageLimitPerUser(request.getUsageLimitPerUser())
                    .isActive(request.getIsActive())
                    .validFrom(request.getValidFrom())
                    .validUntil(request.getValidUntil())
                    .description(request.getDescription())
                    .applicablePackageId(request.getApplicablePackageId())
                    .build();
            
            Discount updated = discountService.updateDiscount(code, discount);
            return ResponseEntity.ok(DiscountResponse.from(updated));
        } catch (Exception e) {
            log.error("❌ Failed to update discount: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete discount - Admin only
     */
    @DeleteMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete discount (Admin)",
        description = "Delete a discount - Admin only"
    )
    public ResponseEntity<Map<String, String>> deleteDiscount(@PathVariable String code) {
        log.info("🗑️ Deleting discount: {}", code);
        
        try {
            discountService.deleteDiscount(code);
            return ResponseEntity.ok(Map.of("message", "Discount deleted successfully"));
        } catch (Exception e) {
            log.error("❌ Failed to delete discount: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Deactivate expired discounts - Admin only
     */
    @PostMapping("/deactivate-expired")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Deactivate expired discounts (Admin)",
        description = "Deactivate all expired discounts - Admin only"
    )
    public ResponseEntity<Map<String, String>> deactivateExpiredDiscounts() {
        log.info("🎟️ Deactivating expired discounts");
        
        discountService.deactivateExpiredDiscounts();
        return ResponseEntity.ok(Map.of("message", "Expired discounts deactivated successfully"));
    }
}
