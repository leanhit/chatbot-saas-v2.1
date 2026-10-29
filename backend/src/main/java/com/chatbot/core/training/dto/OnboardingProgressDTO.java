package com.chatbot.core.training.dto;

import lombok.Data;

@Data
public class OnboardingProgressDTO {
    private Long id;
    private Long agentId;
    private Long taskId;
    private String status;
    private String startedAt;
    private String completedAt;
}
