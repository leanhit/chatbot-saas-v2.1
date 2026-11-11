package com.chatbot.core.training.service;

import com.chatbot.core.message.store.service.AgentAssignmentService;
import com.chatbot.core.training.model.AgentSkill;
import com.chatbot.core.training.model.AgentTrainingProgress;
import com.chatbot.core.training.repository.AgentSkillRepository;
import com.chatbot.core.training.repository.AgentTrainingProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillAssessmentService {

    private final AgentSkillRepository agentSkillRepository;
    private final AgentTrainingProgressRepository trainingProgressRepository;
    private final AgentAssignmentService agentAssignmentService;

    public AgentSkill updateSkill(Long agentId, String category, Integer score, Long tenantId) {
        AgentSkill.SkillCategory skillCategory = category != null
            ? AgentSkill.SkillCategory.valueOf(category.toUpperCase())
            : null;
        AgentSkill skill;
        if (skillCategory != null) {
            skill = agentSkillRepository
                .findByAgentIdAndSkillCategoryAndTenantId(agentId, skillCategory, tenantId)
                .orElse(AgentSkill.builder()
                    .agentId(agentId)
                    .skillCategory(skillCategory)
                    .score(0)
                    .proficiencyLevel(AgentSkill.ProficiencyLevel.BEGINNER)
                    .build());
        } else {
            skill = AgentSkill.builder()
                .agentId(agentId)
                .score(0)
                .proficiencyLevel(AgentSkill.ProficiencyLevel.BEGINNER)
                .build();
        }
        skill.setTenantId(tenantId);
        skill.setScore(score);
        skill.setProficiencyLevel(proficiencyFor(score));
        AgentSkill saved = agentSkillRepository.save(skill);
        syncSkillToAgentRouting(agentId, skillCategory, score);
        return saved;
    }

    /**
     * Bridge training skills → routing: when an agent reaches INTERMEDIATE+
     * (score >= 60) in a SkillCategory, expose it on Agent.skills so
     * AgentAssignmentService can match it against requiredSkills.
     * Skill string = category name lowercase (e.g. "technical", "billing").
     * Add-only: never removes manually assigned skills.
     */
    private void syncSkillToAgentRouting(Long agentId, AgentSkill.SkillCategory category, Integer score) {
        if (category == null || score == null || score < 60) {
            return;
        }
        try {
            agentAssignmentService.addSkillToAgent(agentId, category.name().toLowerCase());
        } catch (Exception e) {
            log.warn("[SkillBridge] Failed to sync skill {} to agent {}: {}", category, agentId, e.getMessage());
        }
    }

    private AgentSkill.ProficiencyLevel proficiencyFor(int score) {
        // Same thresholds as TrainingService.updateSkillScore
        if (score >= 80) return AgentSkill.ProficiencyLevel.EXPERT;
        if (score >= 60) return AgentSkill.ProficiencyLevel.INTERMEDIATE;
        return AgentSkill.ProficiencyLevel.BEGINNER;
    }

    public List<AgentSkill> getAgentSkills(Long agentId, Long tenantId) {
        return agentSkillRepository.findByAgentIdAndTenantId(agentId, tenantId);
    }

    public List<AgentSkill> getAgentSkills(Long agentId) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        return agentSkillRepository.findByAgentIdAndTenantId(agentId, tenantId);
    }

    public List<AgentTrainingProgress> getAgentTrainingProgress(Long agentId) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        return trainingProgressRepository.findByAgentIdAndTenantId(agentId, tenantId);
    }

    public AgentTrainingProgress updateTrainingProgress(AgentTrainingProgress progress) {
        return trainingProgressRepository.save(progress);
    }

    public AgentSkill updateAgentSkill(AgentSkill skill) {
        return agentSkillRepository.save(skill);
    }
}
