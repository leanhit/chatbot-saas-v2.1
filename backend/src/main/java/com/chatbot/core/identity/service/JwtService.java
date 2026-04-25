// JwtService.java
package com.chatbot.core.identity.service;
import com.chatbot.core.identity.constants.IdentityConstants;
import com.chatbot.core.identity.exception.InvalidTokenException;
import com.chatbot.core.user.model.User;
import lombok.extern.slf4j.Slf4j;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    @Value("${spring.security.jwt.secret}")
    private String secretKey;

    @Value("${spring.security.jwt.expiration}")
    private long expirationTime;

    private Key key;
    
    @Autowired(required = false)
    private TokenBlacklistService tokenBlacklistService;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            if (tokenBlacklistService != null && tokenBlacklistService.isTokenBlacklisted(token)) {
                log.warn("Token is blacklisted: {}", token.substring(0, Math.min(10, token.length())));
                return false;
            }
            
            String emailFromToken = extractEmail(token);
            return (emailFromToken.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return getClaim(token, Claims::getExpiration).before(new Date());
    }

    public String extractEmail(String token) {
        return getClaim(token, Claims::getSubject);
    }

    /**
     * @deprecated Use extractEmail instead
     */
    @Deprecated
    public String getEmailFromToken(String token) {
        return extractEmail(token);
    }

    /**
     * @deprecated Use extractEmail instead
     */
    @Deprecated
    public String extractUsername(String token) {
        return extractEmail(token);
    }

    public boolean isTokenValid(String token, User user) {
        try {
            if (tokenBlacklistService != null && tokenBlacklistService.isTokenBlacklisted(token)) {
                log.warn("Token is blacklisted for user: {}", user.getEmail());
                return false;
            }
            
            String emailFromToken = extractEmail(token);
            return (emailFromToken.equals(user.getEmail()) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    public void revokeToken(String token) {
        if (tokenBlacklistService != null) {
            tokenBlacklistService.blacklistToken(token);
        }
    }

    public void revokeAllUserTokens(String userEmail) {
        if (tokenBlacklistService != null) {
            tokenBlacklistService.blacklistAllUserTokens(userEmail);
        }
    }

    public Date getExpirationDate(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpiredSoon(String token, long thresholdMinutes) {
        try {
            Date expiration = getExpirationDate(token);
            long timeUntilExpiration = expiration.getTime() - System.currentTimeMillis();
            return timeUntilExpiration <= (thresholdMinutes * 60 * 1000);
        } catch (Exception e) {
            return true; // Consider expired if we can't parse
        }
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new InvalidTokenException("Token không hợp lệ hoặc đã hết hạn", e);
        }
    }

    private <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = getClaims(token);
        return claimsResolver.apply(claims);
    }

    // License JWT methods according to req.md
    public String generateLicenseToken(String email, Long userId, Long expiration, 
                                     List<String> features, List<String> modules, 
                                     Map<String, Integer> limits) {
        return Jwts.builder()
                .setSubject(email)
                .claim("sub", userId.toString()) // User ID as string for local app
                .claim("email", email)
                .claim("exp", expiration) // Unix timestamp for local app compatibility
                .claim("features", features)
                .claim("modules", modules)
                .claim("limits", limits)
                .setIssuedAt(new Date())
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUserId(String token) {
        return getClaim(token, claims -> claims.get("sub", String.class));
    }

    public String extractEmailFromLicense(String token) {
        return getClaim(token, claims -> claims.get("email", String.class));
    }

    public Long extractExpiration(String token) {
        return getClaim(token, claims -> claims.get("exp", Long.class));
    }

    @SuppressWarnings("unchecked")
    public List<String> extractFeatures(String token) {
        return getClaim(token, claims -> claims.get("features", List.class));
    }

    @SuppressWarnings("unchecked")
    public List<String> extractModules(String token) {
        return getClaim(token, claims -> claims.get("modules", List.class));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Integer> extractLimits(String token) {
        return getClaim(token, claims -> claims.get("limits", Map.class));
    }

    public boolean isLicenseExpired(String token) {
        try {
            Long exp = extractExpiration(token);
            return exp != null && exp < (System.currentTimeMillis() / 1000);
        } catch (Exception e) {
            log.error("Error checking license expiration: {}", e.getMessage());
            return true; // Consider expired if we can't parse
        }
    }
}
