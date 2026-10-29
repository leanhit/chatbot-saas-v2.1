package com.chatbot.core.training.repository;

import com.chatbot.core.training.model.AgentOnboardingTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentOnboardingTaskRepository extends JpaRepository<AgentOnboardingTask, Long> {
}
