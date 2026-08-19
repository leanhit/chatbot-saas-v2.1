package com.chatbot.spokes.facebook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for Facebook Message - used for cross-spoke communication
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacebookMessageDTO {
    private String messageId;
    private String senderId;
    private String recipientId;
    private String messageText;
    private String messageType;
    private Map<String, Object> attachments;
    private LocalDateTime timestamp;
    private UUID connectionId;
    private Long tenantId;
}
