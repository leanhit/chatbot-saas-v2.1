package com.chatbot.core.message.store.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.chatbot.shared.utils.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO for Agent entity
 * Excludes sensitive internal fields and provides clean API response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponseDTO {

    private Long id;
    private Long userId;
    private String name;
    private String email;
    
    private String role; // String representation of AgentRole enum
    
    private String status; // String representation of AgentStatus enum
    
    private Integer currentLoad;
    private Integer maxConcurrentConversations;
    private Set<String> skills;
    private String assignmentPreferences;
    private Boolean active;
    
    private String bio;
    private String phoneNumber;
    private String avatarUrl;
    
    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    private LocalDateTime updatedAt;
    
    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    private LocalDateTime lastActivityAt;
    
    private Long tenantId;
    
    /**
     * Convert Agent entity to AgentResponseDTO
     */
    public static AgentResponseDTO fromEntity(com.chatbot.core.message.store.model.Agent agent) {
        return AgentResponseDTO.builder()
                .id(agent.getId())
                .userId(agent.getUserId())
                .name(agent.getName())
                .email(agent.getEmail())
                .role(agent.getRole() != null ? agent.getRole().name() : null)
                .status(agent.getStatus() != null ? agent.getStatus().name() : null)
                .currentLoad(agent.getCurrentLoad())
                .maxConcurrentConversations(agent.getMaxConcurrentConversations())
                .skills(agent.getSkills())
                .assignmentPreferences(agent.getAssignmentPreferences())
                .active(agent.getActive())
                .bio(agent.getBio())
                .phoneNumber(agent.getPhoneNumber())
                .avatarUrl(agent.getAvatarUrl())
                .createdAt(agent.getCreatedAt())
                .updatedAt(agent.getUpdatedAt())
                .lastActivityAt(agent.getLastActivityAt())
                .tenantId(agent.getTenantId())
                .build();
    }
}
