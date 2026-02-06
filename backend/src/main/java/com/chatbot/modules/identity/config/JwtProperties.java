package com.chatbot.modules.identity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ⚠️ DO NOT REGISTER AS SPRING BEAN
 * This class is kept ONLY for reference / mapping purpose.
 * Actual JwtProperties bean is in: identity.security.JwtProperties
 */
@Data
@ConfigurationProperties(prefix = "identity.jwt")
public class JwtProperties {

    private String secret;
    private long expiration;
    private long refreshExpiration;
    private String issuer;
}
