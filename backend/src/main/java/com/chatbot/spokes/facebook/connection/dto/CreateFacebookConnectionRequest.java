package com.chatbot.spokes.facebook.connection.dto;

import com.chatbot.spokes.facebook.connection.model.ChatbotProvider;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import java.util.Map;

@Data
public class CreateFacebookConnectionRequest {
    @NotBlank(message = "Bot ID is required")
    private String botId;
    
    private String botName;
    
    @NotBlank(message = "Page ID is required")
    private String pageId;
    
    private String fanpageUrl;
    
    @NotBlank(message = "Page Access Token is required")
    private String pageAccessToken;
    
    private String appSecret;
    private String verifyToken;
    private String urlCallback;
    
    private boolean isEnabled = true;
    private ChatbotProvider chatbotProvider = ChatbotProvider.PENNYBOT; // Default to PENNYBOT
    
    // Additional fields from frontend modal
    private String connectionName;
    private String description;
    private Integer priority;
    private String connectionType;
    
    // Configuration map for additional settings
    private Map<String, Object> config;
}