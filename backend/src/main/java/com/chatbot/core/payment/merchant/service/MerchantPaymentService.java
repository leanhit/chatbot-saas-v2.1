package com.chatbot.core.payment.merchant.service;

import com.chatbot.core.payment.common.event.MerchantPaymentCompletedEvent;
import com.chatbot.core.payment.merchant.dto.MerchantPaymentRequest;
import com.chatbot.core.payment.merchant.dto.MerchantPaymentResponse;
import com.chatbot.core.payment.merchant.model.MerchantApiKey;
import com.chatbot.core.payment.merchant.model.MerchantPaymentSession;
import com.chatbot.core.payment.merchant.model.MerchantPaymentSession.SessionStatus;
import com.chatbot.core.payment.merchant.repository.MerchantApiKeyRepository;
import com.chatbot.core.payment.merchant.repository.MerchantPaymentSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantPaymentService {

    private final MerchantPaymentSessionRepository sessionRepository;
    private final MerchantApiKeyRepository apiKeyRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Create a new merchant payment session
     */
    @Transactional(transactionManager = "sharedTransactionManager")
    public MerchantPaymentResponse createPaymentSession(MerchantPaymentRequest request, Long merchantId) {
        log.info("🏪 Creating merchant payment session for merchant: {}, order: {}", merchantId, request.getMerchantOrderId());

        // Get merchant API key
        MerchantApiKey merchantKey = apiKeyRepository.findById(merchantId)
                .orElseThrow(() -> new RuntimeException("Merchant not found: " + merchantId));

        // Check if order ID already exists for this merchant
        if (sessionRepository.existsByMerchantOrderId(request.getMerchantOrderId())) {
            throw new RuntimeException("Order ID already exists: " + request.getMerchantOrderId());
        }

        // Create payment session
        MerchantPaymentSession session = MerchantPaymentSession.builder()
                .merchantId(merchantId)
                .tenantId(merchantKey.getTenantId())
                .merchantOrderId(request.getMerchantOrderId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .description(request.getDescription())
                .returnUrl(request.getReturnUrl())
                .cancelUrl(request.getCancelUrl())
                .metadata(request.getMetadata())
                .status(SessionStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        MerchantPaymentSession savedSession = sessionRepository.save(session);

        // Generate checkout URL for embeddable widget
        String checkoutUrl = generateCheckoutUrl(savedSession.getSessionId());

        log.info("✅ Merchant payment session created: {}", savedSession.getSessionId());

        return MerchantPaymentResponse.from(savedSession, checkoutUrl);
    }

    /**
     * Get payment session status
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public MerchantPaymentSession getSessionStatus(String sessionId) {
        log.info("🏪 Getting payment session status: {}", sessionId);

        MerchantPaymentSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        // Check if session is expired
        if (session.isExpired() && session.getStatus() == SessionStatus.PENDING) {
            session.setStatus(SessionStatus.EXPIRED);
            sessionRepository.save(session);
        }

        return session;
    }

    /**
     * Cancel payment session
     */
    @Transactional(transactionManager = "sharedTransactionManager")
    public MerchantPaymentSession cancelSession(String sessionId) {
        log.info("🏪 Cancelling payment session: {}", sessionId);

        MerchantPaymentSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        if (!session.canCancel()) {
            throw new RuntimeException("Session cannot be cancelled");
        }

        session.setStatus(SessionStatus.CANCELLED);
        session.setCancelledAt(LocalDateTime.now());

        return sessionRepository.save(session);
    }

    /**
     * Complete merchant payment session (called after internal payment is completed)
     */
    @Transactional(transactionManager = "sharedTransactionManager")
    public void completeMerchantPayment(String sessionId, String paymentReferenceCode, String bankTransactionId) {
        log.info("🏪 Completing merchant payment session: {}", sessionId);

        MerchantPaymentSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        if (!session.canComplete()) {
            throw new RuntimeException("Session cannot be completed");
        }

        session.setStatus(SessionStatus.COMPLETED);
        session.setPaymentReferenceCode(paymentReferenceCode);
        session.setBankTransactionId(bankTransactionId);
        session.setCompletedAt(LocalDateTime.now());

        MerchantPaymentSession savedSession = sessionRepository.save(session);

        // Publish event for webhook dispatch
        eventPublisher.publishEvent(new MerchantPaymentCompletedEvent(
            this,
            sessionId,
            session.getMerchantOrderId(),
            session.getMerchantId(),
            session.getAmount(),
            session.getCurrency(),
            bankTransactionId,
            session.getCompletedAt(),
            null // merchantWebhookUrl will be fetched from merchant key
        ));

        log.info("✅ Merchant payment completed: {}", sessionId);
    }

    /**
     * Generate checkout URL for embeddable widget
     */
    private String generateCheckoutUrl(String sessionId) {
        // This would be the URL for the embeddable widget
        // Format: https://your-domain.com/checkout/widget/{sessionId}
        return "/checkout/widget/" + sessionId;
    }

    /**
     * Get sessions for merchant
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public java.util.List<MerchantPaymentSession> getMerchantSessions(Long merchantId) {
        return sessionRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId);
    }
}
