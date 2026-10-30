package com.chatbot.core.customer.repository;

import com.chatbot.core.customer.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByTenantId(Long tenantId);

    List<Feedback> findByCustomerProfileIdAndTenantId(Long customerProfileId, Long tenantId);

    List<Feedback> findByConversationIdAndTenantId(Long conversationId, Long tenantId);

    List<Feedback> findByTenantIdAndStatus(Long tenantId, Feedback.FeedbackStatus status);

    List<Feedback> findByTenantIdAndType(Long tenantId, Feedback.FeedbackType type);

    List<Feedback> findByTenantIdAndSubmittedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);

    List<Feedback> findByTenantIdOrderBySubmittedAtDesc(Long tenantId);
}
