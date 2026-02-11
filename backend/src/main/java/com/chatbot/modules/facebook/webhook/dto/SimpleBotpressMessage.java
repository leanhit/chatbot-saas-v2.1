// src/main/java/com/chatbot/chatHub/facebook/webhook/dto/SimpleBotpressMessage.java
package com.chatbot.modules.facebook.webhook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO để gửi tin nhắn văn bản đơn giản đến Botpress.
 */
@Data
public class SimpleBotpressMessage {

    @JsonProperty("text")
    private String text;
    
    @JsonProperty("type")
    private final String type = "text";

    public SimpleBotpressMessage(String text) {
        this.text = text;
    }
}
