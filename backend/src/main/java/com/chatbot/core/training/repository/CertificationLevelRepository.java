package com.chatbot.core.training.repository;

import com.chatbot.core.training.model.CertificationLevel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificationLevelRepository extends JpaRepository<CertificationLevel, Long> {
}
