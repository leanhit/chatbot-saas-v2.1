package com.chatbot.core.payment.gateway.repository;

import com.chatbot.core.payment.gateway.model.WebhookDeadLetter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WebhookDeadLetterRepository extends JpaRepository<WebhookDeadLetter, Long> {

    List<WebhookDeadLetter> findByStatus(String status);

    @Query("SELECT w FROM WebhookDeadLetter w WHERE w.status = 'PENDING' ORDER BY w.createdAt DESC")
    List<WebhookDeadLetter> findPendingDeadLetters();

    @Query("SELECT w FROM WebhookDeadLetter w WHERE w.paymentReferenceCode = :referenceCode ORDER BY w.createdAt DESC")
    List<WebhookDeadLetter> findByPaymentReferenceCode(@Param("referenceCode") String referenceCode);

    @Query("SELECT w FROM WebhookDeadLetter w WHERE w.createdAt < :before ORDER BY w.createdAt DESC")
    List<WebhookDeadLetter> findOldDeadLetters(@Param("before") LocalDateTime before);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional(value = "sharedTransactionManager", rollbackFor = Exception.class)
    void deleteByCreatedAtBefore(LocalDateTime before);
}
