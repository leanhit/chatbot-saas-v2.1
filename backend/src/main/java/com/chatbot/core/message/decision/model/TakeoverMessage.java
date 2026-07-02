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
    @NotBlank(message = "Message ID is required")
    private String id;              // Unique message ID
    
    @NotBlank(message = "Conversation ID is required")
    private String conversationId;  // ID cuộc trò chuyện
    
    @NotBlank(message = "Sender is required")
    private String sender;          // user | bot | agent
    
    @NotBlank(message = "Content is required")
    private String content;
    
    @NotNull(message = "Timestamp is required")
    private long timestamp;         // millis
}
