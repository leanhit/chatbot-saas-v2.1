package com.chatbot.core.training.dto;

import lombok.Data;

@Data
public class CertificationLevelRequest {
    private String levelName;
    private String description;
    private String requiredTrainingIds;
    private String requiredSkillScores;
    private String requiredStats;
    private String badgeIcon;
    private String badgeColor;
    private Integer displayOrder;
    private Boolean active;
    private Integer validityDays;
}
