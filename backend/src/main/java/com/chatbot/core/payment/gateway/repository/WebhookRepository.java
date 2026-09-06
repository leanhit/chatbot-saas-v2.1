package com.chatbot.core.payment.gateway.repository;

import com.chatbot.core.payment.gateway.model.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookRepository extends JpaRepository<Webhook, Long> {

    Optional<Webhook> findByUrl(String url);

    List<Webhook> findByIsActiveTrue();

    @Query("SELECT w FROM Webhook w WHERE w.isActive = true AND :eventType MEMBER OF w.eventTypes")
    List<Webhook> findActiveWebhooksForEvent(@Param("eventType") Webhook.WebhookEventType eventType);

    @Query("SELECT w FROM Webhook w WHERE w.status = 'FAILED' AND w.nextRetryAt IS NOT NULL AND w.nextRetryAt <= :now")
    List<Webhook> findWebhooksReadyForRetry(@Param("now") LocalDateTime now);

    @Query("SELECT w FROM Webhook w WHERE w.status = 'FAILED' AND w.currentRetryAttempt >= w.retryCount")
    List<Webhook> findFailedWebhooksForDeadLetter();

    boolean existsByUrl(String url);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional(value = "sharedTransactionManager", rollbackFor = Exception.class)
    void deleteByUrl(String url);
}
