package com.chatbot.modules.identity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Refresh token request DTO
 * TODO: Add device validation
 * TODO: Add refresh token rotation support
 */
@Data
public class RefreshRequest {
    
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
