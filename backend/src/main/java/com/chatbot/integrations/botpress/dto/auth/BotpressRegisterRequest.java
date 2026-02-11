// dto/BotpressRegisterRequest.java
package com.chatbot.integrations.botpress.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BotpressRegisterRequest {
    private String email;
    private String password;
}
