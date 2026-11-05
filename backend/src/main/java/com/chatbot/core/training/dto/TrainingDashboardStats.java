package com.chatbot.core.training.dto;

import lombok.Data;

@Data
public class TrainingDashboardStats {
    private Long totalAgents;
    private Long activeTraining;
    private Long completedTraining;
    private Long certifiedAgents;
}
