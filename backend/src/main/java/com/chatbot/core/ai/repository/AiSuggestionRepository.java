package com.chatbot.core.ai.repository;

import com.chatbot.core.ai.model.AiSuggestion;
import com.chatbot.core.ai.model.AiSuggestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AiSuggestionRepository extends JpaRepository<AiSuggestion, Long> {

    List<AiSuggestion> findByTenantIdAndConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(
        Long tenantId, Long conversationId);

    List<AiSuggestion> findByTenantIdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(
        Long tenantId, AiSuggestionStatus status);

    Optional<AiSuggestion> findByIdAndTenantIdAndIsDeletedFalse(Long id, Long tenantId);

    @Query("SELECT s FROM AiSuggestion s WHERE s.tenantId = :tenantId AND s.status = :status " +
           "AND s.createdAt < :before AND s.isDeleted = false")
    List<AiSuggestion> findPendingOlderThan(
        @Param("tenantId") Long tenantId,
        @Param("status") AiSuggestionStatus status,
        @Param("before") LocalDateTime before);

    @Query("SELECT COUNT(s) FROM AiSuggestion s WHERE s.tenantId = :tenantId " +
           "AND s.status = :status AND s.createdAt BETWEEN :from AND :to AND s.isDeleted = false")
    long countByTenantIdAndStatusBetween(
        @Param("tenantId") Long tenantId,
        @Param("status") AiSuggestionStatus status,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(s) FROM AiSuggestion s WHERE s.tenantId = :tenantId " +
           "AND s.createdAt BETWEEN :from AND :to AND s.isDeleted = false")
    long countByTenantIdBetween(
        @Param("tenantId") Long tenantId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);
}
