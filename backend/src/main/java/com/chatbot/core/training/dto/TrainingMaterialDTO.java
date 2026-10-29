package com.chatbot.core.training.dto;

import lombok.Data;

@Data
public class TrainingMaterialDTO {
    private Long id;
    private String title;
    private String description;
    private String type;
    private Long categoryId;
    private String status;
    private Integer estimatedDurationMinutes;
}
