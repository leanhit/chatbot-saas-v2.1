package com.chatbot.spokes.facebook.autoconnect.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * DTO for auto-connect client request (manual connection list)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutoConnectClientRequest {
    
    @NotNull(message = "Connections list is required")
    @NotEmpty(message = "Connections list cannot be empty")
    @Valid
    private List<ConnectionData> connections;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConnectionData {
        
        @NotNull(message = "Bot ID is required")
        private String botId;
        
        @NotNull(message = "Bot name is required")
        private String botName;
        
        @NotNull(message = "Page ID is required")
        private String pageId;
        
        @NotNull(message = "Fanpage URL is required")
        private String fanpageUrl;
        
        // Page access token (optional if userAccessToken is provided)
        private String pageAccessToken;
        
        // User access token for token exchange (like traloitudongV2)
        @NotNull(message = "User access token is required")
        private String userAccessToken;
        
        @Builder.Default
        private Boolean isEnabled = true;
    }
}
