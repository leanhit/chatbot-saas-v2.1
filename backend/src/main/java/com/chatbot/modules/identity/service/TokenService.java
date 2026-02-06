package com.chatbot.modules.identity.service;

import com.chatbot.modules.identity.dto.LoginResponse;
import com.chatbot.modules.identity.dto.RefreshResponse;
import com.chatbot.modules.identity.security.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Token service for JWT generation and validation
 * v0.2: Updated to use UUID for tenant IDs
 * TODO: Implement refresh token rotation
 * TODO: Add token revocation support
 * TODO: Implement asymmetric key signing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {
    
    private final JwtProperties jwtProperties;
    private Key signingKey;
    
    // Initialize signing key from secret
    private Key getSigningKey() {
        if (signingKey == null) {
            byte[] keyBytes = jwtProperties.getSecret().getBytes();
            signingKey = Keys.hmacShaKeyFor(keyBytes);
        }
        return signingKey;
    }
    
    /**
     * Generate access and refresh tokens for user
     */
    public LoginResponse generateTokens(Long userId, List<UUID> tenantIds) {
        return generateTokens(userId, tenantIds, "vi"); // Default locale
    }
    
    /**
     * Generate access and refresh tokens for user with locale
     */
    public LoginResponse generateTokens(Long userId, List<UUID> tenantIds, String locale) {
        String accessToken = generateAccessToken(userId, tenantIds, locale);
        String refreshToken = generateRefreshToken(userId);
        
        log.info("LOGIN issuing token={}", accessToken);
        
        return LoginResponse.of(
            accessToken,
            refreshToken,
            jwtProperties.getExpiration() / 1000 // Convert to seconds
        );
    }
    
    /**
     * Generate access token with basic claims only
     */
    private String generateAccessToken(Long userId, List<UUID> tenantIds) {
        return generateAccessToken(userId, tenantIds, "vi"); // Default locale
    }
    
    /**
     * Generate access token with basic claims and locale
     */
    private String generateAccessToken(Long userId, List<UUID> tenantIds, String locale) {
        Instant now = Instant.now();
        Instant expiration = now.plus(jwtProperties.getExpiration(), ChronoUnit.MILLIS);
        
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("scope", "identity")
                .claim("tenant_ids", tenantIds)
                .claim("locale", locale != null ? locale : "vi")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .setIssuer(jwtProperties.getIssuer())
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Generate refresh token
     */
    private String generateRefreshToken(Long userId) {
        Instant now = Instant.now();
        Instant expiration = now.plus(jwtProperties.getRefreshExpiration(), ChronoUnit.MILLIS);
        
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("type", "refresh")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .setIssuer(jwtProperties.getIssuer())
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Validate JWT token and extract claims
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("Token expired: {}", e.getMessage());
            throw new SecurityException("Token has expired");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported token: {}", e.getMessage());
            throw new SecurityException("Unsupported token");
        } catch (MalformedJwtException e) {
            log.error("Malformed token: {}", e.getMessage());
            throw new SecurityException("Malformed token");
        } catch (SecurityException e) {
            log.error("Invalid token signature: {}", e.getMessage());
            throw new SecurityException("Invalid token signature");
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            throw new SecurityException("Token validation failed");
        }
    }
    
    /**
     * Extract user ID from token
     */
    public Long extractUserId(String token) {
        Claims claims = validateToken(token);
        return Long.parseLong(claims.getSubject());
    }
    
    /**
     * Extract tenant IDs from token
     */
    @SuppressWarnings("unchecked")
    public List<UUID> extractTenantIds(String token) {
        Claims claims = validateToken(token);
        return (List<UUID>) claims.get("tenant_ids");
    }
    
    /**
     * Check if token is a refresh token
     */
    public boolean isRefreshToken(String token) {
        Claims claims = validateToken(token);
        return "refresh".equals(claims.get("type"));
    }
    
    /**
     * Refresh tokens using valid refresh token
     */
    public RefreshResponse refreshTokens(String refreshToken, List<UUID> tenantIds) {
        if (!isRefreshToken(refreshToken)) {
            throw new SecurityException("Invalid refresh token");
        }
        
        Long userId = extractUserId(refreshToken);
        
        String newAccessToken = generateAccessToken(userId, tenantIds);
        String newRefreshToken = generateRefreshToken(userId);
        
        // TODO: Invalidate old refresh token
        
        return RefreshResponse.of(
            newAccessToken,
            newRefreshToken,
            jwtProperties.getExpiration() / 1000
        );
    }
}
