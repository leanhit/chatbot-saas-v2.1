package com.chatbot.core.routing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupervisorTakeoverResult {
    
    private Long conversationId;
    private Long supervisorId;
    private boolean success;
    private Long sessionId;
    private String sessionType;
    private LocalDateTime takenOverAt;
    private LocalDateTime releasedAt;
    private String reason;
}
