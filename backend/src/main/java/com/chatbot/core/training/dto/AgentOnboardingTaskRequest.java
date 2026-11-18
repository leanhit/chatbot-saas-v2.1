package com.chatbot.core.training.dto;

import com.chatbot.core.training.model.AgentOnboardingTask;
import lombok.Data;

@Data
public class AgentOnboardingTaskRequest {
    private String title;
    private String description;
    private AgentOnboardingTask.TaskType type;
    private Long trainingId;
    private Boolean required;
    private Integer displayOrder;
    private AgentOnboardingTask.TaskStatus status;
    private Integer estimatedDurationMinutes;
    private String instructions;
    private String externalUrl;
}
