// dto/BotpressRegisterResponse.java
package com.chatbot.spokes.botpress.dto.auth;

import lombok.Data;

@Data
public class BotpressRegisterResponse {
    private String strategy;
    private String email;
    private boolean isSuperAdmin;
    
    // Setters for Lombok compatibility
    public void setEmail(String email) { this.email = email; }
}
