package com.chatbot.core.training.dto;

import lombok.Data;

@Data
public class ManualSkillAssessmentRequest {
    private Long agentId;
    private String category;
    private Integer score;
    private String notes;
    private Long supervisorId;
}
