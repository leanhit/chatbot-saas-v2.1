package com.chatbot.core.identity.service;

import com.chatbot.core.identity.exception.InvalidTokenException;
import com.chatbot.core.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private JwtKeyManagementService jwtKeyManagementService;
    @Mock private UserDetails userDetails;

    private JwtService service;

    @BeforeEach
    void setUp() {
        service = new JwtService(tokenBlacklistService, jwtKeyManagementService);
        ReflectionTestUtils.setField(service, "secretKey",
            "test-secret-key-that-is-long-enough-for-hs256-signing-0123456789");
        ReflectionTestUtils.setField(service, "expirationTime", 3_600_000L); // 1h
        ReflectionTestUtils.setField(service, "jwtAlgorithm", "HS256");
        service.init();
    }

    private JwtService expiredService() {
        JwtService s = new JwtService(tokenBlacklistService, jwtKeyManagementService);
        ReflectionTestUtils.setField(s, "secretKey",
            "test-secret-key-that-is-long-enough-for-hs256-signing-0123456789");
        ReflectionTestUtils.setField(s, "expirationTime", -10_000L); // already expired
        ReflectionTestUtils.setField(s, "jwtAlgorithm", "HS256");
        s.init();
        return s;
    }

    // ---------- generateToken / extractEmail ----------

    @Test
    void generateTokenAndExtractEmail() {
        String token = service.generateToken("u@t.com");
        assertEquals("u@t.com", service.extractEmail(token));
    }

    @Test
    void getExpirationDateIsInFuture() {
        String token = service.generateToken("u@t.com");
        Date exp = service.getExpirationDate(token);
        assertTrue(exp.after(new Date()));
    }

    // ---------- validateToken ----------

    @Test
    void validateTokenTrueForValidMatchingUser() {
        String token = service.generateToken("u@t.com");
        when(userDetails.getUsername()).thenReturn("u@t.com");
        when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(false);
        when(tokenBlacklistService.areUserTokensBlacklisted("u@t.com")).thenReturn(false);

        assertTrue(service.validateToken(token, userDetails));
    }

    @Test
    void validateTokenFalseForWrongUser() {
        String token = service.generateToken("u@t.com");
        when(userDetails.getUsername()).thenReturn("other@t.com");
        when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(false);
        when(tokenBlacklistService.areUserTokensBlacklisted("u@t.com")).thenReturn(false);

        assertFalse(service.validateToken(token, userDetails));
    }

    @Test
    void validateTokenFalseWhenTokenBlacklisted() {
        String token = service.generateToken("u@t.com");
        when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(true);

        assertFalse(service.validateToken(token, userDetails));
        verify(userDetails, never()).getUsername();
    }

    @Test
    void validateTokenFalseWhenUserTokensBlacklisted() {
        String token = service.generateToken("u@t.com");
        when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(false);
        when(tokenBlacklistService.areUserTokensBlacklisted("u@t.com")).thenReturn(true);

        assertFalse(service.validateToken(token, userDetails));
    }

    @Test
    void validateTokenFalseForExpiredToken() {
        JwtService s = expiredService();
        String token = s.generateToken("u@t.com");
        when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(false);

        assertFalse(s.validateToken(token, userDetails));
    }

    @Test
    void validateTokenFalseForGarbageToken() {
        assertFalse(service.validateToken("not-a-jwt", userDetails));
    }

    // ---------- isTokenValid ----------

    @Test
    void isTokenValidTrueForMatchingUser() {
        String token = service.generateToken("u@t.com");
        User u = new User();
        u.setEmail("u@t.com");
        when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(false);

        assertTrue(service.isTokenValid(token, u));
    }

    @Test
    void isTokenValidFalseForMismatchedEmail() {
        String token = service.generateToken("u@t.com");
        User u = new User();
        u.setEmail("other@t.com");
        when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(false);

        assertFalse(service.isTokenValid(token, u));
    }

    // ---------- revoke ----------

    @Test
    void revokeTokenDelegatesToBlacklist() {
        service.revokeToken("tok");
        verify(tokenBlacklistService).blacklistToken("tok");
    }

    @Test
    void revokeAllUserTokensDelegates() {
        service.revokeAllUserTokens("u@t.com");
        verify(tokenBlacklistService).blacklistAllUserTokens("u@t.com");
    }

    @Test
    void revokeIsNullSafe() {
        JwtService s = new JwtService(null, null);
        ReflectionTestUtils.setField(s, "secretKey",
            "test-secret-key-that-is-long-enough-for-hs256-signing-0123456789");
        ReflectionTestUtils.setField(s, "expirationTime", 3_600_000L);
        ReflectionTestUtils.setField(s, "jwtAlgorithm", "HS256");
        s.init();
        assertDoesNotThrow(() -> s.revokeToken("tok"));
    }

    // ---------- isTokenExpiredSoon ----------

    @Test
    void isTokenExpiredSoonTrueForExpiredToken() {
        JwtService s = expiredService();
        String token = s.generateToken("u@t.com");
        assertTrue(s.isTokenExpiredSoon(token, 60));
    }

    @Test
    void isTokenExpiredSoonFalseForFreshToken() {
        String token = service.generateToken("u@t.com");
        assertFalse(service.isTokenExpiredSoon(token, 30));
    }

    @Test
    void isTokenExpiredSoonTrueForGarbage() {
        assertTrue(service.isTokenExpiredSoon("bad", 60));
    }

    // ---------- license tokens ----------

    @Test
    void generateLicenseTokenAndExtractClaims() {
        long exp = System.currentTimeMillis() / 1000 + 86400;
        String token = service.generateLicenseToken(
            "lic@t.com", 42L, "device-1", exp,
            List.of("feature-a"), List.of("module-x"), Map.of("bots", 5));

        assertEquals("42", service.extractUserId(token));
        assertEquals("lic@t.com", service.extractEmailFromLicense(token));
        assertEquals("device-1", service.extractDeviceId(token));
        assertEquals(exp, service.extractExpiration(token));
        assertEquals(List.of("feature-a"), service.extractFeatures(token));
        assertEquals(List.of("module-x"), service.extractModules(token));
        assertEquals(5, service.extractLimits(token).get("bots"));
    }

    @Test
    void licenseTokenWithoutDeviceId() {
        String token = service.generateLicenseToken(
            "lic@t.com", 1L, null, System.currentTimeMillis() / 1000 + 100,
            List.of(), List.of(), Map.of());
        assertNull(service.extractDeviceId(token));
    }

    @Test
    void verifyLicenseSignedByCloudTrue() {
        String token = service.generateLicenseToken(
            "lic@t.com", 1L, null, System.currentTimeMillis() / 1000 + 100,
            List.of(), List.of(), Map.of());
        assertTrue(service.verifyLicenseSignedByCloud(token));
    }

    @Test
    void verifyLicenseSignedByCloudFalseForAccessToken() {
        String token = service.generateToken("u@t.com"); // no signed_by claim
        assertFalse(service.verifyLicenseSignedByCloud(token));
    }

    @Test
    void isLicenseExpiredChecksExp() {
        String expired = service.generateLicenseToken(
            "lic@t.com", 1L, null, System.currentTimeMillis() / 1000 - 100,
            List.of(), List.of(), Map.of());
        assertTrue(service.isLicenseExpired(expired));

        String valid = service.generateLicenseToken(
            "lic@t.com", 1L, null, System.currentTimeMillis() / 1000 + 10000,
            List.of(), List.of(), Map.of());
        assertFalse(service.isLicenseExpired(valid));
    }

    // ---------- init / RS256 ----------

    @Test
    void initThrowsForRs256WithoutKeys() {
        JwtService s = new JwtService(null, null);
        ReflectionTestUtils.setField(s, "secretKey", "secret");
        ReflectionTestUtils.setField(s, "jwtAlgorithm", "RS256");
        ReflectionTestUtils.setField(s, "rsaPrivateKey", "");
        ReflectionTestUtils.setField(s, "rsaPublicKey", "");
        assertThrows(RuntimeException.class, s::init);
    }

    @Test
    void getPublicKeyPemThrowsForHs256() {
        assertThrows(IllegalStateException.class, service::getPublicKeyPem);
    }

    @Test
    void extractEmailThrowsInvalidTokenForGarbage() {
        assertThrows(InvalidTokenException.class, () -> service.extractEmail("garbage"));
    }
}
