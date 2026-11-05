package com.chatbot.core.training.repository;

import com.chatbot.core.training.model.CertificationLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CertificationLevelRepository extends JpaRepository<CertificationLevel, Long> {

    List<CertificationLevel> findByTenantId(Long tenantId);
}
