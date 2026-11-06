package com.chatbot.core.training.repository;

import com.chatbot.core.training.model.CertificationLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificationLevelRepository extends JpaRepository<CertificationLevel, Long> {

    List<CertificationLevel> findByTenantId(Long tenantId);

    Optional<CertificationLevel> findByIdAndTenantId(Long id, Long tenantId);
}
