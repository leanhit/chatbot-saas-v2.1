package com.chatbot.core.payment.plan.service;

import com.chatbot.core.payment.plan.model.Discount;
import com.chatbot.core.payment.plan.repository.DiscountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountService {

    private final DiscountRepository discountRepository;

    /**
     * Get all active discounts
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    @Cacheable(value = "discounts", key = "'all-active'")
    public List<Discount> getActiveDiscounts() {
        log.info("🎟️ Fetching all active discounts");
        return discountRepository.findActiveDiscounts(LocalDateTime.now());
    }

    /**
     * Get discount by code
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    @Cacheable(value = "discounts", key = "#code")
    public Optional<Discount> getDiscountByCode(String code) {
        log.debug("🎟️ Fetching discount: {}", code);
        return discountRepository.findByCode(code);
    }

    /**
     * Validate and apply discount
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public DiscountValidationResult validateDiscount(String code, Long userId, BigDecimal amount, String packageId) {
        log.info("🎟️ Validating discount: {} for user: {}, amount: {}", code, userId, amount);

        Optional<Discount> discountOpt = discountRepository.findActiveDiscountByCode(code, LocalDateTime.now());
        
        if (discountOpt.isEmpty()) {
            return DiscountValidationResult.invalid("Discount code not found or expired");
        }

        Discount discount = discountOpt.get();

        validationChecks:
        {
            // Check if discount is valid
            if (!discount.isValid()) {
                break validationChecks;
            }

            // Check minimum amount
            if (amount.compareTo(discount.getMinimumAmount()) < 0) {
                return DiscountValidationResult.invalid(
                    "Minimum amount required: " + discount.getMinimumAmount()
                );
            }

            // Check if applicable to package
            if (!discount.isApplicableToPackage(packageId)) {
                return DiscountValidationResult.invalid(
                    "Discount not applicable to this package"
                );
            }

            // Check user usage limit
            if (!discount.canBeUsedByUser(userId)) {
                return DiscountValidationResult.invalid(
                    "You have reached the usage limit for this discount"
                );
            }

            // Calculate discount
            BigDecimal discountAmount = discount.calculateDiscount(amount);
            BigDecimal finalAmount = amount.subtract(discountAmount);

            return DiscountValidationResult.valid(discount, discountAmount, finalAmount);
        }

        return DiscountValidationResult.invalid("Discount is not valid");
    }

    /**
     * Record discount usage
     */
    @Transactional(transactionManager = "sharedTransactionManager")
    @CacheEvict(value = "discounts", allEntries = true)
    public void recordDiscountUsage(String code, Long userId) {
        log.info("🎟️ Recording discount usage: {} for user: {}", code, userId);

        Discount discount = discountRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Discount not found: " + code));

        // Increment usage count
        discount.setUsageCount(discount.getUsageCount() + 1);

        // Add user to usedByUserIds
        if (discount.getUsedByUserIds() == null) {
            discount.setUsedByUserIds(List.of(userId));
        } else {
            List<Long> usedByUserIds = new java.util.ArrayList<>(discount.getUsedByUserIds());
            usedByUserIds.add(userId);
            discount.setUsedByUserIds(usedByUserIds);
        }

        // Deactivate if usage limit reached
        if (discount.getUsageLimit() != null && discount.getUsageCount() >= discount.getUsageLimit()) {
            discount.setIsActive(false);
            log.info("🎟️ Discount {} deactivated - usage limit reached", code);
        }

        discountRepository.save(discount);
    }

    /**
     * Create new discount
     */
    @Transactional(transactionManager = "sharedTransactionManager")
    @CacheEvict(value = "discounts", allEntries = true)
    public Discount createDiscount(Discount discount) {
        log.info("🎟️ Creating new discount: {}", discount.getCode());

        if (discountRepository.existsByCode(discount.getCode())) {
            throw new RuntimeException("Discount code already exists: " + discount.getCode());
        }

        return discountRepository.save(discount);
    }

    /**
     * Update discount
     */
    @Transactional(transactionManager = "sharedTransactionManager")
    @CacheEvict(value = "discounts", allEntries = true)
    public Discount updateDiscount(String code, Discount discount) {
        log.info("🎟️ Updating discount: {}", code);

        Discount existing = discountRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Discount not found: " + code));

        // Update fields
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

        return discountRepository.save(existing);
    }

    /**
     * Delete discount
     */
    @Transactional(transactionManager = "sharedTransactionManager")
    @CacheEvict(value = "discounts", allEntries = true)
    public void deleteDiscount(String code) {
        log.info("🗑️ Deleting discount: {}", code);
        discountRepository.deleteByCode(code);
    }

    /**
     * Deactivate expired discounts
     */
    @Transactional(transactionManager = "sharedTransactionManager")
    @CacheEvict(value = "discounts", allEntries = true)
    public void deactivateExpiredDiscounts() {
        log.info("🎟️ Deactivating expired discounts");
        
        List<Discount> expiredDiscounts = discountRepository.findActiveDiscounts(LocalDateTime.now())
                .stream()
                .filter(d -> !d.isValid())
                .toList();

        for (Discount discount : expiredDiscounts) {
            discount.setIsActive(false);
            discountRepository.save(discount);
            log.info("🎟️ Deactivated expired discount: {}", discount.getCode());
        }
    }

    public static class DiscountValidationResult {
        private final boolean valid;
        private final Discount discount;
        private final BigDecimal discountAmount;
        private final BigDecimal finalAmount;
        private final String errorMessage;

        private DiscountValidationResult(boolean valid, Discount discount, 
                                         BigDecimal discountAmount, BigDecimal finalAmount, 
                                         String errorMessage) {
            this.valid = valid;
            this.discount = discount;
            this.discountAmount = discountAmount;
            this.finalAmount = finalAmount;
            this.errorMessage = errorMessage;
        }

        public static DiscountValidationResult valid(Discount discount, BigDecimal discountAmount, BigDecimal finalAmount) {
            return new DiscountValidationResult(true, discount, discountAmount, finalAmount, null);
        }

        public static DiscountValidationResult invalid(String errorMessage) {
            return new DiscountValidationResult(false, null, null, null, errorMessage);
        }

        // Getters
        public boolean isValid() { return valid; }
        public Discount getDiscount() { return discount; }
        public BigDecimal getDiscountAmount() { return discountAmount; }
        public BigDecimal getFinalAmount() { return finalAmount; }
        public String getErrorMessage() { return errorMessage; }
    }
}
