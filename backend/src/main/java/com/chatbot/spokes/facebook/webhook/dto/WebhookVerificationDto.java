package com.chatbot.spokes.facebook.webhook.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookVerificationDto {
    
    @NotBlank(message = "Hub mode is required")
    private String mode;
    
    @NotBlank(message = "Hub verify token is required")
    private String verifyToken;
    
    @NotBlank(message = "Hub challenge is required")
    private String challenge;
    
    /**
     * Validates if the webhook verification is for subscription
     */
    public boolean isSubscriptionMode() {
        return "subscribe".equals(mode);
    }
    
    /**
     * Validates if the webhook verification is for page
     */
    public boolean isPageMode() {
        return "page".equals(mode);
    }
}
