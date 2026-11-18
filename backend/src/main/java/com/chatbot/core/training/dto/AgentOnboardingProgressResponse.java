package com.chatbot.core.training.dto;

import com.chatbot.core.training.model.AgentOnboardingProgress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentOnboardingProgressResponse {
    private Long id;
    private Long agentId;
    private Long taskId;
    private String status;
    private String startedAt;
    private String completedAt;
    private Integer timeSpentSeconds;
    private String notes;
    private String supervisorApproval;
    private Long supervisorId;
    private String supervisorComments;
    private String approvedAt;

    public static AgentOnboardingProgressResponse from(AgentOnboardingProgress p) {
        if (p == null) return null;
        return AgentOnboardingProgressResponse.builder()
                .id(p.getId()).agentId(p.getAgentId()).taskId(p.getTaskId())
                .status(p.getStatus() != null ? p.getStatus().name() : null)
                .startedAt(p.getStartedAt()).completedAt(p.getCompletedAt())
                .timeSpentSeconds(p.getTimeSpentSeconds()).notes(p.getNotes())
                .supervisorApproval(p.getSupervisorApproval() != null ? p.getSupervisorApproval().name() : null)
                .supervisorId(p.getSupervisorId()).supervisorComments(p.getSupervisorComments())
                .approvedAt(p.getApprovedAt())
                .build();
    }
}
