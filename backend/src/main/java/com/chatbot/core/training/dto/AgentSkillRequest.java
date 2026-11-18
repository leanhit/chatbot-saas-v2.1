package com.chatbot.core.training.dto;

import com.chatbot.core.training.model.AgentSkill;
import lombok.Data;

@Data
public class AgentSkillRequest {
    private AgentSkill.SkillCategory skillCategory;
    private Integer score;
    private AgentSkill.ProficiencyLevel proficiencyLevel;
    private String lastAssessedAt;
    private AgentSkill.AssessmentMethod assessmentMethod;
    private Long trainingId;
    private Integer previousScore;
    private Integer assessmentCount;
    private String notes;
}
