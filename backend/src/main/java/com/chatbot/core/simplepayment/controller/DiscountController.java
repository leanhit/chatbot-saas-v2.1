package com.chatbot.core.simplepayment.controller;

import com.chatbot.core.simplepayment.dto.DiscountRequest;
import com.chatbot.core.simplepayment.model.Discount;
import com.chatbot.core.simplepayment.service.DiscountService;
import com.chatbot.shared.constants.ApiConstants;
import com.chatbot.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping(ApiConstants.BASE_PATH + "/discounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Discount Management", description = "API for managing discount/promotion codes")
public class DiscountController {

    private final DiscountService discountService;

    /**
     * Create new discount code (admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create discount code", description = "Create a new discount/promotion code (Admin only)")
    public ResponseEntity<ApiResponse<Discount>> createDiscount(@Valid @RequestBody DiscountRequest request) {
        log.info("🎟️ Creating discount code: {}", request.getCode());
        try {
            Discount discount = convertToDiscount(request);
            Discount created = discountService.createDiscount(discount);
            return ResponseEntity.ok(ApiResponse.success(created, "Discount created successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Validate discount code
     */
    @PostMapping("/validate")
    @Operation(summary = "Validate discount code", description = "Validate a discount code and calculate discount amount")
    public ResponseEntity<ApiResponse<Object>> validateDiscount(
            @RequestParam String code,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String packageId,
            @RequestParam(required = false) Long userId) {
        log.info("🔍 Validating discount code: {} for amount: {}", code, amount);
        try {
            var result = discountService.validateDiscount(code, amount, packageId, userId);
            return ResponseEntity.ok(ApiResponse.success(result, result.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get all active discounts
     */
    @GetMapping("/active")
    @Operation(summary = "Get active discounts", description = "Get all active discount codes")
    public ResponseEntity<ApiResponse<List<Discount>>> getActiveDiscounts() {
        log.info("📋 Fetching active discounts");
        List<Discount> discounts = discountService.getActiveDiscounts();
        return ResponseEntity.ok(ApiResponse.success(discounts, "Active discounts retrieved successfully"));
    }

    /**
     * Get discount by code
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "Get discount by code", description = "Get discount information by code")
    public ResponseEntity<ApiResponse<Discount>> getDiscountByCode(@PathVariable String code) {
        log.info("🔍 Fetching discount by code: {}", code);
        Discount discount = discountService.getDiscountByCode(code);
        if (discount == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success(discount, "Discount retrieved successfully"));
    }

    /**
     * Update discount (admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update discount", description = "Update an existing discount code (Admin only)")
    public ResponseEntity<ApiResponse<Discount>> updateDiscount(
            @PathVariable Long id,
            @Valid @RequestBody DiscountRequest request) {
        log.info("🔄 Updating discount: {}", id);
        try {
            Discount discount = convertToDiscount(request);
            Discount updated = discountService.updateDiscount(id, discount);
            return ResponseEntity.ok(ApiResponse.success(updated, "Discount updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Delete discount (admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete discount", description = "Delete a discount code (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteDiscount(@PathVariable Long id) {
        log.info("🗑️ Deleting discount: {}", id);
        try {
            discountService.deleteDiscount(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Discount deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Deactivate expired discounts (admin only)
     */
    @PostMapping("/deactivate-expired")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate expired discounts", description = "Deactivate all expired discount codes (Admin only)")
    public ResponseEntity<ApiResponse<String>> deactivateExpiredDiscounts() {
        log.info("⏰ Deactivating expired discounts");
        try {
            discountService.deactivateExpiredDiscounts();
            return ResponseEntity.ok(ApiResponse.success("Expired discounts deactivated", "Operation completed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    private Discount convertToDiscount(DiscountRequest request) {
        return Discount.builder()
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
    }
}
