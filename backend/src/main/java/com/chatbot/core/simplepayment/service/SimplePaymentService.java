package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.dto.DepositRequest;
import com.chatbot.core.simplepayment.dto.DepositResponse;
import com.chatbot.core.simplepayment.dto.PaymentStatusResponse;
import com.chatbot.core.simplepayment.model.PaymentStatus;
import com.chatbot.core.simplepayment.model.SimplePayment;
import com.chatbot.core.simplepayment.repository.SimplePaymentRepository;
import com.chatbot.core.user.repository.UserRepository;
import com.chatbot.core.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimplePaymentService {

    private final SimplePaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final QRCodeService qrCodeService;
    private final BankApiService bankApiService;

    /**
     * Tạo yêu cầu nạp tiền mới
     */
    @Transactional
    public DepositResponse createDeposit(DepositRequest request, Long userId, Long tenantId) {
        log.info("📱 Creating deposit request for user: {}, amount: {}", userId, request.getAmount());

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

        log.info("✅ Deposit request created: {}", referenceCode);
        return DepositResponse.from(savedPayment, qrContent);
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
        response.setCreatedAt(payment.getCreatedAt());
        response.setCompletedAt(payment.getCompletedAt());
        response.setBankTransactionId(payment.getBankTransactionId());

        return response;
    }

    /**
     * Hoàn thành thanh toán (khi thấy transaction ở bank)
     */
    @Transactional
    public void completePayment(String referenceCode, String bankTransactionId) {
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

        log.info("✅ Payment completed successfully: {}", referenceCode);
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
                    response.setCreatedAt(payment.getCreatedAt());
                    response.setCompletedAt(payment.getCompletedAt());
                    response.setBankTransactionId(payment.getBankTransactionId());
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
                    completePayment(payment.getReferenceCode(), bankTransactionId);
                }
            } catch (Exception e) {
                log.error("❌ Error checking payment {}: {}", payment.getReferenceCode(), e.getMessage());
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
        }

        log.info("✅ Expired {} payments", expiredPayments.size());
    }

    private String generateReferenceCode() {
        String code;
        do {
            code = "NAP" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (paymentRepository.existsByReferenceCode(code));
        return code;
    }

    private void updateUserBalance(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Add balance column to user if not exists
        if (user.getBalance() == null) {
            user.setBalance(BigDecimal.ZERO);
        }

        user.setBalance(user.getBalance().add(amount));
        userRepository.save(user);

        log.info("💰 Updated user balance: {} + {} = {}", userId, amount, user.getBalance());
    }
}
