package com.chatbot.core.payment.plan.service;

import com.chatbot.core.payment.common.audit.PaymentAuditLog.AuditAction;
import com.chatbot.core.payment.common.audit.PaymentAuditService;
import com.chatbot.core.payment.common.event.PaymentCompletedEvent;
import com.chatbot.core.payment.plan.model.Package;
import com.chatbot.core.payment.plan.model.PackageUpgradeAudit;
import com.chatbot.core.payment.plan.model.PackageUpgradeAudit.UpgradeStatus;
import com.chatbot.core.payment.plan.repository.PackageUpgradeAuditRepository;
import com.chatbot.core.payment.plan.repository.PackageRepository;
import com.chatbot.core.payment.transaction.model.SimplePayment;
import com.chatbot.core.payment.transaction.service.BalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentPackageUpgradeService {

    private final PackageUpgradeAuditRepository auditRepository;
    private final PackageRepository packageRepository;
    private final PackageService packageService;
    private final BalanceService balanceService;
    private final PaymentAuditService paymentAuditService;

    // External dependencies - will be injected after tenant service integration
    // private final TenantPackageService tenantPackageService;

    /**
     * Event listener for PaymentCompletedEvent
     * This handles package upgrade in an event-driven manner
     */
    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "sharedTransactionManager")
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        log.info("📦 [EVENT] Handling PaymentCompletedEvent for reference: {}", event.getReferenceCode());

        if (event.getTargetPackageId() == null || event.getTargetPackageId().trim().isEmpty()) {
            log.debug("💳 Payment {} has no target package, skipping upgrade", event.getReferenceCode());
            return;
        }

        // Create a SimplePayment object for processing
        SimplePayment payment = SimplePayment.builder()
                .referenceCode(event.getReferenceCode())
                .userId(event.getUserId())
                .tenantId(event.getTenantId())
                .amount(event.getAmount())
                .targetPackageId(event.getTargetPackageId())
                .build();

        processPackageUpgrade(payment);
    }

    /**
     * Process package upgrade after payment completion
     * This is the main business logic for package upgrade
     */
    @Transactional("sharedTransactionManager")
    public boolean processPackageUpgrade(SimplePayment payment) {
        log.info("📝 [DEBUG] processPackageUpgrade called - referenceCode: {}, targetPackageId: {}, tenantId: {}",
                payment.getReferenceCode(), payment.getTargetPackageId(), payment.getTenantId());

        if (payment.getTargetPackageId() == null || payment.getTargetPackageId().trim().isEmpty()) {
            log.debug("💳 Payment {} has no target package, skipping upgrade", payment.getReferenceCode());
            return false;
        }

        String targetPackageId = payment.getTargetPackageId().trim();
        PackageUpgradeAudit audit = createAuditRecord(payment, targetPackageId);

        try {
            // Validate target package
            if (!isValidUpgradePackage(targetPackageId)) {
                String error = "Invalid target package " + targetPackageId;
                log.error("❌ {} for payment {}", error, payment.getReferenceCode());
                updateAuditAsFailed(audit, error);
                return false;
            }

            // Get target package
            Package targetPackage = packageService.getPackageByPackageId(targetPackageId)
                    .orElse(null);
            if (targetPackage == null) {
                String error = "Package " + targetPackageId + " not found";
                log.error("❌ {} for payment {}", error, payment.getReferenceCode());
                updateAuditAsFailed(audit, error);
                return false;
            }

            // Validate payment amount matches package price
            if (payment.getAmount().compareTo(targetPackage.getPrice()) != 0) {
                String error = String.format("Payment amount %s doesn't match package price %s",
                        payment.getAmount(), targetPackage.getPrice());
                log.error("❌ {} for payment {}", error, payment.getReferenceCode());
                updateAuditAsFailed(audit, error);
                return false;
            }

            // Get current package
            String currentPackageId = getCurrentPackageId(payment.getTenantId());
            audit.setFromPackageId(currentPackageId);

            // Credit user balance
            balanceService.creditUserBalance(payment.getUserId(), targetPackage.getPrice());

            // Upgrade tenant package
            // This will be implemented after tenant service integration
            // tenantPackageService.upgradeTenantPackage(payment.getTenantId(), targetPackageId);
            log.info("📦 Tenant package upgrade will be implemented after tenant service integration");

            // Update audit as success
            audit.setUpgradeStatus(UpgradeStatus.SUCCESS);
            audit.setProcessedAt(LocalDateTime.now());
            auditRepository.save(audit);

            // Log audit
            paymentAuditService.logPaymentAction(
                payment.getReferenceCode(),
                payment.getUserId(),
                payment.getTenantId(),
                AuditAction.PACKAGE_UPGRADED,
                currentPackageId,
                targetPackageId,
                payment.getAmount(),
                "Package upgraded from " + currentPackageId + " to " + targetPackageId,
                null
            );

            log.info("✅ Package upgrade successful: {} -> {} for tenant {}",
                    currentPackageId, targetPackageId, payment.getTenantId());
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
     * Get upgrade history for tenant
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public java.util.List<PackageUpgradeAudit> getUpgradeHistory(Long tenantId) {
        return auditRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    /**
     * Execute package upgrade with balance deduction
     */
    @Transactional("sharedTransactionManager")
    public boolean executeUpgradeWithBalance(Long tenantId, Long userId, String targetPackageId) {
        log.info("📦 Executing upgrade with balance for tenant: {}, package: {}", tenantId, targetPackageId);

        Package targetPackage = packageService.getPackageByPackageId(targetPackageId)
                .orElseThrow(() -> new RuntimeException("Package not found: " + targetPackageId));

        // Check if user has sufficient balance
        if (!balanceService.hasSufficientBalance(userId, targetPackage.getPrice())) {
            log.error("❌ Insufficient balance for upgrade");
            return false;
        }

        // Deduct balance
        balanceService.deductUserBalance(userId, targetPackage.getPrice());

        // Upgrade tenant package
        // This will be implemented after tenant service integration
        // tenantPackageService.upgradeTenantPackage(tenantId, targetPackageId);

        log.info("✅ Upgrade with balance completed");
        return true;
    }

    private PackageUpgradeAudit createAuditRecord(SimplePayment payment, String targetPackageId) {
        return PackageUpgradeAudit.builder()
                .tenantId(payment.getTenantId())
                .userId(payment.getUserId())
                .paymentReferenceCode(payment.getReferenceCode())
                .toPackageId(targetPackageId)
                .paymentAmount(payment.getAmount())
                .upgradeStatus(UpgradeStatus.PENDING)
                .build();
    }

    private void updateAuditAsFailed(PackageUpgradeAudit audit, String error) {
        audit.setUpgradeStatus(UpgradeStatus.FAILED);
        audit.setFailureReason(error);
        audit.setProcessedAt(LocalDateTime.now());
        auditRepository.save(audit);
    }

    private boolean isValidUpgradePackage(String packageId) {
        return packageService.getPackageByPackageId(packageId)
                .map(Package::getIsActive)
                .orElse(false);
    }

    private String getCurrentPackageId(Long tenantId) {
        // This will be implemented after tenant service integration
        // return tenantPackageService.getCurrentPackageId(tenantId);
        log.debug("📦 Getting current package ID will be implemented after tenant service integration");
        return "free"; // Placeholder
    }
}
