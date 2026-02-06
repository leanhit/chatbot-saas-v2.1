package com.chatbot.modules.identity.dto;

import com.chatbot.modules.auth.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login response DTO containing tokens
 * TODO: Add token type constants
 * TODO: Add refresh token expiration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    
    private String token;
    
    private String refreshToken;
    
    private UserDto user;
    
    @Builder.Default
    private String tokenType = "Bearer";
    
    private Long expiresIn; // seconds
    
    public static LoginResponse of(String token, String refreshToken, Long expiresIn) {
        return LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .build();
    }
    
    public static LoginResponse of(String token, String refreshToken, UserDto user, Long expiresIn) {
        return LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(user)
                .expiresIn(expiresIn)
                .build();
    }
}
