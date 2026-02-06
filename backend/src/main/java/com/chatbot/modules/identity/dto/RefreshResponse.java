package com.chatbot.modules.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Refresh token response DTO
 * TODO: Add token metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshResponse {
    
    private String accessToken;
    
    private String refreshToken;
    
    @Builder.Default
    private String tokenType = "Bearer";
    
    private Long expiresIn; // seconds
    
    public static RefreshResponse of(String accessToken, String refreshToken, Long expiresIn) {
        return RefreshResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .build();
    }
}
