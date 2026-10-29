package com.chatbot.core.training.repository;

import com.chatbot.core.training.model.AgentTrainingProgress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentTrainingProgressRepository extends JpaRepository<AgentTrainingProgress, Long> {
}
