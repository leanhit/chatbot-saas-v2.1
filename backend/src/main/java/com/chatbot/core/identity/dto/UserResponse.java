// UserResponse.java
package com.chatbot.core.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private String token;
    private String refreshToken;
    private UserDto user;
    
    // Constructor for backward compatibility
    public UserResponse(String token, UserDto user) {
        this.token = token;
        this.user = user;
    }
}