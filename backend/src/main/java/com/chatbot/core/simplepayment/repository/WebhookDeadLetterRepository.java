package com.chatbot.core.simplepayment.repository;

import com.chatbot.core.simplepayment.model.WebhookDeadLetter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for WebhookDeadLetter entity.
 */
@Repository
public interface WebhookDeadLetterRepository extends JpaRepository<WebhookDeadLetter, Long> {
    
    List<WebhookDeadLetter> findByStatusOrderByCreatedAtDesc(String status);
    
    List<WebhookDeadLetter> findByPaymentReferenceCode(String referenceCode);
}
