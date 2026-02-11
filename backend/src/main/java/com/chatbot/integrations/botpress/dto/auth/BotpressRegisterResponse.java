// dto/BotpressRegisterResponse.java
package com.chatbot.integrations.botpress.dto.auth;

import lombok.Data;

@Data
public class BotpressRegisterResponse {
    private String strategy;
    private String email;
    private boolean isSuperAdmin;
}
