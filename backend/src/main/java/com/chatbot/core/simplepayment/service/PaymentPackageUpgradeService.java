package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.model.Package;
import com.chatbot.core.simplepayment.model.SimplePayment;
import com.chatbot.core.simplepayment.model.PackageUpgradeAudit;
import com.chatbot.core.simplepayment.repository.PackageUpgradeAuditRepository;
import com.chatbot.core.tenant.service.TenantPackageService;
import com.chatbot.core.user.repository.UserRepository;
import com.chatbot.core.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentPackageUpgradeService {

    private final TenantPackageService tenantPackageService;
    private final PackageService packageService;
    private final PackageUpgradeAuditRepository auditRepository;
    private final UserRepository userRepository;

    // Allowed package IDs for auto-upgrade
    private static final List<String> ALLOWED_UPGRADE_PACKAGES = Arrays.asList(
            "3months", "6months", "12months"
    );

    /**
     * Validate and execute package upgrade after payment completion
     */
    @Transactional
    public boolean processPackageUpgrade(SimplePayment payment) {
        if (payment.getTargetPackageId() == null || payment.getTargetPackageId().trim().isEmpty()) {
            log.debug("💳 Payment {} has no target package, skipping upgrade", payment.getReferenceCode());
            return false;
        }

        String targetPackageId = payment.getTargetPackageId().trim();
        
        // Create audit record
        PackageUpgradeAudit audit = createAuditRecord(payment, targetPackageId);
        
        try {
            // Security validation
            if (!isValidUpgradePackage(targetPackageId)) {
                String error = "Invalid target package " + targetPackageId;
                log.error("❌ {} for payment {}", error, payment.getReferenceCode());
                updateAuditAsFailed(audit, error);
                return false;
            }

            // Validate package exists
            Package targetPackage = packageService.getPackageByPackageId(targetPackageId)
                    .orElse(null);
            if (targetPackage == null) {
                String error = "Package " + targetPackageId + " not found";
                log.error("❌ {} for payment {}", error, payment.getReferenceCode());
                updateAuditAsFailed(audit, error);
                return false;
            }

            // Validate payment amount matches package price
            if (!payment.getAmount().equals(targetPackage.getPrice())) {
                String error = String.format("Payment amount %s doesn't match package price %s", 
                        payment.getAmount(), targetPackage.getPrice());
                log.error("❌ {} for payment {}", error, payment.getReferenceCode());
                updateAuditAsFailed(audit, error);
                return false;
            }

            // Get current package for audit
            String currentPackageId = getCurrentPackageId(payment.getTenantId());
            audit.setFromPackageId(currentPackageId);

            log.info("🔄 [PaymentPackageUpgradeService] About to upgrade tenant {} from {} to {}", 
                    payment.getTenantId(), currentPackageId, targetPackageId);

            // Execute upgrade with balance deduction
            executeUpgradeWithBalanceDeduction(payment, targetPackageId);
            
            log.info("✅ [PaymentPackageUpgradeService] Successfully called upgrade for tenant {} to {}", 
                    payment.getTenantId(), targetPackageId);
            
            // Update audit as successful
            audit.setUpgradeStatus(PackageUpgradeAudit.UpgradeStatus.SUCCESS);
            audit.setProcessedAt(LocalDateTime.now());
            auditRepository.save(audit);
            
            log.info("✅ Auto-upgraded tenant {} from {} to {} after payment {}", 
                    payment.getTenantId(), currentPackageId, targetPackageId, payment.getReferenceCode());
            
            return true;
            
        } catch (Exception e) {
            String error = "Failed to auto-upgrade: " + e.getMessage();
            log.error("❌ {} tenant {} to package {} after payment {}: {}", 
                    error, payment.getTenantId(), targetPackageId, payment.getReferenceCode(), e.getMessage(), e);
            
            updateAuditAsFailed(audit, error);
            return false;
        }
    }

    /**
     * Create audit record for package upgrade
     */
    private PackageUpgradeAudit createAuditRecord(SimplePayment payment, String targetPackageId) {
        PackageUpgradeAudit audit = PackageUpgradeAudit.builder()
                .tenantId(payment.getTenantId())
                .userId(payment.getUserId())
                .paymentReferenceCode(payment.getReferenceCode())
                .toPackageId(targetPackageId)
                .paymentAmount(payment.getAmount())
                .currency(payment.getCurrency())
                .bankTransactionId(payment.getBankTransactionId())
                .upgradeStatus(PackageUpgradeAudit.UpgradeStatus.PENDING)
                .build();
        
        return auditRepository.save(audit);
    }

    /**
     * Update audit record as failed
     */
    private void updateAuditAsFailed(PackageUpgradeAudit audit, String failureReason) {
        audit.setUpgradeStatus(PackageUpgradeAudit.UpgradeStatus.FAILED);
        audit.setFailureReason(failureReason);
        audit.setProcessedAt(LocalDateTime.now());
        auditRepository.save(audit);
    }

    /**
     * Get current package ID for tenant
     */
    private String getCurrentPackageId(Long tenantId) {
        try {
            Package currentPackage = tenantPackageService.getCurrentTenantPackage(tenantId);
            return currentPackage != null ? currentPackage.getPackageId() : null;
        } catch (Exception e) {
            log.warn("Could not get current package for tenant {}: {}", tenantId, e.getMessage());
            return null;
        }
    }

    /**
     * Validate that the target package is allowed for auto-upgrade
     */
    private boolean isValidUpgradePackage(String packageId) {
        return ALLOWED_UPGRADE_PACKAGES.contains(packageId);
    }

    /**
     * Extract package ID from payment description (backward compatibility)
     */
    public String extractPackageIdFromDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return null;
        }

        // Look for package patterns in description
        String desc = description.toLowerCase();
        
        if (desc.contains("3 tháng") || desc.contains("3months")) {
            return "3months";
        }
        if (desc.contains("6 tháng") || desc.contains("6months")) {
            return "6months";
        }
        if (desc.contains("12 tháng") || desc.contains("12months") || desc.contains("1 năm")) {
            return "12months";
        }
        
        return null;
    }

    /**
     * Validate tenant context for security
     */
    public boolean validateTenantContext(Long paymentTenantId, Long currentTenantId) {
        // Ensure payment belongs to the current tenant context
        if (!paymentTenantId.equals(currentTenantId)) {
            log.error("❌ Security violation: Payment tenant {} doesn't match current tenant {}", 
                    paymentTenantId, currentTenantId);
            return false;
        }
        return true;
    }

    /**
     * Get upgrade history for tenant
     */
    @Transactional(readOnly = true)
    public List<PackageUpgradeAudit> getTenantUpgradeHistory(Long tenantId) {
        return auditRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    /**
     * Execute package upgrade with balance deduction
     */
    public void executeUpgradeWithBalanceDeduction(SimplePayment payment, String targetPackageId) {
        log.info(" executing upgrade with balance deduction for payment: {}, targetPackage: {}", 
                payment.getReferenceCode(), targetPackageId);
        
        try {
            // Get package price
            Package targetPackage = packageService.getPackageByPackageId(targetPackageId)
                    .orElseThrow(() -> new RuntimeException("Package not found: " + targetPackageId));
            
            // Check and deduct user balance directly with pessimistic locking
            User user = userRepository.findByIdWithLock(payment.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found: " + payment.getUserId()));
            
            // Initialize balance if null
            if (user.getBalance() == null) {
                user.setBalance(java.math.BigDecimal.ZERO);
                userRepository.save(user);
            }
            
            // Check sufficient balance
            if (user.getBalance().compareTo(targetPackage.getPrice()) < 0) {
                throw new RuntimeException(
                    String.format("Insufficient balance for package upgrade. Required: %s, Available: %s", 
                        targetPackage.getPrice(), user.getBalance())
                );
            }
            
            // Deduct balance
            java.math.BigDecimal oldBalance = user.getBalance();
            user.setBalance(user.getBalance().subtract(targetPackage.getPrice()));
            userRepository.save(user);
            
            log.info(" Deducted balance for user {}: {} - {} = {}", 
                    payment.getUserId(), oldBalance, targetPackage.getPrice(), user.getBalance());
            
            // Then upgrade package
            log.info(" Upgrading tenant {} to package {}", payment.getTenantId(), targetPackageId);
            tenantPackageService.upgradeTenantPackage(payment.getTenantId(), targetPackageId);
            
            log.info(" Successfully completed upgrade with balance deduction for payment: {}", 
                    payment.getReferenceCode());
                    
        } catch (Exception e) {
            log.error(" Failed to execute upgrade with balance deduction for payment {}: {}", 
                    payment.getReferenceCode(), e.getMessage(), e);
            throw e; // Re-throw to mark audit as failed
        }
    }

    /**
     * Get upgrade statistics
     */
    @Transactional(readOnly = true)
    public List<Object[]> getUpgradeStatistics() {
        return auditRepository.countUpgradesByPackage();
    }
}
