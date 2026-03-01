package com.chatbot.shared.penny.dto;

import com.chatbot.shared.penny.model.PennyBotType;
import com.chatbot.shared.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Penny Bot responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PennyBotResponse {
    
    private UUID id;
    private String botName;
    private PennyBotType botType;
    private Long tenantId;
    private String ownerId;
    private String botpressBotId;
    private String description;
    private Boolean isActive;
    private Boolean isEnabled;
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private LocalDateTime createdAt;
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private LocalDateTime updatedAt;
}
