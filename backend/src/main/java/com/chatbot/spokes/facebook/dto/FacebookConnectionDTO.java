package com.chatbot.spokes.facebook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Facebook Connection - used for cross-spoke communication
 * This decouples the internal FacebookConnection entity from other spokes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacebookConnectionDTO {
    private UUID id;
    private Long tenantId;
    private String botId;
    private String botName;
    private String pageId;
    private String ownerId;
    private String pageAccessToken;
    private boolean isActive;
    private boolean isEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Convert from internal entity to DTO
     */
    public static FacebookConnectionDTO fromEntity(com.chatbot.spokes.facebook.connection.model.FacebookConnection entity) {
        if (entity == null) {
            return null;
        }
        
        return FacebookConnectionDTO.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .botId(entity.getBotId())
                .botName(entity.getBotName())
                .pageId(entity.getPageId())
                .ownerId(entity.getOwnerId())
                .pageAccessToken(entity.getPageAccessToken())
                .isActive(entity.isActive())
                .isEnabled(entity.isEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
