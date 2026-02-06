package com.chatbot.modules.messagehub.core.dto;

import lombok.*;
import java.util.Map;
import lombok.experimental.NonFinal;

/**
 * Message response DTO for Message Hub MVP
 * Response from Message Gateway back to adapters
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@NonFinal
public class MessageResponse {

    private boolean success;
    private String message;
    private String decision; // BOT_PROCESS, HUMAN_REQUIRED, BLOCKED
    private String reason;
    private String handlerType; // bot, human
    private Map<String, Object> metadata;

    public static MessageResponse botProcess(String message) {
        return MessageResponse.builder()
                .success(true)
                .message(message)
                .decision("BOT_PROCESS")
                .handlerType("bot")
                .build();
    }
    
    public static MessageResponse botProcess(String message, Map<String, Object> metadata) {
        return MessageResponse.builder()
                .success(true)
                .message(message)
                .decision("BOT_PROCESS")
                .handlerType("bot")
                .metadata(metadata)
                .build();
    }

    public static MessageResponse humanRequired(String reason) {
        return MessageResponse.builder()
                .success(true)
                .decision("HUMAN_REQUIRED")
                .reason(reason)
                .handlerType("human")
                .build();
    }

    public static MessageResponse blocked(String reason) {
        return MessageResponse.builder()
                .success(false)
                .decision("BLOCKED")
                .reason(reason)
                .build();
    }
}
