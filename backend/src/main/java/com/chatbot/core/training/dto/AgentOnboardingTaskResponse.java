package com.chatbot.core.training.dto;

import com.chatbot.core.training.model.AgentOnboardingTask;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentOnboardingTaskResponse {
    private Long id;
    private String title;
    private String description;
    private String type;
    private Long trainingId;
    private Boolean required;
    private Integer displayOrder;
    private String status;
    private Integer estimatedDurationMinutes;
    private String instructions;
    private String externalUrl;

    public static AgentOnboardingTaskResponse from(AgentOnboardingTask t) {
        if (t == null) return null;
        return AgentOnboardingTaskResponse.builder()
                .id(t.getId()).title(t.getTitle()).description(t.getDescription())
                .type(t.getType() != null ? t.getType().name() : null)
                .trainingId(t.getTrainingId()).required(t.getRequired())
                .displayOrder(t.getDisplayOrder())
                .status(t.getStatus() != null ? t.getStatus().name() : null)
                .estimatedDurationMinutes(t.getEstimatedDurationMinutes())
                .instructions(t.getInstructions()).externalUrl(t.getExternalUrl())
                .build();
    }
}
