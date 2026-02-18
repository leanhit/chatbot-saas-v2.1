package com.chatbot.modules.penny.dto;

import com.chatbot.modules.penny.model.PennyBotType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Penny Bot
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PennyBotDto {
    
    private UUID id;
    private String botName;
    private PennyBotType botType;
    private Long tenantId;
    private String ownerId;
    private String botpressBotId;
    private String description;
    private Boolean isActive;
    private Boolean isEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
