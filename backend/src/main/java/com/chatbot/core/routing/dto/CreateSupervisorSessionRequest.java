package com.chatbot.core.routing.dto;

import com.chatbot.core.routing.model.SupervisorSession;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSupervisorSessionRequest {
    @NotNull(message = "Supervisor ID is required")
    private Long supervisorId;

    @NotNull(message = "Conversation ID is required")
    private Long conversationId;

    @NotNull(message = "Session type is required")
    private SupervisorSession.SessionType sessionType;

    private Long monitoredAgentId;

    @Builder.Default
    private Boolean isSilent = true;

    @Builder.Default
    private Boolean canIntervene = false;

    private String notes;
}
