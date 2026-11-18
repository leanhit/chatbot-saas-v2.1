package com.chatbot.core.training.dto;

import com.chatbot.core.training.model.TrainingMaterial;
import lombok.Data;

import java.util.Set;

@Data
public class TrainingMaterialRequest {
    private String title;
    private String description;
    private String content;
    private TrainingMaterial.TrainingType type;
    private Long categoryId;
    private TrainingMaterial.TrainingStatus status;
    private Integer estimatedDurationMinutes;
    private Set<String> requiredSkills;
    private TrainingMaterial.DifficultyLevel difficultyLevel;
    private Boolean isMandatory;
    private Integer sequenceOrder;
    private Integer passScoreThreshold;
    private String quizQuestions;
    private String externalUrl;
    private String fileUrl;
}
