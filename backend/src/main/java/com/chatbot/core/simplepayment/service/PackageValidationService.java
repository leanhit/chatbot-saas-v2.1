package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.model.Package;
import com.chatbot.core.simplepayment.repository.PackageRepository;
import com.chatbot.core.simplepayment.repository.SimplePaymentRepository;
import com.chatbot.core.simplepayment.model.PaymentStatus;
import com.chatbot.core.user.repository.UserRepository;
import com.chatbot.core.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackageValidationService {

    private final PackageRepository packageRepository;
    private final SimplePaymentRepository paymentRepository;
    private final UserRepository userRepository;

    /**
     * Validate package before creating payment
     */
    @Transactional(readOnly = true)
    public PackageValidationResult validatePackageForPayment(String packageId, BigDecimal amount, Long userId, Long tenantId) {
        log.info("🔍 Validating package {} for payment amount: {}", packageId, amount);

        PackageValidationResult result = new PackageValidationResult();

        // Find package
        Package targetPackage = findPackageByIdentifier(packageId);
        if (targetPackage == null) {
            result.setValid(false);
            result.addError("Package not found: " + packageId);
            return result;
        }

        result.setPackage(targetPackage);

        // Validate package is active
        if (!targetPackage.getIsActive()) {
            result.setValid(false);
            result.addError("Package is not active: " + packageId);
            return result;
        }

        // Validate amount matches package price (if not free)
        if (!targetPackage.isFree()) {
            BigDecimal expectedPrice = targetPackage.getPrice();
            if (amount.compareTo(expectedPrice) != 0) {
                result.setValid(false);
                result.addError(String.format("Amount mismatch. Expected: %s, Provided: %s", 
                    expectedPrice, amount));
                return result;
            }
        } else {
            // For free packages, amount should be zero
            if (amount.compareTo(BigDecimal.ZERO) != 0) {
                result.setValid(false);
                result.addError("Free package must have zero amount");
                return result;
            }
        }

        // Validate free package limits
        if (targetPackage.isFree()) {
            validateFreePackageLimits(userId, tenantId, result);
        }

        // Validate user hasn't already purchased this package recently
        validateRecentPurchase(userId, tenantId, targetPackage, result);

        // Validate user has sufficient balance (for paid packages)
        if (!targetPackage.isFree()) {
            validateUserBalance(userId, targetPackage.getPrice(), result);
        }

        if (result.isValid()) {
            log.info(" Package validation passed for: {}", packageId);
        } else {
            log.warn(" Package validation failed for: {}", packageId);
            result.getErrors().values().forEach(error -> log.warn("  - {}", error));
        }

        return result;
    }

    /**
     * Find package by ID or packageId
     */
    private Package findPackageByIdentifier(String identifier) {
        try {
            // Try to parse as Long first (for package ID)
            Long packageId = Long.parseLong(identifier);
            return packageRepository.findById(packageId).orElse(null);
        } catch (NumberFormatException e) {
            // If not a number, try to find by packageId field
            return packageRepository.findByPackageId(identifier).orElse(null);
        }
    }

    /**
     * Validate free package limits
     */
    private void validateFreePackageLimits(Long userId, Long tenantId, PackageValidationResult result) {
        log.info("🔍 [FREE PACKAGE] Validating deposit limits for user: {}, tenant: {}", userId, tenantId);

        LocalDateTime monthStart = LocalDateTime.now()
            .withDayOfMonth(1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0);

        // Count deposits for this month for the tenant
        Long monthlyDepositCount = paymentRepository.countByTenantIdAndCreatedAtAfterAndStatus(
            tenantId, monthStart, PaymentStatus.COMPLETED);

        // Calculate total amount deposited this month
        BigDecimal monthlyTotalAmount = paymentRepository.sumAmountByTenantIdAndCreatedAtAfterAndStatus(
            tenantId, monthStart, PaymentStatus.COMPLETED);

        // Free package limits
        int maxMonthlyDeposits = 3;
        BigDecimal maxMonthlyAmount = new BigDecimal("1000000");

        // Check monthly deposit count limit
        if (monthlyDepositCount >= maxMonthlyDeposits) {
            result.addError(String.format(
                "Free package limit exceeded! Maximum %d deposits per month. You have made %d deposits this month.",
                maxMonthlyDeposits, monthlyDepositCount));
            return;
        }

        // Check monthly amount limit
        if (monthlyTotalAmount.compareTo(maxMonthlyAmount) >= 0) {
            result.addError(String.format(
                "Free package limit exceeded! Maximum deposit amount is %,.0f VND per month. You have deposited %,.0f VND this month.",
                maxMonthlyAmount, monthlyTotalAmount));
            return;
        }

        // Add usage info to result
        result.setUsageInfo(Map.of(
            "monthlyDepositCount", monthlyDepositCount,
            "monthlyTotalAmount", monthlyTotalAmount,
            "maxMonthlyDeposits", maxMonthlyDeposits,
            "maxMonthlyAmount", maxMonthlyAmount,
            "remainingDeposits", Math.max(0, maxMonthlyDeposits - monthlyDepositCount.intValue()),
            "remainingAmount", maxMonthlyAmount.subtract(monthlyTotalAmount).max(BigDecimal.ZERO)
        ));
    }

    /**
     * Validate user hasn't purchased the same package recently
     */
    private void validateRecentPurchase(Long userId, Long tenantId, Package targetPackage, PackageValidationResult result) {
        // For non-free packages, check if user already has active subscription
        if (!targetPackage.isFree()) {
            LocalDateTime recentTime = LocalDateTime.now().minusDays(30);
            boolean recentPurchase = paymentRepository.existsByUserIdAndTenantIdAndTargetPackageIdAndCreatedAtAfterAndStatus(
                userId, tenantId, targetPackage.getPackageId(), recentTime, PaymentStatus.COMPLETED);

            if (recentPurchase) {
                result.addWarning("You have already purchased this package recently. Consider upgrading to a longer duration package.");
            }
        }
    }

    /**
     * Get current package usage statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getPackageUsageStats(Long userId, Long tenantId) {
        log.info("📊 Getting package usage stats for user: {}, tenant: {}", userId, tenantId);

        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime monthStart = LocalDateTime.now()
            .withDayOfMonth(1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0);

        // Monthly stats
        Long monthlyDepositCount = paymentRepository.countByTenantIdAndCreatedAtAfterAndStatus(
            tenantId, monthStart, PaymentStatus.COMPLETED);
        
        BigDecimal monthlyTotalAmount = paymentRepository.sumAmountByTenantIdAndCreatedAtAfterAndStatus(
            tenantId, monthStart, PaymentStatus.COMPLETED);

        // All time stats
        Long totalDepositCount = paymentRepository.countByTenantIdAndStatus(tenantId, PaymentStatus.COMPLETED);
        BigDecimal totalAmount = paymentRepository.sumAmountByTenantIdAndStatus(tenantId, PaymentStatus.COMPLETED);

        stats.put("tenantId", tenantId);
        stats.put("userId", userId);
        stats.put("periodStart", monthStart);
        stats.put("monthlyDepositCount", monthlyDepositCount);
        stats.put("monthlyTotalAmount", monthlyTotalAmount);
        stats.put("totalDepositCount", totalDepositCount);
        stats.put("totalAmount", totalAmount);

        // Available packages
        stats.put("availablePackages", packageRepository.findActivePackagesOrdered());

        return stats;
    }

    /**
     * Validate user has sufficient balance for package purchase
     */
    private void validateUserBalance(Long userId, BigDecimal requiredAmount, PackageValidationResult result) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            // Initialize balance if null
            if (user.getBalance() == null) {
                user.setBalance(BigDecimal.ZERO);
                userRepository.save(user);
            }

            // Check sufficient balance
            if (user.getBalance().compareTo(requiredAmount) < 0) {
                result.setValid(false);
                result.addError(String.format(
                    "Insufficient balance. Required: %s, Available: %s", 
                    requiredAmount, user.getBalance()
                ));
                log.warn(" Balance validation failed for user {}: required={}, available={}", 
                        userId, requiredAmount, user.getBalance());
            } else {
                log.info(" Balance validation passed for user {}: required={}, available={}", 
                        userId, requiredAmount, user.getBalance());
            }
        } catch (Exception e) {
            result.setValid(false);
            result.addError("Balance validation error: " + e.getMessage());
            log.error(" Error validating balance for user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Inner class for validation result
     */
    public static class PackageValidationResult {
        private boolean valid = true;
        private Package packageInfo;
        private Map<String, Object> usageInfo;
        private final Map<String, String> errors = new HashMap<>();
        private final Map<String, String> warnings = new HashMap<>();

        // Getters and setters
        public boolean isValid() { return valid && errors.isEmpty(); }
        public void setValid(boolean valid) { this.valid = valid; }
        public Package getPackage() { return packageInfo; }
        public void setPackage(Package packageInfo) { this.packageInfo = packageInfo; }
        public Map<String, Object> getUsageInfo() { return usageInfo; }
        public void setUsageInfo(Map<String, Object> usageInfo) { this.usageInfo = usageInfo; }
        public Map<String, String> getErrors() { return errors; }
        public Map<String, String> getWarnings() { return warnings; }

        public void addError(String error) { 
            this.valid = false;
            errors.put("error_" + errors.size(), error); 
        }
        
        public void addWarning(String warning) { 
            warnings.put("warning_" + warnings.size(), warning); 
        }
    }
}
