package com.chatbot.modules.auth.security;

import com.chatbot.modules.identity.security.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.UUID;

/**
 * JWT Token Consumer - handles both legacy and Identity Hub token formats
 * Legacy: { sub: "email" }
 * Identity: { sub: "userId", scope: "identity", tenant_ids: [1,2,3], locale: "vi" }
 */
@Component
@Slf4j
public class JwtTokenConsumer {

    private final JwtProperties jwtProperties;

    public JwtTokenConsumer(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    private SecretKey signingKey;

    private SecretKey getSigningKey() {
        if (signingKey == null) {
            signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
        }
        return signingKey;
    }

    /**
     * Extract all claims from token
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extract subject (email for legacy, userId for Identity Hub)
     */
    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extract user ID from token (for Identity Hub tokens)
     */
    public Long extractUserId(String token) {
        try {
            String subject = extractSubject(token);
            // Check if subject is numeric (Identity Hub) or email (legacy)
            if (subject.matches("\\d+")) {
                return Long.valueOf(subject);
            }
            return null;
        } catch (Exception e) {
            log.debug("Could not extract userId from token", e);
            return null;
        }
    }

    /**
     * Extract email from token (for legacy tokens)
     */
    public String extractEmail(String token) {
        try {
            String subject = extractSubject(token);
            // Check if subject is email format (legacy)
            if (subject.contains("@")) {
                return subject;
            }
            return null;
        } catch (Exception e) {
            log.debug("Could not extract email from token", e);
            return null;
        }
    }

    /**
     * Extract locale from token
     */
    public String extractLocale(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String locale = (String) claims.get("locale");
            return locale != null ? locale : "vi"; // Default to Vietnamese
        } catch (Exception e) {
            log.debug("Could not extract locale from token", e);
            return "vi"; // Default to Vietnamese
        }
    }

    /**
     * Extract tenant IDs from token (Identity Hub only)
     */
    @SuppressWarnings("unchecked")
    public List<UUID> extractTenantIds(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object tenantIdsObj = claims.get("tenant_ids");
            
            if (tenantIdsObj instanceof List) {
                return (List<UUID>) tenantIdsObj;
            }
            return List.of();
        } catch (Exception e) {
            log.debug("Could not extract tenant_ids from token", e);
            return List.of();
        }
    }

    /**
     * Check if token is from Identity Hub
     */
    public boolean isIdentityHubToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return "identity".equals(claims.get("scope"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if token is legacy format
     */
    public boolean isLegacyToken(String token) {
        return !isIdentityHubToken(token);
    }

    /**
     * Validate token signature and expiration
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            log.debug("Token validation failed", e);
            return false;
        }
    }
}
