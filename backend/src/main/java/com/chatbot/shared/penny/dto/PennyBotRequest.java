package com.chatbot.shared.penny.dto;

import com.chatbot.shared.penny.model.PennyBotType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Penny Bot creation/update requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PennyBotRequest {
    
    @NotBlank(message = "Bot name is required")
    private String botName;
    
    @NotNull(message = "Bot type is required")
    private PennyBotType botType;
    
    @NotNull(message = "Tenant ID is required")
    private Long tenantId;
    
    @NotBlank(message = "Owner ID is required")
    private String ownerId;
    
    private String botpressBotId;
    
    private String description;
}
