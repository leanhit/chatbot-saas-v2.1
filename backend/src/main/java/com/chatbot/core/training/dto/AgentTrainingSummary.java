package com.chatbot.core.training.dto;

import lombok.Data;

@Data
public class AgentTrainingSummary {
    private Long agentId;
    private String agentName;
    private String agentEmail;
    private Integer onboardingProgress;
    private Integer trainingProgress;
    private Integer averageScore;
    private String certification;
    private String trainingStatus;
}
