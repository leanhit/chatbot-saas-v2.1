package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.dto.DepositRequest;
import com.chatbot.core.simplepayment.dto.DepositResponse;
import com.chatbot.core.simplepayment.dto.PaymentEvent;

import com.chatbot.core.simplepayment.dto.PaymentStatusResponse;
import com.chatbot.core.simplepayment.model.PaymentStatus;
import com.chatbot.core.simplepayment.model.SimplePayment;
import com.chatbot.core.simplepayment.model.Package;
import com.chatbot.core.simplepayment.repository.SimplePaymentRepository;
import com.chatbot.core.simplepayment.repository.PackageRepository;
import com.chatbot.core.simplepayment.metrics.PaymentMetricsService;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.shared.exceptions.ResourceNotFoundException;
import com.chatbot.core.simplepayment.exception.PaymentNotFoundException;
import com.chatbot.core.simplepayment.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimplePaymentService {

    private final SimplePaymentRepository paymentRepository;
    private final PackageRepository packageRepository;
    private final UserBalanceService userBalanceService;
    private final QRCodeService qrCodeService;
    private final BankApiService bankApiService;
    private final RedisPaymentService redisPaymentService;
    private final PaymentPackageUpgradeService packageUpgradeService;
    private final PackageValidationService packageValidationService;
    private final PaymentTTLService paymentTTLService;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentEmailNotificationService emailNotificationService;
    private final WebhookService webhookService;
    private final InvoiceService invoiceService;
    private final DiscountService discountService;
    private final PaymentAuditService paymentAuditService;
    private final PaymentMetricsService paymentMetricsService;
    private final PaymentNotificationService paymentNotificationService;

    /**
     * Tạo yêu cầu nạp tiền mới
     */
    @Transactional("sharedTransactionManager")
    public DepositResponse createDeposit(DepositRequest request, Long userId, Long tenantId) {
        log.info("📱 Creating deposit request for user: {}, amount: {}, targetPackage: {}, discountCode: {}", 
                userId, request.getAmount(), request.getTargetPackageId(), request.getDiscountCode());

        // Validate package using PackageValidationService
        PackageValidationService.PackageValidationResult validationResult = null;
        Package targetPackage = null;
        
        if (request.getTargetPackageId() != null) {
            // Only validate package exists, skip balance validation for QR creation
            targetPackage = packageRepository.findByPackageId(request.getTargetPackageId())
                .orElseThrow(() -> new IllegalArgumentException("Package not found: " + request.getTargetPackageId()));
            
            if (!targetPackage.getIsActive()) {
                throw new IllegalArgumentException("Package is not active: " + request.getTargetPackageId());
            }
            
            log.info("   Package validated for QR creation: {} (balance check skipped)", targetPackage.getName());
        }

        // Apply discount code if provided
        java.math.BigDecimal finalAmount = request.getAmount();
        String appliedDiscountCode = null;
        
        if (request.getDiscountCode() != null && !request.getDiscountCode().isBlank()) {
            var discountResult = discountService.validateDiscount(
                request.getDiscountCode(), 
                request.getAmount(), 
                request.getTargetPackageId(), 
                userId
            );
            
            if (discountResult.isValid()) {
                finalAmount = discountResult.getFinalAmount();
                appliedDiscountCode = request.getDiscountCode();
                log.info("🎟️ Discount applied: {} - Original: {}, Discount: {}, Final: {}", 
                    request.getDiscountCode(), request.getAmount(), discountResult.getDiscountAmount(), finalAmount);
                
                // Mark discount as used
                discountService.useDiscount(request.getDiscountCode(), userId);
            } else {
                log.warn("⚠️ Discount code invalid: {} - {}", request.getDiscountCode(), discountResult.getMessage());
                // Continue without discount - don't fail the deposit
            }
        }

        // Generate unique reference code
        String referenceCode = generateReferenceCode();

        // Create payment record
        SimplePayment payment = SimplePayment.builder()
                .userId(userId)
                .tenantId(tenantId)
                .amount(finalAmount)
                .currency(request.getCurrency())
                .referenceCode(referenceCode)
                .status(PaymentStatus.PENDING)
                .description(request.getDescription() + (appliedDiscountCode != null ? " [DISCOUNT: " + appliedDiscountCode + "]" : ""))
                .targetPackageId(request.getTargetPackageId())
                .build();

        SimplePayment savedPayment = paymentRepository.save(payment);

        // Generate QR code
        String qrContent = qrCodeService.generateQRCode(
                savedPayment.getAmount(),
                savedPayment.getReferenceCode(),
                savedPayment.getDescription()
        );

        savedPayment.setQrContent(qrContent);
        paymentRepository.save(savedPayment);

        // Redis event publishing removed to avoid blocking during deposit creation
        log.debug("Redis event publishing skipped for deposit {}", referenceCode);

        // Publish event for event-driven processing
        eventPublisher.publishEvent(new PaymentEventService.PaymentCreatedEvent(
            referenceCode, savedPayment.getCreatedAt(), userId, tenantId
        ));

        // Set TTL for automatic expiration
        paymentTTLService.setPaymentTTL(referenceCode, savedPayment.getCreatedAt());

        // Log audit
        paymentAuditService.logPaymentAction(
            referenceCode,
            userId,
            tenantId,
            com.chatbot.core.simplepayment.model.PaymentAuditLog.AuditAction.PAYMENT_CREATED,
            null,
            "PENDING",
            finalAmount,
            "Deposit request created" + (appliedDiscountCode != null ? " with discount: " + appliedDiscountCode : ""),
            null
        );

        // Track metrics
        paymentMetricsService.incrementPaymentCreated();
        paymentMetricsService.recordPaymentAmount(finalAmount);

        log.info("✅ Deposit request created: {}", referenceCode);
        return DepositResponse.from(savedPayment, qrContent);
    }

    /**
     * Get current deposit limits for user/tenant (using PackageValidationService)
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public Map<String, Object> getCurrentDepositLimits(Long userId, Long tenantId) {
        return packageValidationService.getPackageUsageStats(userId, tenantId);
    }

    /**
     * Generate unique reference code for payment
     */
    private String generateReferenceCode() {
        return "PAY" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    /**
     * Kiểm tra trạng thái thanh toán
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public PaymentStatusResponse checkPaymentStatus(String referenceCode) {
        log.info("🔍 Checking payment status: {}", referenceCode);

        SimplePayment payment = paymentRepository.findByReferenceCode(referenceCode)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + referenceCode));

        PaymentStatusResponse response = new PaymentStatusResponse();
        response.setReferenceCode(payment.getReferenceCode());
        response.setStatus(payment.getStatus().name());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setDescription(payment.getDescription());
        response.setBankTransactionId(payment.getBankTransactionId());
        response.setTargetPackageId(payment.getTargetPackageId()); // Add targetPackageId
        response.setCreatedAt(payment.getCreatedAt());
        response.setCompletedAt(payment.getCompletedAt());
        response.setExpiresAt(payment.getExpiresAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        return response;
    }

    /**
     * Get payment by reference code (for other services to check status without circular dependency)
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public SimplePayment getPaymentByReference(String referenceCode) {
        return paymentRepository.findByReferenceCode(referenceCode)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + referenceCode));
    }

    /**
     * Hoàn thành thanh toán (khi nhìn transaction o bank)
     * Atomic transaction: payment + balance + package upgrade
     * Uses REQUIRES_NEW to ensure transaction context from Redis listener
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, transactionManager = "sharedTransactionManager")
    public void completePayment(String referenceCode, String bankTransactionId) {
        log.info("✅ Completing payment: {}", referenceCode);

        SimplePayment payment = paymentRepository.findByReferenceCode(referenceCode)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + referenceCode));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("Payment {} is not pending: {}", referenceCode, payment.getStatus());
            return;
        }

        try {
            // Update payment status
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setBankTransactionId(bankTransactionId);
            payment.setCompletedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            log.debug("📝 [DEBUG] Payment status updated to COMPLETED for: {}, targetPackage: {}",
                    referenceCode, payment.getTargetPackageId());

            // 1. Process package upgrade if a target package is requested (it handles balance crediting internally)
            if (payment.getTargetPackageId() != null && !payment.getTargetPackageId().trim().isEmpty()) {
                log.info(" [SimplePaymentService] Processing package upgrade for payment: {}, targetPackage: {}", 
                        referenceCode, payment.getTargetPackageId());
                
                boolean upgradeSuccess = TenantContext.executeWithTenantId(
                    payment.getTenantId(), 
                    () -> packageUpgradeService.processPackageUpgrade(payment)
                );
                
                log.debug("📝 [DEBUG] Package upgrade result for payment {}: {}", referenceCode, upgradeSuccess);
                
                if (!upgradeSuccess) {
                    throw new PaymentException("Package upgrade failed for payment: " + referenceCode);
                }
                
                log.info(" [SimplePaymentService] Package upgrade completed successfully for payment: {}", 
                        referenceCode);
            } else {
                // 2. Otherwise, this is a standard deposit, credit the user balance directly
                log.info(" [SimplePaymentService] Standard deposit. Crediting user balance for payment: {}", referenceCode);
                userBalanceService.updateUserBalanceInSeparateTransaction(payment.getUserId(), payment.getAmount());
            }

            // Publish Redis event for real-time notification
            PaymentEvent event = redisPaymentService.createStatusUpdateEvent(
                    referenceCode, PaymentStatus.COMPLETED, bankTransactionId
            );
            redisPaymentService.publishPaymentEvent(event);

            // Mark transaction as processed in mock bank api to avoid checking it again
            bankApiService.markTransactionAsProcessed(referenceCode);

            // Trigger email notification
            emailNotificationService.sendPaymentSuccessEmail(referenceCode);

            // Trigger webhook notification
            webhookService.triggerWebhook(com.chatbot.core.simplepayment.model.Webhook.WebhookEventType.PAYMENT_COMPLETED, payment);

            // Generate invoice
            try {
                invoiceService.generateInvoice(referenceCode);
            } catch (Exception e) {
                log.warn("⚠️ Failed to generate invoice for payment {}: {}", referenceCode, e.getMessage());
            }

            // Send package upgrade email if applicable
            if (payment.getTargetPackageId() != null && !payment.getTargetPackageId().trim().isEmpty()) {
                emailNotificationService.sendPackageUpgradeEmail(referenceCode, payment.getTargetPackageId());
                webhookService.triggerWebhook(com.chatbot.core.simplepayment.model.Webhook.WebhookEventType.PACKAGE_UPGRADED, payment);
                
                // Log audit for package upgrade
                paymentAuditService.logPaymentAction(
                    referenceCode,
                    payment.getUserId(),
                    payment.getTenantId(),
                    com.chatbot.core.simplepayment.model.PaymentAuditLog.AuditAction.PACKAGE_UPGRADED,
                    "COMPLETED",
                    "COMPLETED",
                    payment.getAmount(),
                    "Package upgraded: " + payment.getTargetPackageId(),
                    null
                );
            }

            // Log audit for payment completion
            paymentAuditService.logPaymentAction(
                referenceCode,
                payment.getUserId(),
                payment.getTenantId(),
                com.chatbot.core.simplepayment.model.PaymentAuditLog.AuditAction.PAYMENT_COMPLETED,
                "PENDING",
                "COMPLETED",
                payment.getAmount(),
                "Payment completed successfully",
                null
            );

            // Track metrics
            paymentMetricsService.incrementPaymentCompleted();
            paymentMetricsService.recordPaymentAmount(payment.getAmount());

            log.info("✅ Payment completed successfully: {}", referenceCode);
            
            // Trigger SSE live update if connection is active
            try {
                PaymentStatusResponse sseResponse = new PaymentStatusResponse();
                sseResponse.setReferenceCode(payment.getReferenceCode());
                sseResponse.setStatus(payment.getStatus().name());
                sseResponse.setAmount(payment.getAmount());
                sseResponse.setCurrency(payment.getCurrency());
                sseResponse.setDescription(payment.getDescription());
                sseResponse.setBankTransactionId(payment.getBankTransactionId());
                sseResponse.setTargetPackageId(payment.getTargetPackageId());
                sseResponse.setCreatedAt(payment.getCreatedAt());
                sseResponse.setCompletedAt(payment.getCompletedAt());
                sseResponse.setExpiresAt(payment.getExpiresAt());
                sseResponse.setUpdatedAt(payment.getUpdatedAt());
                sseResponse.withFormattedDates();
                
                paymentNotificationService.notifyPaymentSuccess(payment.getReferenceCode(), sseResponse);
            } catch (Exception e) {
                log.error("⚠️ Failed to send SSE notification: {}", e.getMessage());
            }
            
        } catch (Exception e) {
            log.error("❌ Payment completion failed for {}: {}", referenceCode, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Lấy danh sách thanh toán của user
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public List<PaymentStatusResponse> getUserPayments(Long userId, Long tenantId) {
        List<SimplePayment> payments = paymentRepository.findByUserIdAndTenantIdOrderByCreatedAtDesc(userId, tenantId);
        
        return payments.stream()
                .map(payment -> {
                    PaymentStatusResponse response = new PaymentStatusResponse();
                    response.setReferenceCode(payment.getReferenceCode());
                    response.setStatus(payment.getStatus().name());
                    response.setAmount(payment.getAmount());
                    response.setCurrency(payment.getCurrency());
                    response.setDescription(payment.getDescription());
                    response.setBankTransactionId(payment.getBankTransactionId());
                    response.setTargetPackageId(payment.getTargetPackageId()); // Add targetPackageId
                    response.setCreatedAt(payment.getCreatedAt());
                    response.setCompletedAt(payment.getCompletedAt());
                    response.setExpiresAt(payment.getExpiresAt());
                    response.setUpdatedAt(payment.getUpdatedAt());
                    return response;
                })
                .toList();
    }

    /**
     * Job để check các pending payments
     */
    @Transactional("sharedTransactionManager")
    public void checkPendingPayments() {
        log.info("🏦 Checking pending payments...");

        List<SimplePayment> pendingPayments = paymentRepository.findActivePendingPayments(LocalDateTime.now());

        for (SimplePayment payment : pendingPayments) {
            try {
                // Check with bank API
                String bankTransactionId = bankApiService.findTransactionByReference(payment.getReferenceCode());
                
                if (bankTransactionId != null) {
                    // Call the unified completePayment
                    completePayment(payment.getReferenceCode(), bankTransactionId);
                }
            } catch (Exception e) {
                log.error("❌ Error checking payment {}: {}", payment.getReferenceCode(), e.getMessage());
                // Continue with other payments - don't rollback entire transaction
            }
        }

        log.info("✅ Checked {} pending payments", pendingPayments.size());
    }

    /**
     * Expire old pending payments
     */
    @Transactional("sharedTransactionManager")
    public void expireOldPayments() {
        log.info("⏰ Expiring old pending payments...");

        List<SimplePayment> expiredPayments = paymentRepository.findByStatusAndExpiresAtBefore(
                PaymentStatus.PENDING, LocalDateTime.now()
        );

        for (SimplePayment payment : expiredPayments) {
            payment.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);

            // Publish Redis event for real-time notification
            PaymentEvent event = redisPaymentService.createStatusUpdateEvent(
                payment.getReferenceCode(), PaymentStatus.EXPIRED, null
            );
            redisPaymentService.publishPaymentEvent(event);

            // Trigger email notification
            emailNotificationService.sendPaymentExpiredEmail(payment.getReferenceCode());

            // Trigger webhook notification
            webhookService.triggerWebhook(com.chatbot.core.simplepayment.model.Webhook.WebhookEventType.PAYMENT_EXPIRED, payment);

            // Log audit
            paymentAuditService.logPaymentAction(
                payment.getReferenceCode(),
                payment.getUserId(),
                payment.getTenantId(),
                com.chatbot.core.simplepayment.model.PaymentAuditLog.AuditAction.PAYMENT_EXPIRED,
                "PENDING",
                "EXPIRED",
                payment.getAmount(),
                "Payment expired automatically",
                null
            );

            // Track metrics
            paymentMetricsService.incrementPaymentExpired();
        }

        log.info("✅ Expired {} pending payments", expiredPayments.size());
    }

    public void updateUserBalanceInSeparateTransaction(Long userId, BigDecimal amount) {
        userBalanceService.updateUserBalanceInSeparateTransaction(userId, amount);
    }

    /**
     * @deprecated Use updateUserBalanceInSeparateTransaction for proper transaction isolation.
     * This non-transactional version is kept only for compatibility; prefer the annotated variant.
     */
    @Deprecated
    public void updateUserBalance(Long userId, BigDecimal amount) {
        userBalanceService.updateUserBalanceInSeparateTransaction(userId, amount);
    }

    public void deductUserBalance(Long userId, BigDecimal amount) {
        userBalanceService.deductUserBalance(userId, amount);
    }

    public boolean hasSufficientBalance(Long userId, BigDecimal requiredAmount) {
        return userBalanceService.hasSufficientBalance(userId, requiredAmount);
    }
}
