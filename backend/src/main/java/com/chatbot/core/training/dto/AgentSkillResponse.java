package com.chatbot.core.training.dto;

import com.chatbot.core.training.model.AgentSkill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentSkillResponse {
    private Long id;
    private Long agentId;
    private String skillCategory;
    private Integer score;
    private String proficiencyLevel;
    private String lastAssessedAt;
    private String assessmentMethod;
    private Long trainingId;
    private Integer previousScore;
    private Integer assessmentCount;
    private String notes;

    public static AgentSkillResponse from(AgentSkill s) {
        if (s == null) return null;
        return AgentSkillResponse.builder()
                .id(s.getId()).agentId(s.getAgentId())
                .skillCategory(s.getSkillCategory() != null ? s.getSkillCategory().name() : null)
                .score(s.getScore())
                .proficiencyLevel(s.getProficiencyLevel() != null ? s.getProficiencyLevel().name() : null)
                .lastAssessedAt(s.getLastAssessedAt())
                .assessmentMethod(s.getAssessmentMethod() != null ? s.getAssessmentMethod().name() : null)
                .trainingId(s.getTrainingId()).previousScore(s.getPreviousScore())
                .assessmentCount(s.getAssessmentCount()).notes(s.getNotes())
                .build();
    }
}
