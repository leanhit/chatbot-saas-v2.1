package com.chatbot.core.training.dto;

import com.chatbot.core.training.model.AgentTrainingProgress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentTrainingProgressResponse {
    private Long id;
    private Long agentId;
    private Long trainingId;
    private String status;
    private Integer progress;
    private Integer quizScore;
    private String startedAt;
    private String completedAt;
    private Integer timeSpentSeconds;
    private String notes;

    public static AgentTrainingProgressResponse from(AgentTrainingProgress p) {
        if (p == null) return null;
        return AgentTrainingProgressResponse.builder()
                .id(p.getId()).agentId(p.getAgentId()).trainingId(p.getTrainingId())
                .status(p.getStatus() != null ? p.getStatus().name() : null)
                .progress(p.getProgress()).quizScore(p.getQuizScore())
                .startedAt(p.getStartedAt()).completedAt(p.getCompletedAt())
                .timeSpentSeconds(p.getTimeSpentSeconds()).notes(p.getNotes())
                .build();
    }
}
