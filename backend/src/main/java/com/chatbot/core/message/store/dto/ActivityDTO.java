package com.chatbot.core.message.store.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivityDTO {
    private String id;
    /** "conversation" | "takeover" | "bot_response" | "closed" | "connection" */
    private String type;
    private String title;
    private String description;
    private LocalDateTime timestamp;
}
