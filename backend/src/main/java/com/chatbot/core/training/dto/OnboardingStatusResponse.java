package com.chatbot.core.training.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OnboardingStatusResponse {
    private Boolean isComplete;
    private Double completionPercentage;
    private Long completedTasks;
    private Long totalRequiredTasks;
}
