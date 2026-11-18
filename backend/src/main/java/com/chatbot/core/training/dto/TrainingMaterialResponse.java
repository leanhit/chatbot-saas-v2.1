package com.chatbot.core.training.dto;

import com.chatbot.core.training.model.TrainingMaterial;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingMaterialResponse {
    private Long id;
    private String title;
    private String description;
    private String content;
    private String type;
    private Long categoryId;
    private String status;
    private Integer estimatedDurationMinutes;
    private Set<String> requiredSkills;
    private String difficultyLevel;
    private Boolean isMandatory;
    private Integer sequenceOrder;
    private Integer passScoreThreshold;
    private String quizQuestions;
    private String externalUrl;
    private String fileUrl;

    public static TrainingMaterialResponse from(TrainingMaterial m) {
        if (m == null) return null;
        return TrainingMaterialResponse.builder()
                .id(m.getId()).title(m.getTitle()).description(m.getDescription())
                .content(m.getContent())
                .type(m.getType() != null ? m.getType().name() : null)
                .categoryId(m.getCategoryId())
                .status(m.getStatus() != null ? m.getStatus().name() : null)
                .estimatedDurationMinutes(m.getEstimatedDurationMinutes())
                .requiredSkills(m.getRequiredSkills())
                .difficultyLevel(m.getDifficultyLevel() != null ? m.getDifficultyLevel().name() : null)
                .isMandatory(m.getIsMandatory())
                .sequenceOrder(m.getSequenceOrder())
                .passScoreThreshold(m.getPassScoreThreshold())
                .quizQuestions(m.getQuizQuestions())
                .externalUrl(m.getExternalUrl()).fileUrl(m.getFileUrl())
                .build();
    }
}
