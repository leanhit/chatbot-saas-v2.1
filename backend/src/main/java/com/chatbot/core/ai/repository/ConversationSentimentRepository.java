package com.chatbot.core.ai.repository;

import com.chatbot.core.ai.model.ConversationSentiment;
import com.chatbot.core.ai.model.SentimentLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationSentimentRepository extends JpaRepository<ConversationSentiment, Long> {

    Optional<ConversationSentiment> findTopByTenantIdAndConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(
        Long tenantId, Long conversationId);

    List<ConversationSentiment> findByTenantIdAndConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(
        Long tenantId, Long conversationId);

    @Query("SELECT AVG(s.score) FROM ConversationSentiment s WHERE s.tenantId = :tenantId " +
           "AND s.createdAt BETWEEN :from AND :to AND s.isDeleted = false")
    Double avgScoreByTenantIdBetween(
        @Param("tenantId") Long tenantId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(s) FROM ConversationSentiment s WHERE s.tenantId = :tenantId " +
           "AND s.label = :label AND s.createdAt BETWEEN :from AND :to AND s.isDeleted = false")
    long countByTenantIdAndLabelBetween(
        @Param("tenantId") Long tenantId,
        @Param("label") SentimentLabel label,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);

    // Data retention: hard-delete soft-deleted sentiment rows older than cutoff
    @Modifying
    @Transactional(value = "messageTransactionManager", rollbackFor = Exception.class)
    @Query(value = "DELETE FROM conversation_sentiment WHERE is_deleted = true AND updated_at < :cutoff", nativeQuery = true)
    int purgeSoftDeletedBefore(@Param("cutoff") LocalDateTime cutoff);
}
