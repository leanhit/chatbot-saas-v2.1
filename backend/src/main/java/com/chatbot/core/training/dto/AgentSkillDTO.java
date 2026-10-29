package com.chatbot.core.training.dto;

import lombok.Data;

@Data
public class AgentSkillDTO {
    private Long id;
    private Long agentId;
    private String skillCategory;
    private Integer score;
    private String proficiencyLevel;
}
