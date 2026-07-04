package com.chatbot.core.message.decision.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TakeoverMessage {
    private String id;              // Unique message ID
    
    @NotBlank(message = "Conversation ID is required")
    private String conversationId;  // ID cuộc trò chuyện
    
    private String sender;          // user | bot | agent
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private Long timestamp;         // millis
}
