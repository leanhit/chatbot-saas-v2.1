package com.chatbot.core.training.service;

import com.chatbot.core.training.model.AgentSkill;
import com.chatbot.core.training.repository.AgentSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillAssessmentService {

    private final AgentSkillRepository agentSkillRepository;

    public AgentSkill updateSkill(Long agentId, Integer score, Long tenantId) {
        AgentSkill skill = AgentSkill.builder()
            .agentId(agentId)
            .score(score)
            .build();
        return agentSkillRepository.save(skill);
    }

    public List<AgentSkill> getAgentSkills(Long agentId, Long tenantId) {
        return agentSkillRepository.findAll();
    }
}
