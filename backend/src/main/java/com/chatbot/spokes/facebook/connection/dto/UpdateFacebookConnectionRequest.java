package com.chatbot.spokes.facebook.connection.dto;

import com.chatbot.spokes.facebook.connection.model.ChatbotProvider;
import lombok.Data;
import java.util.Map;

@Data
public class UpdateFacebookConnectionRequest {
    private String botName;
    private String botId;
    private String pageId;
    private String fanpageUrl;
    private String pageAccessToken;
    private String appSecret;
    private String verifyToken;
    private String urlCallback;
    
    private Boolean isEnabled;
    private Boolean isActive;
    private ChatbotProvider chatbotProvider;
    
    // Additional fields from frontend modal
    private String connectionName;
    private String description;
    private Integer priority;
    private String connectionType;
    
    // Configuration map for additional settings
    private Map<String, Object> config;
}