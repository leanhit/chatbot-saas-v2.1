package com.chatbot.core.training.service;

import com.chatbot.core.training.model.AgentSkill;
import com.chatbot.core.training.model.AgentTrainingProgress;
import com.chatbot.core.training.repository.AgentSkillRepository;
import com.chatbot.core.training.repository.AgentTrainingProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillAssessmentService {

    private final AgentSkillRepository agentSkillRepository;
    private final AgentTrainingProgressRepository trainingProgressRepository;

    public AgentSkill updateSkill(Long agentId, Integer score, Long tenantId) {
        AgentSkill skill = AgentSkill.builder()
            .agentId(agentId)
            .score(score)
            .build();
        skill.setTenantId(tenantId);
        return agentSkillRepository.save(skill);
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
