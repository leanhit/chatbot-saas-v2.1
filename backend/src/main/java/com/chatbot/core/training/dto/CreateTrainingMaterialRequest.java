package com.chatbot.core.training.dto;

import lombok.Data;

@Data
public class CreateTrainingMaterialRequest {
    private String title;
    private String description;
    private String content;
    private String type;
    private Long categoryId;
    private Integer estimatedDurationMinutes;
    private Boolean isMandatory;
}
