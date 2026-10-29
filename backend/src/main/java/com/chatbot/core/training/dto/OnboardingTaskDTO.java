package com.chatbot.core.training.dto;

import lombok.Data;

@Data
public class OnboardingTaskDTO {
    private Long id;
    private String title;
    private String description;
    private String type;
    private Boolean required;
    private String status;
}
