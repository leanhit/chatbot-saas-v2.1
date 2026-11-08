package com.chatbot.core.identity.service;

import com.chatbot.core.identity.exception.InvalidTokenException;
import com.chatbot.core.identity.model.RefreshToken;
import com.chatbot.core.identity.repository.RefreshTokenRepository;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtService jwtService;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "refreshTokenExpirationMs", 2_592_000_000L); // 30 days
    }

    private User user(Long id, String email) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        return u;
    }

    // ---------- createRefreshToken ----------

    @Test
    void createRefreshTokenForUserDeletesOldAndSaves() {
        User u = user(1L, "u@t.com");
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = service.createRefreshToken(u);

        verify(refreshTokenRepository).deleteByUserId(1L);
        assertEquals(1L, result.getUserId());
        assertNotNull(result.getToken());
        assertTrue(result.getExpiryDate().isAfter(Instant.now().plus(29, ChronoUnit.DAYS)));
    }

    @Test
    void createRefreshTokenForUserId() {
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = service.createRefreshToken(5L);

        verify(refreshTokenRepository).deleteByUserId(5L);
        assertEquals(5L, result.getUserId());
    }

    // ---------- verifyExpiration ----------

    @Test
    void verifyExpirationReturnsValidToken() {
        RefreshToken t = RefreshToken.builder()
            .userId(1L).token("tok")
            .expiryDate(Instant.now().plus(1, ChronoUnit.HOURS)).build();
        assertSame(t, service.verifyExpiration(t));
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void verifyExpirationDeletesAndThrowsWhenExpired() {
        RefreshToken t = RefreshToken.builder()
            .userId(1L).token("tok")
            .expiryDate(Instant.now().minus(1, ChronoUnit.HOURS)).build();

        assertThrows(InvalidTokenException.class, () -> service.verifyExpiration(t));
        verify(refreshTokenRepository).delete(t);
    }

    // ---------- refreshAccessToken ----------

    @Test
    void refreshAccessTokenGeneratesNewToken() {
        RefreshToken t = RefreshToken.builder()
            .userId(1L).token("refresh-tok")
            .expiryDate(Instant.now().plus(1, ChronoUnit.HOURS)).build();
        when(refreshTokenRepository.findByToken("refresh-tok")).thenReturn(Optional.of(t));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, "u@t.com")));
        when(jwtService.generateToken("u@t.com")).thenReturn("new-access-token");

        assertEquals("new-access-token", service.refreshAccessToken("refresh-tok"));
    }

    @Test
    void refreshAccessTokenThrowsWhenTokenNotFound() {
        when(refreshTokenRepository.findByToken("bad")).thenReturn(Optional.empty());
        assertThrows(InvalidTokenException.class, () -> service.refreshAccessToken("bad"));
    }

    @Test
    void refreshAccessTokenThrowsWhenTokenExpired() {
        RefreshToken t = RefreshToken.builder()
            .userId(1L).token("old")
            .expiryDate(Instant.now().minus(1, ChronoUnit.HOURS)).build();
        when(refreshTokenRepository.findByToken("old")).thenReturn(Optional.of(t));

        assertThrows(InvalidTokenException.class, () -> service.refreshAccessToken("old"));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void refreshAccessTokenThrowsWhenUserMissing() {
        RefreshToken t = RefreshToken.builder()
            .userId(99L).token("tok")
            .expiryDate(Instant.now().plus(1, ChronoUnit.HOURS)).build();
        when(refreshTokenRepository.findByToken("tok")).thenReturn(Optional.of(t));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> service.refreshAccessToken("tok"));
    }

    // ---------- deleteByUserId / findByToken ----------

    @Test
    void deleteByUserIdDelegates() {
        service.deleteByUserId(3L);
        verify(refreshTokenRepository).deleteByUserId(3L);
    }

    @Test
    void findByTokenDelegates() {
        RefreshToken t = RefreshToken.builder().userId(1L).token("x").build();
        when(refreshTokenRepository.findByToken("x")).thenReturn(Optional.of(t));
        assertTrue(service.findByToken("x").isPresent());
    }
}
