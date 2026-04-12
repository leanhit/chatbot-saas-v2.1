package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.dto.PaymentEvent;
import com.chatbot.core.simplepayment.model.PaymentStatus;
import com.chatbot.core.simplepayment.model.SimplePayment;
import com.chatbot.core.simplepayment.repository.SimplePaymentRepository;
import com.chatbot.core.simplepayment.service.BankApiService;
import com.chatbot.core.simplepayment.service.PaymentPackageUpgradeService;
import com.chatbot.core.simplepayment.service.RedisPaymentService;
import com.chatbot.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventService {

    private final BankApiService bankApiService;
    private final RedisPaymentService redisPaymentService;
    private final SimplePaymentRepository paymentRepository;
    private final PaymentPackageUpgradeService packageUpgradeService;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Event khi payment mới được tạo
     */
    @EventListener
    @Async
    public void handlePaymentCreated(PaymentCreatedEvent event) {
        log.info("🎯 Payment created event: {}", event.getReferenceCode());
        
        // Schedule individual payment check
        schedulePaymentCheck(event.getReferenceCode(), event.getCreatedAt());
    }

    /**
     * Event-driven payment check
     */
    private void schedulePaymentCheck(String referenceCode, LocalDateTime createdAt) {
        // Check ngay lập tức
        checkSinglePayment(referenceCode);
        
        // Schedule retry với exponential backoff
        scheduleRetryWithBackoff(referenceCode, createdAt);
    }

    /**
     * Check individual payment
     */
    private void checkSinglePayment(String referenceCode) {
        try {
            String bankTransactionId = bankApiService.findTransactionByReference(referenceCode);
            
            if (bankTransactionId != null) {
                // Complete payment directly without circular dependency
                completePaymentDirectly(referenceCode, bankTransactionId);
            }
        } catch (Exception e) {
            log.error("❌ Error checking payment {}: {}", referenceCode, e.getMessage());
        }
    }

    /**
     * Complete payment directly without circular dependency
     */
    @Transactional
    private void completePaymentDirectly(String referenceCode, String bankTransactionId) {
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
        updateUserBalanceDirectly(payment.getUserId(), payment.getAmount());

        // Process package upgrade if needed
        if (payment.getTargetPackageId() != null) {
            processPackageUpgradeDirectly(payment.getUserId(), payment.getTenantId(), 
                    payment.getTargetPackageId(), referenceCode);
        }

        // Publish completion event
        PaymentEvent event = redisPaymentService.createStatusUpdateEvent(
                referenceCode, PaymentStatus.COMPLETED, bankTransactionId
        );
        redisPaymentService.publishPaymentEvent(event);

        log.info("✅ Payment completed via event: {}", referenceCode);
    }

    /**
     * Update user balance directly
     */
    private void updateUserBalanceDirectly(Long userId, BigDecimal amount) {
        userRepository.findById(userId).ifPresent(user -> {
            if (user.getBalance() == null) {
                user.setBalance(BigDecimal.ZERO);
            }
            user.setBalance(user.getBalance().add(amount));
            userRepository.save(user);
            log.info("💸 Updated user balance: {} + {} = {}", userId, amount, user.getBalance());
        });
    }

    /**
     * Process package upgrade directly (for testing)
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void processPackageUpgradeDirectly(Long userId, Long tenantId, String packageId, String referenceCode) {
        log.info("🔄 Processing package upgrade directly for user: {}, tenant: {}, package: {}", userId, tenantId, packageId);
        
        try {
            // Create a mock SimplePayment object for the upgrade service
            SimplePayment mockPayment = SimplePayment.builder()
                    .userId(userId)
                    .tenantId(tenantId)
                    .referenceCode(referenceCode)
                    .build();
                    
            packageUpgradeService.executeUpgradeWithBalanceDeduction(mockPayment, packageId);
            log.info("📦 Package upgraded via event: {} for user: {}", packageId, userId);
        } catch (Exception e) {
            log.error("❌ Failed to upgrade package {}: {}", packageId, e.getMessage());
        }
    }

    /**
     * Exponential backoff retry
     */
    private void scheduleRetryWithBackoff(String referenceCode, LocalDateTime createdAt) {
        // Retry sau: 30s, 2m, 5m, 15m, 30m
        int[] retryIntervals = {30, 120, 300, 900, 1800};
        
        for (int i = 0; i < retryIntervals.length; i++) {
            // Schedule retry với delay
            scheduleRetry(referenceCode, retryIntervals[i], i + 1);
        }
    }

    /**
     * Schedule single retry
     */
    @Async
    private void scheduleRetry(String referenceCode, int delaySeconds, int attempt) {
        try {
            Thread.sleep(delaySeconds * 1000L);
            checkSinglePayment(referenceCode);
            log.debug("🔄 Retry attempt {} for payment {}", attempt, referenceCode);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Payment created event
     */
    public static class PaymentCreatedEvent {
        private final String referenceCode;
        private final LocalDateTime createdAt;
        private final Long userId;
        private final Long tenantId;

        public PaymentCreatedEvent(String referenceCode, LocalDateTime createdAt, Long userId, Long tenantId) {
            this.referenceCode = referenceCode;
            this.createdAt = createdAt;
            this.userId = userId;
            this.tenantId = tenantId;
        }

        // Getters
        public String getReferenceCode() { return referenceCode; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public Long getUserId() { return userId; }
        public Long getTenantId() { return tenantId; }
    }
}
