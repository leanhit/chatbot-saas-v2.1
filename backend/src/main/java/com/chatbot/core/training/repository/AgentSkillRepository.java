package com.chatbot.core.training.repository;

import com.chatbot.core.training.model.AgentSkill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentSkillRepository extends JpaRepository<AgentSkill, Long> {
}
