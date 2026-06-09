package com.chatbot.core.simplepayment.repository;

import com.chatbot.core.simplepayment.model.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface WebhookRepository extends JpaRepository<Webhook, Long> {
    
    Optional<Webhook> findByUrl(String url);
    
    List<Webhook> findByIsActiveTrueOrderByCreatedAtDesc();
    
    @Query("SELECT w FROM Webhook w WHERE w.isActive = true AND :eventType MEMBER OF w.eventTypes")
    List<Webhook> findActiveWebhooksForEvent(@Param("eventType") Webhook.WebhookEventType eventType);
    
    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM Webhook w WHERE w.url = :url")
    boolean existsByUrl(@Param("url") String url);
}
