package com.chatbot.core.payment.transaction.service;

import com.chatbot.core.payment.common.audit.PaymentAuditLog.AuditAction;
import com.chatbot.core.payment.common.audit.PaymentAuditService;
import com.chatbot.core.payment.common.event.PaymentCompletedEvent;
import com.chatbot.core.payment.common.event.PaymentFailedEvent;
import com.chatbot.core.payment.common.metrics.PaymentMetricsService;
import com.chatbot.core.payment.transaction.dto.DepositRequest;
import com.chatbot.core.payment.transaction.dto.DepositResponse;
import com.chatbot.core.payment.transaction.dto.PaymentStatusResponse;
import com.chatbot.core.payment.transaction.exception.PaymentException;
import com.chatbot.core.payment.transaction.exception.PaymentNotFoundException;
import com.chatbot.core.payment.transaction.model.PaymentStatus;
import com.chatbot.core.payment.transaction.model.SimplePayment;
import com.chatbot.core.payment.transaction.repository.SimplePaymentRepository;
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
    private final PaymentAuditService paymentAuditService;
    private final PaymentMetricsService paymentMetricsService;
    private final ApplicationEventPublisher eventPublisher;

    // Dependencies to be injected after gateway migration
    // private final QRCodeService qrCodeService;
    // private final BankApiService bankApiService;
    // private final RedisPaymentService redisPaymentService;
    // private final PackageUpgradeService packageUpgradeService;
    // private final UserBalanceService userBalanceService;

    /**
     * Tạo yêu cầu nạp tiền mới
     * NOTE: This is a simplified version for migration. Full implementation will be added after gateway migration.
     */
    @Transactional("sharedTransactionManager")
    public DepositResponse createDeposit(DepositRequest request, Long userId, Long tenantId) {
        log.info("📱 Creating deposit request for user: {}, amount: {}, targetPackage: {}", 
                userId, request.getAmount(), request.getTargetPackageId());

        // Generate unique reference code
        String referenceCode = generateReferenceCode();

        // Create payment record
        SimplePayment payment = SimplePayment.builder()
                .userId(userId)
                .tenantId(tenantId)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .referenceCode(referenceCode)
                .status(PaymentStatus.PENDING)
                .description(request.getDescription())
                .targetPackageId(request.getTargetPackageId())
                .build();

        SimplePayment savedPayment = paymentRepository.save(payment);

        // QR code generation will be added after gateway migration
        // String qrContent = qrCodeService.generateQRCode(...);
        // savedPayment.setQrContent(qrContent);
        // paymentRepository.save(savedPayment);

        // Publish event for event-driven processing
        eventPublisher.publishEvent(new PaymentCompletedEvent(
            this, referenceCode, userId, tenantId, request.getAmount(), request.getCurrency(),
            null, request.getTargetPackageId(), savedPayment.getCreatedAt(), request.getDescription()
        ));

        // Log audit
        paymentAuditService.logPaymentAction(
            referenceCode,
            userId,
            tenantId,
            AuditAction.PAYMENT_CREATED,
            null,
            "PENDING",
            request.getAmount(),
            "Deposit request created",
            null
        );

        // Track metrics
        paymentMetricsService.incrementPaymentCreated();
        paymentMetricsService.recordPaymentAmount(request.getAmount());

        log.info("✅ Deposit request created: {}", referenceCode);
        return DepositResponse.from(savedPayment, null); // QR content null until gateway migration
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
        response.setTargetPackageId(payment.getTargetPackageId());
        response.setCreatedAt(payment.getCreatedAt());
        response.setCompletedAt(payment.getCompletedAt());
        response.setExpiresAt(payment.getExpiresAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        return response;
    }

    /**
     * Get payment by reference code
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public SimplePayment getPaymentByReference(String referenceCode) {
        return paymentRepository.findByReferenceCode(referenceCode)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + referenceCode));
    }

    /**
     * Hoàn thành thanh toán (Event-Driven version)
     * This method only updates the payment status and fires events
     * Downstream processing (package upgrade, balance credit, notifications) is handled by event listeners
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

        // Update payment status
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setBankTransactionId(bankTransactionId);
        payment.setCompletedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Publish PaymentCompletedEvent for downstream processing
        eventPublisher.publishEvent(new PaymentCompletedEvent(
            this,
            referenceCode,
            payment.getUserId(),
            payment.getTenantId(),
            payment.getAmount(),
            payment.getCurrency(),
            bankTransactionId,
            payment.getTargetPackageId(),
            payment.getCompletedAt(),
            payment.getDescription()
        ));

        // Log audit
        paymentAuditService.logPaymentAction(
            referenceCode,
            payment.getUserId(),
            payment.getTenantId(),
            AuditAction.PAYMENT_COMPLETED,
            "PENDING",
            "COMPLETED",
            payment.getAmount(),
            "Payment completed successfully",
            null
        );

        // Track metrics
        paymentMetricsService.incrementPaymentCompleted();

        log.info("✅ Payment completed successfully: {}", referenceCode);
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
                    response.setTargetPackageId(payment.getTargetPackageId());
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
                // Bank API check will be added after gateway migration
                // String bankTransactionId = bankApiService.findTransactionByReference(payment.getReferenceCode());
                // if (bankTransactionId != null) {
                //     completePayment(payment.getReferenceCode(), bankTransactionId);
                // }
            } catch (Exception e) {
                log.error("❌ Error checking payment {}: {}", payment.getReferenceCode(), e.getMessage());
            }
        }

        log.info("✅ Checked {} pending payments", pendingPayments.size());
    }

    /**
     * Get current deposit limits for user/tenant
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public Map<String, Object> getCurrentDepositLimits(Long userId, Long tenantId) {
        // Package validation will be added after plan migration
        return Map.of(
            "userId", userId,
            "tenantId", tenantId,
            "status", "MIGRATION_IN_PROGRESS",
            "message", "Package validation service will be migrated in plan subdomain"
        );
    }

    private String generateReferenceCode() {
        return "PAY" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
