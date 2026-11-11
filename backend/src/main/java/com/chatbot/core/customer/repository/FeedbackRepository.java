package com.chatbot.core.customer.repository;

import com.chatbot.core.customer.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    @Modifying
    @Transactional(value = "appTransactionManager", rollbackFor = Exception.class)
    @Query("DELETE FROM Feedback f WHERE f.tenantId = :tenantId")
    int deleteByTenantId(@Param("tenantId") Long tenantId);

    // Data retention: hard-delete soft-deleted feedback older than cutoff
    @Modifying
    @Transactional(value = "appTransactionManager", rollbackFor = Exception.class)
    @Query(value = "DELETE FROM feedback WHERE is_deleted = true AND updated_at < :cutoff", nativeQuery = true)
    int purgeSoftDeletedBefore(@Param("cutoff") java.time.LocalDateTime cutoff);
}
