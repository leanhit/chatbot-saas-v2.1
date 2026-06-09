package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.model.Discount;
import com.chatbot.core.simplepayment.repository.DiscountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountService {

    private final DiscountRepository discountRepository;

    /**
     * Create new discount code
     */
    @Transactional("sharedTransactionManager")
    public Discount createDiscount(Discount discount) {
        log.info("🎟️ Creating discount code: {}", discount.getCode());

        // Check if code already exists
        if (discountRepository.existsByCode(discount.getCode())) {
            throw new IllegalArgumentException("Discount code already exists: " + discount.getCode());
        }

        // Set default values
        if (discount.getIsActive() == null) {
            discount.setIsActive(true);
        }
        if (discount.getUsageCount() == null) {
            discount.setUsageCount(0);
        }
        if (discount.getValidFrom() == null) {
            discount.setValidFrom(LocalDateTime.now());
        }

        Discount saved = discountRepository.save(discount);
        log.info("✅ Discount code created: {}", saved.getCode());
        return saved;
    }

    /**
     * Validate and apply discount code
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public DiscountValidationResult validateDiscount(String code, BigDecimal amount, String packageId, Long userId) {
        log.info("🔍 Validating discount code: {} for amount: {}, package: {}", code, amount, packageId);

        Discount discount = discountRepository.findActiveDiscountByCode(code, LocalDateTime.now())
                .orElse(null);

        if (discount == null) {
            return DiscountValidationResult.invalid("Discount code not found or expired");
        }

        // Check if discount is valid
        if (!discount.isValid()) {
            return DiscountValidationResult.invalid("Discount code is not valid");
        }

        // Check minimum amount
        if (discount.getMinimumAmount() != null && amount.compareTo(discount.getMinimumAmount()) < 0) {
            return DiscountValidationResult.invalid(
                String.format("Minimum amount %s required for this discount", discount.getMinimumAmount())
            );
        }

        // Check usage limit
        if (discount.getUsageLimit() != null && discount.getUsageCount() >= discount.getUsageLimit()) {
            return DiscountValidationResult.invalid("Discount code has reached its usage limit");
        }

        // Check user usage limit
        if (userId != null && !discount.canBeUsedByUser(userId)) {
            return DiscountValidationResult.invalid("You have already used this discount code");
        }

        // Check package applicability
        if (!discount.isApplicableToPackage(packageId)) {
            return DiscountValidationResult.invalid("This discount is not applicable to this package");
        }

        // Calculate discount
        BigDecimal discountAmount = discount.calculateDiscount(amount);
        BigDecimal finalAmount = amount.subtract(discountAmount);

        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        log.info("✅ Discount validated: {} - Original: {}, Discount: {}, Final: {}", 
                code, amount, discountAmount, finalAmount);

        return DiscountValidationResult.valid(discount, discountAmount, finalAmount);
    }

    /**
     * Use discount code (increment usage count)
     */
    @Transactional("sharedTransactionManager")
    public void useDiscount(String code, Long userId) {
        log.info("🎟️ Using discount code: {} by user: {}", code, userId);

        Discount discount = discountRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Discount not found: " + code));

        // Increment usage count
        discount.setUsageCount(discount.getUsageCount() + 1);

        // Add user to used list
        if (userId != null) {
            if (discount.getUsedByUserIds() == null) {
                discount.setUsedByUserIds(List.of(userId));
            } else {
                discount.getUsedByUserIds().add(userId);
            }
        }

        discountRepository.save(discount);
        log.info("✅ Discount used: {} (usage count: {})", code, discount.getUsageCount());
    }

    /**
     * Get all active discounts
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    @Cacheable(value = "activeDiscounts", key = "'all'")
    public List<Discount> getActiveDiscounts() {
        return discountRepository.findActiveDiscounts(LocalDateTime.now());
    }

    /**
     * Get discount by code
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public Discount getDiscountByCode(String code) {
        return discountRepository.findByCode(code).orElse(null);
    }

    /**
     * Update discount
     */
    @Transactional("sharedTransactionManager")
    @CacheEvict(value = "activeDiscounts", allEntries = true)
    public Discount updateDiscount(Long id, Discount discount) {
        log.info("🔄 Updating discount: {}", id);

        Discount existing = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found: " + id));

        existing.setName(discount.getName());
        existing.setDiscountType(discount.getDiscountType());
        existing.setDiscountValue(discount.getDiscountValue());
        existing.setMinimumAmount(discount.getMinimumAmount());
        existing.setMaximumDiscount(discount.getMaximumDiscount());
        existing.setUsageLimit(discount.getUsageLimit());
        existing.setUsageLimitPerUser(discount.getUsageLimitPerUser());
        existing.setIsActive(discount.getIsActive());
        existing.setValidFrom(discount.getValidFrom());
        existing.setValidUntil(discount.getValidUntil());
        existing.setDescription(discount.getDescription());
        existing.setApplicablePackageId(discount.getApplicablePackageId());

        Discount updated = discountRepository.save(existing);
        log.info("✅ Discount updated: {}", updated.getCode());
        return updated;
    }

    /**
     * Delete discount
     */
    @Transactional("sharedTransactionManager")
    @CacheEvict(value = "activeDiscounts", allEntries = true)
    public void deleteDiscount(Long id) {
        log.info("🗑️ Deleting discount: {}", id);

        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found: " + id));

        discountRepository.delete(discount);
        log.info("✅ Discount deleted: {}", discount.getCode());
    }

    /**
     * Deactivate expired discounts
     */
    @Transactional("sharedTransactionManager")
    public void deactivateExpiredDiscounts() {
        log.info("⏰ Deactivating expired discounts...");

        List<Discount> expired = discountRepository.findExpiredDiscounts(LocalDateTime.now());
        
        for (Discount discount : expired) {
            discount.setIsActive(false);
            discountRepository.save(discount);
            log.info("🗑️ Deactivated expired discount: {}", discount.getCode());
        }

        log.info("✅ Deactivated {} expired discounts", expired.size());
    }

    /**
     * Result class for discount validation
     */
    public static class DiscountValidationResult {
        private final boolean valid;
        private final String message;
        private final Discount discount;
        private final BigDecimal discountAmount;
        private final BigDecimal finalAmount;

        private DiscountValidationResult(boolean valid, String message, Discount discount, 
                                        BigDecimal discountAmount, BigDecimal finalAmount) {
            this.valid = valid;
            this.message = message;
            this.discount = discount;
            this.discountAmount = discountAmount;
            this.finalAmount = finalAmount;
        }

        public static DiscountValidationResult valid(Discount discount, BigDecimal discountAmount, BigDecimal finalAmount) {
            return new DiscountValidationResult(true, "Discount applied successfully", discount, discountAmount, finalAmount);
        }

        public static DiscountValidationResult invalid(String message) {
            return new DiscountValidationResult(false, message, null, null, null);
        }

        // Getters
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public Discount getDiscount() { return discount; }
        public BigDecimal getDiscountAmount() { return discountAmount; }
        public BigDecimal getFinalAmount() { return finalAmount; }
    }
}
