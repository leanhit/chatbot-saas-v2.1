package com.chatbot.core.training.repository;

import com.chatbot.core.training.model.AgentCertification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentCertificationRepository extends JpaRepository<AgentCertification, Long> {
}
