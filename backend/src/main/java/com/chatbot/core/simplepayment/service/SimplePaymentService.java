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
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.user.repository.UserRepository;
import com.chatbot.core.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimplePaymentService {

    private final SimplePaymentRepository paymentRepository;
    private final PackageRepository packageRepository;
    private final UserRepository userRepository;
    private final QRCodeService qrCodeService;
    private final BankApiService bankApiService;
    private final RedisPaymentService redisPaymentService;
    private final PaymentPackageUpgradeService packageUpgradeService;
    private final PackageValidationService packageValidationService;
    private final PaymentEventService paymentEventService;
    private final PaymentTTLService paymentTTLService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Tạo yêu cầu nạp tiền mới
     */
    @Transactional
    public DepositResponse createDeposit(DepositRequest request, Long userId, Long tenantId) {
        log.info("📱 Creating deposit request for user: {}, amount: {}, targetPackage: {}", 
                userId, request.getAmount(), request.getTargetPackageId());

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

        // Generate QR code
        String qrContent = qrCodeService.generateQRCode(
                savedPayment.getAmount(),
                savedPayment.getReferenceCode(),
                savedPayment.getDescription()
        );

        savedPayment.setQrContent(qrContent);
        paymentRepository.save(savedPayment);

        // Publish Redis event for real-time notification
        PaymentEvent event = redisPaymentService.createPaymentEvent(
            referenceCode, userId, tenantId, 
            request.getAmount().toString(), request.getCurrency(), 
            request.getDescription()
        );
        redisPaymentService.publishPaymentEvent(event);

        // Publish event for event-driven processing
        eventPublisher.publishEvent(new PaymentEventService.PaymentCreatedEvent(
            referenceCode, savedPayment.getCreatedAt(), userId, tenantId
        ));

        // Set TTL for automatic expiration
        paymentTTLService.setPaymentTTL(referenceCode, savedPayment.getCreatedAt());

        log.info("✅ Deposit request created: {}", referenceCode);
        return DepositResponse.from(savedPayment, qrContent);
    }

    /**
     * Get current deposit limits for user/tenant (using PackageValidationService)
     */
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public PaymentStatusResponse checkPaymentStatus(String referenceCode) {
        log.info("🔍 Checking payment status: {}", referenceCode);

        SimplePayment payment = paymentRepository.findByReferenceCode(referenceCode)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + referenceCode));

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
     * Complete payment in same transaction for proper rollback support
     */
    @Transactional
    public void completePaymentInNewTransaction(String referenceCode, String bankTransactionId) {
        log.info("✅ Completing payment: {}", referenceCode);

        SimplePayment payment = paymentRepository.findByReferenceCode(referenceCode)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + referenceCode));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("Payment {} is not pending: {}", referenceCode, payment.getStatus());
            return;
        }

        // Update payment status
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setBankTransactionId(bankTransactionId);
        payment.setCompletedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Update user balance
        updateUserBalance(payment.getUserId(), payment.getAmount());

        // Publish Redis event for real-time notification
        PaymentEvent event = redisPaymentService.createStatusUpdateEvent(
                referenceCode, PaymentStatus.COMPLETED, bankTransactionId
        );
        redisPaymentService.publishPaymentEvent(event);

        log.info("✅ Payment completed successfully: {}", referenceCode);
    }

    /**
     * Hoàn thành thanh toán (khi nhìn transaction o bank)
     * Atomic transaction: payment + balance + package upgrade
     */
    @Transactional
    public void completePayment(String referenceCode, String bankTransactionId) {
        log.info(" Completing payment: {}", referenceCode);

        SimplePayment payment = paymentRepository.findByReferenceCode(referenceCode)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + referenceCode));

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

            // Update user balance
            updateUserBalance(payment.getUserId(), payment.getAmount());

            // Process automatic package upgrade in same transaction
            if (payment.getTargetPackageId() != null && !payment.getTargetPackageId().trim().isEmpty()) {
                log.info(" [SimplePaymentService] Processing package upgrade for payment: {}, targetPackage: {}", 
                        referenceCode, payment.getTargetPackageId());
                
                boolean upgradeSuccess = TenantContext.executeWithTenantId(
                    payment.getTenantId(), 
                    () -> packageUpgradeService.processPackageUpgrade(payment)
                );
                
                if (!upgradeSuccess) {
                    throw new RuntimeException("Package upgrade failed for payment: " + referenceCode);
                }
                
                log.info(" [SimplePaymentService] Package upgrade completed successfully for payment: {}", 
                        referenceCode);
            }

            // Publish Redis event for real-time notification
            PaymentEvent event = redisPaymentService.createStatusUpdateEvent(
                    referenceCode, PaymentStatus.COMPLETED, bankTransactionId
            );
            redisPaymentService.publishPaymentEvent(event);

            log.info(" Payment completed successfully: {}", referenceCode);
            
        } catch (Exception e) {
            log.error(" Payment completion failed for {}: {}", referenceCode, e.getMessage(), e);
            // Transaction will rollback automatically
            throw e;
        }
    }

    /**
     * Lấy danh sách thanh toán của user
     */
    @Transactional(readOnly = true)
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
    @Transactional
    public void checkPendingPayments() {
        log.info("🏦 Checking pending payments...");

        List<SimplePayment> pendingPayments = paymentRepository.findActivePendingPayments(LocalDateTime.now());

        for (SimplePayment payment : pendingPayments) {
            try {
                // Check with bank API
                String bankTransactionId = bankApiService.findTransactionByReference(payment.getReferenceCode());
                
                if (bankTransactionId != null) {
                    // Use new transaction for each payment completion
                    completePaymentInNewTransaction(payment.getReferenceCode(), bankTransactionId);
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
    @Transactional
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
        }

        log.info("✅ Expired {} pending payments", expiredPayments.size());
    }

    private void updateUserBalance(Long userId, BigDecimal amount) {
        User user = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Initialize balance if null
        if (user.getBalance() == null) {
            user.setBalance(BigDecimal.ZERO);
        }

        user.setBalance(user.getBalance().add(amount));
        userRepository.save(user);

        log.info("💸 Updated user balance: {} + {} = {}", userId, amount, user.getBalance());
    }

    /**
     * Deduct balance from user when purchasing package
     */
    public void deductUserBalance(Long userId, BigDecimal amount) {
        User user = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Initialize balance if null
        if (user.getBalance() == null) {
            user.setBalance(BigDecimal.ZERO);
        }

        // Check sufficient balance
        if (user.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException(
                String.format("Insufficient balance. Required: %s, Available: %s", 
                    amount, user.getBalance())
            );
        }

        BigDecimal oldBalance = user.getBalance();
        user.setBalance(user.getBalance().subtract(amount));
        userRepository.save(user);

        log.info("💸 Deducted user balance: {} - {} = {}", userId, amount, user.getBalance());
        log.info("💰 Balance change for user {}: {} → {}", userId, oldBalance, user.getBalance());
    }

    /**
     * Check if user has sufficient balance for package purchase
     */
    public boolean hasSufficientBalance(Long userId, BigDecimal requiredAmount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (user.getBalance() == null) {
            user.setBalance(BigDecimal.ZERO);
            userRepository.save(user);
        }

        boolean sufficient = user.getBalance().compareTo(requiredAmount) >= 0;
        log.info("🔍 Balance check for user {}: required={}, available={}, sufficient={}", 
                userId, requiredAmount, user.getBalance(), sufficient);
        
        return sufficient;
    }
}
