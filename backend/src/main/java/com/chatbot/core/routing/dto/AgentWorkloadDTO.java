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
public class AgentWorkloadDTO {
    private Long id;
    private Long tenantId;
    private Long agentId;
    private String agentName;
    private String agentEmail;
    private Long teamId;
    private String teamName;
    private Integer activeConversations;
    private Integer maxConcurrentConversations;
    private String status;
    private Integer priorityScore;
    private LocalDateTime lastActivityAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields
    private Double workloadPercentage;
    private Boolean isAvailable;
    private Integer availableSlots;
}
