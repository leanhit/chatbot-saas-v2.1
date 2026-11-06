package com.chatbot.core.analytics.repository;

import com.chatbot.core.analytics.model.AnalyticsDailySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnalyticsDailySummaryRepository extends JpaRepository<AnalyticsDailySummary, Long> {

    Optional<AnalyticsDailySummary> findByTenantIdAndSummaryDateAndIsDeletedFalse(Long tenantId, LocalDate summaryDate);

    List<AnalyticsDailySummary> findByTenantIdAndSummaryDateBetweenAndIsDeletedFalseOrderBySummaryDateAsc(
        Long tenantId, LocalDate from, LocalDate to);

    @Query("SELECT s FROM AnalyticsDailySummary s WHERE s.tenantId = :tenantId " +
           "AND s.summaryDate BETWEEN :from AND :to AND s.isDeleted = false ORDER BY s.summaryDate ASC")
    List<AnalyticsDailySummary> findByTenantIdAndDateRange(
        @Param("tenantId") Long tenantId,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to);
}
