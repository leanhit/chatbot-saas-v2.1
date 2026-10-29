package com.chatbot.core.training.dto;

import lombok.Data;

@Data
public class AgentTrainingProgressDTO {
    private Long id;
    private Long agentId;
    private Long trainingId;
    private String status;
    private Integer progress;
    private Integer quizScore;
}
