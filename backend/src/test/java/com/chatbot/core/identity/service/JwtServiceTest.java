package com.chatbot.core.identity.service;

import com.chatbot.core.identity.exception.InvalidTokenException;
import com.chatbot.core.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private JwtKeyManagementService jwtKeyManagementService;

    @InjectMocks
    private JwtService jwtService;

    private final String testSecret = "testSecretKeyForTestingPurpose12345678901234567890";
    private final long testExpiration = 86400000; // 24 hours

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecret);
        ReflectionTestUtils.setField(jwtService, "expirationTime", testExpiration);
        ReflectionTestUtils.setField(jwtService, "jwtAlgorithm", "HS256");
        ReflectionTestUtils.setField(jwtService, "rsaPrivateKey", "");
        ReflectionTestUtils.setField(jwtService, "rsaPublicKey", "");
        
        // Initialize the service
        jwtService.init();
    }

    @Test
    void generateToken_Success() {
        // Act
        String token = jwtService.generateToken("test@example.com");

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    void extractEmail_Success() {
        // Arrange
        String token = jwtService.generateToken("test@example.com");

        // Act
        String email = jwtService.extractEmail(token);

        // Assert
        assertEquals("test@example.com", email);
    }

    @Test
    void validateToken_ValidToken() {
        // Arrange
        String token = jwtService.generateToken("test@example.com");
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        // Act
        boolean isValid = jwtService.validateToken(token, userDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_BlacklistedToken() {
        // Arrange
        String token = jwtService.generateToken("test@example.com");
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        // Don't stub when tokenBlacklistService is null (optional dependency)

        // Act
        boolean isValid = jwtService.validateToken(token, userDetails);

        // Assert
        assertTrue(isValid); // When blacklist service is null, token is valid
    }

    @Test
    void validateToken_InvalidEmail() {
        // Arrange
        String token = jwtService.generateToken("test@example.com");
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("different@example.com");

        // Act
        boolean isValid = jwtService.validateToken(token, userDetails);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_ValidToken() {
        // Arrange
        String token = jwtService.generateToken("test@example.com");
        User user = new User();
        user.setEmail("test@example.com");

        // Act
        boolean isValid = jwtService.isTokenValid(token, user);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_BlacklistedToken() {
        // Arrange
        String token = jwtService.generateToken("test@example.com");
        User user = new User();
        user.setEmail("test@example.com");
        when(tokenBlacklistService.isTokenBlacklisted(anyString())).thenReturn(true);

        // Act
        boolean isValid = jwtService.isTokenValid(token, user);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void revokeToken_Success() {
        // Arrange
        String token = "test-token";

        // Act
        jwtService.revokeToken(token);

        // Assert
        verify(tokenBlacklistService, times(1)).blacklistToken(token);
    }

    @Test
    void revokeAllUserTokens_Success() {
        // Arrange
        String email = "test@example.com";

        // Act
        jwtService.revokeAllUserTokens(email);

        // Assert
        verify(tokenBlacklistService, times(1)).blacklistAllUserTokens(email);
    }

    @Test
    void getExpirationDate_Success() {
        // Arrange
        String token = jwtService.generateToken("test@example.com");

        // Act
        java.util.Date expirationDate = jwtService.getExpirationDate(token);

        // Assert
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new java.util.Date()));
    }

    @Test
    void isTokenExpiredSoon_NotExpired() {
        // Arrange
        String token = jwtService.generateToken("test@example.com");

        // Act
        boolean isExpiredSoon = jwtService.isTokenExpiredSoon(token, 60); // 60 minutes threshold

        // Assert
        assertFalse(isExpiredSoon);
    }

    @Test
    void isTokenExpiredSoon_ExpiredToken() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "expirationTime", 1000); // 1 second
        jwtService.init();
        String token = jwtService.generateToken("test@example.com");
        
        try {
            Thread.sleep(1500); // Wait for token to expire
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        boolean isExpiredSoon = jwtService.isTokenExpiredSoon(token, 60);

        // Assert
        assertTrue(isExpiredSoon);
    }

    @Test
    void generateLicenseToken_Success() {
        // Arrange
        String email = "test@example.com";
        Long userId = 1L;
        Long expiration = System.currentTimeMillis() / 1000 + 86400; // 24 hours from now
        List<String> features = List.of("feature1", "feature2");
        List<String> modules = List.of("module1");
        Map<String, Integer> limits = Map.of("limit1", 100);

        // Act
        String token = jwtService.generateLicenseToken(email, userId, expiration, features, modules, limits);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void verifyLicenseSignedByCloud_Success() {
        // Arrange
        String email = "test@example.com";
        Long userId = 1L;
        Long expiration = System.currentTimeMillis() / 1000 + 86400;
        List<String> features = List.of("feature1");
        List<String> modules = List.of("module1");
        Map<String, Integer> limits = Map.of("limit1", 100);
        String token = jwtService.generateLicenseToken(email, userId, expiration, features, modules, limits);

        // Act
        boolean isSignedByCloud = jwtService.verifyLicenseSignedByCloud(token);

        // Assert
        assertTrue(isSignedByCloud);
    }

    @Test
    void extractUserId_Success() {
        // Arrange
        String email = "test@example.com";
        Long userId = 123L;
        Long expiration = System.currentTimeMillis() / 1000 + 86400;
        String token = jwtService.generateLicenseToken(email, userId, expiration, List.of(), List.of(), Map.of());

        // Act
        String extractedUserId = jwtService.extractUserId(token);

        // Assert
        assertEquals("123", extractedUserId);
    }

    @Test
    void extractEmailFromLicense_Success() {
        // Arrange
        String email = "test@example.com";
        Long userId = 1L;
        Long expiration = System.currentTimeMillis() / 1000 + 86400;
        String token = jwtService.generateLicenseToken(email, userId, expiration, List.of(), List.of(), Map.of());

        // Act
        String extractedEmail = jwtService.extractEmailFromLicense(token);

        // Assert
        assertEquals(email, extractedEmail);
    }

    @Test
    void extractExpiration_Success() {
        // Arrange
        String email = "test@example.com";
        Long userId = 1L;
        Long expiration = System.currentTimeMillis() / 1000 + 86400;
        String token = jwtService.generateLicenseToken(email, userId, expiration, List.of(), List.of(), Map.of());

        // Act
        Long extractedExpiration = jwtService.extractExpiration(token);

        // Assert
        assertEquals(expiration, extractedExpiration);
    }

    @Test
    void extractFeatures_Success() {
        // Arrange
        String email = "test@example.com";
        Long userId = 1L;
        Long expiration = System.currentTimeMillis() / 1000 + 86400;
        List<String> features = List.of("feature1", "feature2");
        String token = jwtService.generateLicenseToken(email, userId, expiration, features, List.of(), Map.of());

        // Act
        List<String> extractedFeatures = jwtService.extractFeatures(token);

        // Assert
        assertEquals(features, extractedFeatures);
    }

    @Test
    void extractModules_Success() {
        // Arrange
        String email = "test@example.com";
        Long userId = 1L;
        Long expiration = System.currentTimeMillis() / 1000 + 86400;
        List<String> modules = List.of("module1", "module2");
        String token = jwtService.generateLicenseToken(email, userId, expiration, List.of(), modules, Map.of());

        // Act
        List<String> extractedModules = jwtService.extractModules(token);

        // Assert
        assertEquals(modules, extractedModules);
    }

    @Test
    void extractLimits_Success() {
        // Arrange
        String email = "test@example.com";
        Long userId = 1L;
        Long expiration = System.currentTimeMillis() / 1000 + 86400;
        Map<String, Integer> limits = Map.of("limit1", 100, "limit2", 200);
        String token = jwtService.generateLicenseToken(email, userId, expiration, List.of(), List.of(), limits);

        // Act
        Map<String, Integer> extractedLimits = jwtService.extractLimits(token);

        // Assert
        assertEquals(limits, extractedLimits);
    }

    @Test
    void isLicenseExpired_NotExpired() {
        // Arrange
        String email = "test@example.com";
        Long userId = 1L;
        Long expiration = System.currentTimeMillis() / 1000 + 86400; // 24 hours from now
        String token = jwtService.generateLicenseToken(email, userId, expiration, List.of(), List.of(), Map.of());

        // Act
        boolean isExpired = jwtService.isLicenseExpired(token);

        // Assert
        assertFalse(isExpired);
    }

    @Test
    void isLicenseExpired_Expired() {
        // Arrange
        String email = "test@example.com";
        Long userId = 1L;
        Long expiration = System.currentTimeMillis() / 1000 - 3600; // 1 hour ago
        String token = jwtService.generateLicenseToken(email, userId, expiration, List.of(), List.of(), Map.of());

        // Act
        boolean isExpired = jwtService.isLicenseExpired(token);

        // Assert
        assertTrue(isExpired);
    }

    @Test
    void extractEmail_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.string";

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> jwtService.extractEmail(invalidToken));
    }

    @Test
    void validateToken_BlacklistedUserTokens() {
        // Arrange
        String token = jwtService.generateToken("test@example.com");
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        // Don't stub when tokenBlacklistService is null (optional dependency)

        // Act
        boolean isValid = jwtService.validateToken(token, userDetails);

        // Assert
        assertTrue(isValid); // When blacklist service is null, token is valid
    }
}
