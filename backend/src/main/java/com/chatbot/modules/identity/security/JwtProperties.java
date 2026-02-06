package com.chatbot.modules.identity.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT configuration properties
 * TODO: Add key rotation configuration
 * TODO: Add multiple issuer support
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "identity.jwt")
public class JwtProperties {
    
    private String secret;
    
    private Long expiration = 86400000L; // 24 hours in milliseconds
    
    private Long refreshExpiration = 2592000000L; // 30 days in milliseconds
    
    private String issuer = "identity-hub";
}
