package com.chatbot.core.simplepayment.repository;

import com.chatbot.core.simplepayment.model.PaymentStatus;
import com.chatbot.core.simplepayment.model.SimplePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    // Methods for free package limit validation
    @Query("SELECT COUNT(p) FROM SimplePayment p WHERE p.tenantId = :tenantId AND p.createdAt >= :monthStart AND p.status = :status")
    Long countByTenantIdAndCreatedAtAfterAndStatus(@Param("tenantId") Long tenantId, 
                                                   @Param("monthStart") LocalDateTime monthStart, 
                                                   @Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM SimplePayment p WHERE p.tenantId = :tenantId AND p.createdAt >= :monthStart AND p.status = :status")
    BigDecimal sumAmountByTenantIdAndCreatedAtAfterAndStatus(@Param("tenantId") Long tenantId, 
                                                           @Param("monthStart") LocalDateTime monthStart, 
                                                           @Param("status") PaymentStatus status);

    // Methods for package validation
    @Query("SELECT COUNT(p) > 0 FROM SimplePayment p WHERE p.userId = :userId AND p.tenantId = :tenantId AND p.targetPackageId = :packageId AND p.createdAt >= :since AND p.status = :status")
    boolean existsByUserIdAndTenantIdAndTargetPackageIdAndCreatedAtAfterAndStatus(@Param("userId") Long userId,
                                                                                  @Param("tenantId") Long tenantId,
                                                                                  @Param("packageId") String packageId,
                                                                                  @Param("since") LocalDateTime since,
                                                                                  @Param("status") PaymentStatus status);

    @Query("SELECT COUNT(p) FROM SimplePayment p WHERE p.tenantId = :tenantId AND p.status = :status")
    Long countByTenantIdAndStatus(@Param("tenantId") Long tenantId, @Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM SimplePayment p WHERE p.tenantId = :tenantId AND p.status = :status")
    BigDecimal sumAmountByTenantIdAndStatus(@Param("tenantId") Long tenantId, @Param("status") PaymentStatus status);

    // Methods for payment analytics
    List<SimplePayment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<SimplePayment> findByStatusAndCreatedAtAfter(PaymentStatus status, LocalDateTime since);

    @Query("SELECT COUNT(p) FROM SimplePayment p WHERE p.status = :status")
    Long countByStatus(@Param("status") PaymentStatus status);
}
