package com.chatbot.core.training.repository;

import com.chatbot.core.training.model.AgentSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgentSkillRepository extends JpaRepository<AgentSkill, Long> {

    List<AgentSkill> findByAgentId(Long agentId);

    List<AgentSkill> findByAgentIdAndSkillCategory(Long agentId, AgentSkill.SkillCategory skillCategory);

    Optional<AgentSkill> findByAgentIdAndSkillCategoryAndTenantId(Long agentId, AgentSkill.SkillCategory skillCategory, Long tenantId);

    List<AgentSkill> findByTenantIdAndSkillCategory(Long tenantId, AgentSkill.SkillCategory skillCategory);

    List<AgentSkill> findByTenantIdAndSkillCategoryAndScoreGreaterThanEqual(Long tenantId, AgentSkill.SkillCategory skillCategory, Integer minScore);

    List<AgentSkill> findByTenantIdAndProficiencyLevel(Long tenantId, AgentSkill.ProficiencyLevel proficiencyLevel);
}
