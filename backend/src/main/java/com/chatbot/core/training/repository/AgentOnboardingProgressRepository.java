package com.chatbot.core.training.repository;

import com.chatbot.core.training.model.AgentOnboardingProgress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentOnboardingProgressRepository extends JpaRepository<AgentOnboardingProgress, Long> {
}
