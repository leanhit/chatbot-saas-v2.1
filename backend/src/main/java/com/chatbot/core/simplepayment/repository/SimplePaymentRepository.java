package com.chatbot.core.simplepayment.repository;

import com.chatbot.core.simplepayment.model.PaymentStatus;
import com.chatbot.core.simplepayment.model.SimplePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SimplePaymentRepository extends JpaRepository<SimplePayment, Long> {

    Optional<SimplePayment> findByReferenceCode(String referenceCode);

    List<SimplePayment> findByUserIdAndTenantIdOrderByCreatedAtDesc(Long userId, Long tenantId);

    List<SimplePayment> findByStatusAndExpiresAtBefore(PaymentStatus status, LocalDateTime dateTime);

    @Query("SELECT p FROM SimplePayment p WHERE p.status = :status AND p.createdAt > :since")
    List<SimplePayment> findPendingPaymentsSince(@Param("status") PaymentStatus status, @Param("since") LocalDateTime since);

    @Query("SELECT p FROM SimplePayment p WHERE p.status = 'PENDING' AND p.expiresAt > :now")
    List<SimplePayment> findActivePendingPayments(@Param("now") LocalDateTime now);

    boolean existsByReferenceCode(String referenceCode);
}
