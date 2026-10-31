package com.chatbot.core.routing.dto;

import com.chatbot.core.routing.model.SupervisorSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupervisorSessionDTO {
    private Long id;
    private Long supervisorId;
    private Long conversationId;
    private SupervisorSession.SessionType sessionType;
    private SupervisorSession.SessionStatus sessionStatus;
    private Long monitoredAgentId;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private Integer durationSeconds;
    private Boolean isSilent;
    private Boolean canIntervene;
    private Integer interventionCount;
    private String notes;
    private Integer rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
