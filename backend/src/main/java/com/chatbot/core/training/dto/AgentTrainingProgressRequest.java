package com.chatbot.core.training.dto;

import com.chatbot.core.training.model.AgentTrainingProgress;
import lombok.Data;

@Data
public class AgentTrainingProgressRequest {
    private AgentTrainingProgress.ProgressStatus status;
    private Integer progress;
    private Integer quizScore;
    private String startedAt;
    private String completedAt;
    private Integer timeSpentSeconds;
    private String notes;
}
