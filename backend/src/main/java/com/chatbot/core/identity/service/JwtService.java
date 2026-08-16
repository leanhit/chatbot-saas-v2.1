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
import io.jsonwebtoken.security.SignatureException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

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

    @Value("${spring.security.jwt.algorithm:HS256}")
    private String jwtAlgorithm;

    @Value("${spring.security.jwt.rsa.private-key:}")
    private String rsaPrivateKey;

    @Value("${spring.security.jwt.rsa.public-key:}")
    private String rsaPublicKey;

    private Key hmacKey;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtKeyManagementService jwtKeyManagementService;

    public JwtService(@Autowired(required = false) TokenBlacklistService tokenBlacklistService,
                      @Autowired(required = false) JwtKeyManagementService jwtKeyManagementService) {
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtKeyManagementService = jwtKeyManagementService;
    }

    @PostConstruct
    public void init() {
        try {
            // Initialize HMAC key for HS256
            this.hmacKey = Keys.hmacShaKeyFor(secretKey.getBytes());
            
            // Initialize RSA keys for RS256
            if ("RS256".equals(jwtAlgorithm)) {
                if (rsaPrivateKey == null || rsaPrivateKey.isEmpty()) {
                    throw new IllegalArgumentException("RSA private key is required for RS256 algorithm");
                }
                if (rsaPublicKey == null || rsaPublicKey.isEmpty()) {
                    throw new IllegalArgumentException("RSA public key is required for RS256 algorithm");
                }
                
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                
                // Remove PEM headers and decode
                String privateKeyPEM = rsaPrivateKey.replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "")
                        .replaceAll("\\s", "");
                String publicKeyPEM = rsaPublicKey.replace("-----BEGIN PUBLIC KEY-----", "")
                        .replace("-----END PUBLIC KEY-----", "")
                        .replaceAll("\\s", "");
                
                PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyPEM));
                X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyPEM));
                
                this.privateKey = keyFactory.generatePrivate(privateSpec);
                this.publicKey = keyFactory.generatePublic(publicSpec);
                
                log.info("RSA keys initialized successfully for RS256 algorithm");
            } else {
                log.info("HMAC key initialized successfully for HS256 algorithm");
            }
        } catch (Exception e) {
            log.error("Failed to initialize JWT keys: {}", e.getMessage(), e);
            throw new RuntimeException("JWT key initialization failed", e);
        }
    }

    public String generateToken(String email) {
        try {
            if ("RS256".equals(jwtAlgorithm)) {
                return Jwts.builder()
                        .setSubject(email)
                        .setIssuedAt(new Date())
                        .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                        .signWith(privateKey, SignatureAlgorithm.RS256)
                        .compact();
            } else {
                return Jwts.builder()
                        .setSubject(email)
                        .setIssuedAt(new Date())
                        .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                        .signWith(hmacKey, SignatureAlgorithm.HS256)
                        .compact();
            }
        } catch (Exception e) {
            log.error("Token generation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate token", e);
        }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            if (tokenBlacklistService != null && tokenBlacklistService.isTokenBlacklisted(token)) {
                log.warn("Token is blacklisted: {}", token.substring(0, Math.min(10, token.length())));
                return false;
            }
            
            String emailFromToken = extractEmail(token);
            if (tokenBlacklistService != null && tokenBlacklistService.areUserTokensBlacklisted(emailFromToken)) {
                log.warn("User tokens are blacklisted for email: {}", emailFromToken);
                return false;
            }
            
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
            Key validationKey = getValidationKey(token);
            
            if ("RS256".equals(jwtAlgorithm)) {
                return Jwts.parserBuilder()
                        .setSigningKey(validationKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
            } else {
                return Jwts.parserBuilder()
                        .setSigningKey(validationKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
            }
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.debug("Invalid JWT signature: {}", e.getMessage());
            throw new InvalidTokenException("Token signature is invalid", e);
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            throw new InvalidTokenException("Invalid JWT token", e);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.debug("JWT token is expired: {}", e.getMessage());
            throw e; // re-throw ExpiredJwtException so JwtFilter can catch it specifically
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.debug("JWT token is unsupported: {}", e.getMessage());
            throw new InvalidTokenException("JWT token is unsupported", e);
        } catch (IllegalArgumentException e) {
            log.debug("JWT claims string is empty: {}", e.getMessage());
            throw new InvalidTokenException("JWT claims string is empty", e);
        } catch (Exception e) {
            log.error("Token parsing failed: {}", e.getMessage());
            throw new InvalidTokenException(com.chatbot.shared.exceptions.ErrorCode.TOKEN_INVALID_OR_EXPIRED, "Token is invalid or has expired", e);
        }
    }

    private Key getValidationKey(String token) {
        // Try to get key from key management service if rotation is enabled
        if (jwtKeyManagementService != null) {
            try {
                // Extract key ID from token header
                String keyId = extractKeyIdFromToken(token);
                if (keyId != null && jwtKeyManagementService.isValidKeyId(keyId)) {
                    Key key = jwtKeyManagementService.getKeyById(keyId);
                    if (key != null) {
                        return key;
                    }
                }
                
                // If key ID not found or invalid, try current key
                Key currentKey = jwtKeyManagementService.getCurrentSigningKey();
                if (currentKey != null) {
                    return currentKey;
                }
            } catch (Exception e) {
                log.debug("Failed to get key from key management service: {}", e.getMessage());
            }
        }
        
        // Fall back to static key
        return "RS256".equals(jwtAlgorithm) ? publicKey : hmacKey;
    }

    private String extractKeyIdFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 1) {
                return null;
            }
            
            // Decode header
            String header = new String(Base64.getDecoder().decode(parts[0]));
            
            // Extract kid from header (simple JSON parsing)
            if (header.contains("\"kid\"")) {
                int kidIndex = header.indexOf("\"kid\"");
                int colonIndex = header.indexOf(":", kidIndex);
                int startIndex = header.indexOf("\"", colonIndex) + 1;
                int endIndex = header.indexOf("\"", startIndex);
                
                if (startIndex > 0 && endIndex > startIndex) {
                    return header.substring(startIndex, endIndex);
                }
            }
            
            return null;
        } catch (Exception e) {
            log.debug("Failed to extract key ID from token: {}", e.getMessage());
            return null;
        }
    }

    private <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = getClaims(token);
        return claimsResolver.apply(claims);
    }

    // License JWT methods according to req.md - Cloud only signing
    public String generateLicenseToken(String email, Long userId, Long expiration, 
                                     List<String> features, List<String> modules, 
                                     Map<String, Integer> limits) {
        try {
            if ("RS256".equals(jwtAlgorithm)) {
                return Jwts.builder()
                        .setSubject(email)
                        .claim("sub", userId.toString()) // User ID as string for local app
                        .claim("email", email)
                        .claim("exp", expiration) // Unix timestamp for local app compatibility
                        .claim("features", features)
                        .claim("modules", modules)
                        .claim("limits", limits)
                        .claim("signed_by", "cloud") // Prevent client self-signing
                        .setIssuedAt(new Date())
                        .signWith(privateKey, SignatureAlgorithm.RS256)
                        .compact();
            } else {
                return Jwts.builder()
                        .setSubject(email)
                        .claim("sub", userId.toString())
                        .claim("email", email)
                        .claim("exp", expiration)
                        .claim("features", features)
                        .claim("modules", modules)
                        .claim("limits", limits)
                        .claim("signed_by", "cloud") // Prevent client self-signing
                        .setIssuedAt(new Date())
                        .signWith(hmacKey, SignatureAlgorithm.HS256)
                        .compact();
            }
        } catch (Exception e) {
            log.error("License token generation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate license token", e);
        }
    }

    // Verify license was signed by cloud (prevent client self-signing)
    public boolean verifyLicenseSignedByCloud(String token) {
        try {
            // First, verify the cryptographic signature using getClaims()
            // This will throw an exception if the signature is invalid
            Claims claims = getClaims(token);
            
            // Then, verify the "signed_by" claim to ensure it's from cloud
            // This prevents a valid signature from being used if it wasn't signed by cloud
            String signedBy = claims.get("signed_by", String.class);
            boolean isCloudSigned = "cloud".equals(signedBy);
            
            if (!isCloudSigned) {
                log.warn("License token is cryptographically valid but not signed by cloud");
                return false;
            }
            
            // Additional verification: check issuer if present
            String issuer = claims.getIssuer();
            if (issuer != null && !issuer.contains("cloud")) {
                log.warn("License token issuer is not cloud: {}", issuer);
                return false;
            }
            
            log.debug("License token verified as cloud-signed");
            return true;
        } catch (SignatureException e) {
            log.error("License signature verification failed - invalid cryptographic signature: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("License signature verification failed: {}", e.getMessage());
            return false;
        }
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
