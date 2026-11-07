package com.chatbot.core.analytics.repository;

import com.chatbot.core.analytics.model.ReportSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportScheduleRepository extends JpaRepository<ReportSchedule, Long> {

    List<ReportSchedule> findByTenantIdAndIsDeletedFalse(Long tenantId);

    Optional<ReportSchedule> findByIdAndTenantIdAndIsDeletedFalse(Long id, Long tenantId);

    List<ReportSchedule> findByEnabledTrueAndNextRunAtBeforeAndIsDeletedFalse(LocalDateTime now);
}
