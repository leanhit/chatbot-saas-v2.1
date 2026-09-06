package com.chatbot.core.payment.merchant.repository;

import com.chatbot.core.payment.merchant.model.MerchantPaymentSession;
import com.chatbot.core.payment.merchant.model.MerchantPaymentSession.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantPaymentSessionRepository extends JpaRepository<MerchantPaymentSession, Long> {

    Optional<MerchantPaymentSession> findBySessionId(String sessionId);

    List<MerchantPaymentSession> findByMerchantIdOrderByCreatedAtDesc(Long merchantId);

    List<MerchantPaymentSession> findByMerchantOrderId(String merchantOrderId);

    @Query("SELECT m FROM MerchantPaymentSession m WHERE m.merchantId = :merchantId AND m.merchantOrderId = :merchantOrderId")
    Optional<MerchantPaymentSession> findByMerchantIdAndOrderId(@Param("merchantId") Long merchantId, @Param("merchantOrderId") String merchantOrderId);

    @Query("SELECT m FROM MerchantPaymentSession m WHERE m.status = :status AND m.expiresAt < :now")
    List<MerchantPaymentSession> findExpiredSessions(@Param("status") SessionStatus status, @Param("now") LocalDateTime now);

    @Query("SELECT m FROM MerchantPaymentSession m WHERE m.status = 'PENDING' AND m.webhookStatus = 'PENDING' AND m.expiresAt > :now")
    List<MerchantPaymentSession> findPendingWebhooks(@Param("now") LocalDateTime now);

    @Query("SELECT m FROM MerchantPaymentSession m WHERE m.webhookStatus = 'FAILED' AND m.webhookRetryCount < 5")
    List<MerchantPaymentSession> findFailedWebhooksForRetry();

    @Query("SELECT m FROM MerchantPaymentSession m WHERE m.paymentReferenceCode = :referenceCode")
    Optional<MerchantPaymentSession> findByPaymentReferenceCode(@Param("referenceCode") String referenceCode);

    boolean existsBySessionId(String sessionId);

    boolean existsByMerchantOrderId(String merchantOrderId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional(value = "sharedTransactionManager", rollbackFor = Exception.class)
    void deleteBySessionId(String sessionId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional(value = "sharedTransactionManager", rollbackFor = Exception.class)
    void deleteByMerchantId(Long merchantId);
}
