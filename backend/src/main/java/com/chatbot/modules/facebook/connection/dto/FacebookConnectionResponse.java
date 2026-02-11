// src/main/java/com/chatbot/connection/dto/FacebookConnectionResponse.java

package com.chatbot.modules.facebook.connection.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FacebookConnectionResponse {
    private UUID id;
    private String botId;
    private String botName;
    private String pageId;
    private String fanpageUrl;
    private String pageAccessToken;
    private boolean isEnabled;
    private boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime updatedAt;
}