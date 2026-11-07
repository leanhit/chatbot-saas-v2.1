package com.chatbot.core.analytics.repository;

import com.chatbot.core.analytics.model.CustomReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomReportRepository extends JpaRepository<CustomReport, Long> {

    List<CustomReport> findByTenantIdAndIsDeletedFalse(Long tenantId);

    Optional<CustomReport> findByIdAndTenantIdAndIsDeletedFalse(Long id, Long tenantId);
}
