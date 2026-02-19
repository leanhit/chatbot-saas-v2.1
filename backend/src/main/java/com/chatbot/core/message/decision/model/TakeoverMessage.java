package com.chatbot.core.message.decision.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TakeoverMessage {
    private String conversationId;  // ID cuộc trò chuyện
    private String sender;          // user | bot | agent
    private String content;
    private long timestamp;         // millis
}
