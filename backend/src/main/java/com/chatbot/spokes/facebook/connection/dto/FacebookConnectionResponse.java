// src/main/java/com/chatbot/connection/dto/FacebookConnectionResponse.java

package com.chatbot.spokes.facebook.connection.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import com.chatbot.shared.utils.DateUtils;
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

    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private LocalDateTime createdAt;

    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private LocalDateTime updatedAt;
}