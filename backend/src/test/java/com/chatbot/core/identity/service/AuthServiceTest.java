package com.chatbot.core.identity.service;

import com.chatbot.core.identity.dto.ChangePasswordRequest;
import com.chatbot.core.identity.dto.LoginRequest;
import com.chatbot.core.identity.dto.RegisterRequest;
import com.chatbot.core.identity.exception.AuthenticationException;
import com.chatbot.core.identity.exception.EmailAlreadyExistsException;
import com.chatbot.core.identity.exception.UserNotFoundException;
import com.chatbot.core.identity.exception.ValidationException;
import com.chatbot.core.identity.model.RefreshToken;
import com.chatbot.core.identity.model.SystemRole;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.AuthRepository;
import com.chatbot.core.user.service.UserService;
import com.chatbot.shared.address.service.AddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthRepository authRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AddressService addressService;
    @Mock private UserService userService;
    @Mock private AuditService auditService;
    @Mock private RateLimitService rateLimitService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private TwoFactorService twoFactorService;

    @InjectMocks
    private AuthService service;

    private User user(Long id, String email, String encodedPw) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setPassword(encodedPw);
        u.setSystemRole(SystemRole.USER);
        return u;
    }

    private RegisterRequest registerReq(String email, String pw, String confirm) {
        RegisterRequest r = new RegisterRequest();
        r.setEmail(email);
        r.setPassword(pw);
        r.setConfirmPassword(confirm);
        return r;
    }

    private LoginRequest loginReq(String email, String pw, String totp) {
        LoginRequest r = new LoginRequest();
        r.setEmail(email);
        r.setPassword(pw);
        r.setTotpCode(totp);
        return r;
    }

    private RefreshToken refreshToken(Long userId, String token) {
        return RefreshToken.builder().userId(userId).token(token)
            .expiryDate(java.time.Instant.now().plusSeconds(3600)).build();
    }

    // ---------- loadUserByUsername ----------

    @Test
    void loadUserByUsernameReturnsUserDetails() {
        when(authRepository.findByEmail("u@t.com")).thenReturn(Optional.of(user(1L, "u@t.com", "hash")));
        UserDetails details = service.loadUserByUsername("u@t.com");
        assertNotNull(details);
        assertEquals("u@t.com", details.getUsername());
    }

    @Test
    void loadUserByUsernameThrowsWhenMissing() {
        when(authRepository.findByEmail("ghost@t.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> service.loadUserByUsername("ghost@t.com"));
    }

    // ---------- register ----------

    @Test
    void registerRejectsPasswordMismatch() {
        assertThrows(ValidationException.class,
            () -> service.register(registerReq("u@t.com", "pass1", "pass2")));
        verifyNoInteractions(authRepository);
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(authRepository.existsByEmail("u@t.com")).thenReturn(true);
        assertThrows(EmailAlreadyExistsException.class,
            () -> service.register(registerReq("u@t.com", "pass", "pass")));
        verify(auditService).logRegistrationFailure("u@t.com", "Email already exists");
    }

    @Test
    void registerFirstUserBecomesAdmin() {
        when(authRepository.existsByEmail("first@t.com")).thenReturn(false);
        when(authRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(authRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateToken("first@t.com")).thenReturn("jwt");
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(refreshToken(1L, "refresh"));

        var resp = service.register(registerReq("first@t.com", "pass", "pass"));

        assertEquals("jwt", resp.getToken());
        assertEquals("refresh", resp.getRefreshToken());
        assertEquals("ADMIN", resp.getUser().getSystemRole());
        verify(auditService).logRegistrationSuccess("first@t.com");
    }

    @Test
    void registerNormalUserGetsUserRole() {
        when(authRepository.existsByEmail("u@t.com")).thenReturn(false);
        when(authRepository.count()).thenReturn(5L);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(authRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });
        when(jwtService.generateToken("u@t.com")).thenReturn("jwt");
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(refreshToken(2L, "refresh"));

        var resp = service.register(registerReq("u@t.com", "pass", "pass"));

        assertEquals("USER", resp.getUser().getSystemRole());
    }

    @Test
    void registerFailsWhenAddressCreationFails() {
        when(authRepository.existsByEmail("u@t.com")).thenReturn(false);
        when(authRepository.count()).thenReturn(5L);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(authRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });
        doThrow(new RuntimeException("addr fail")).when(addressService)
            .getOrCreateUserAddress(any(), anyLong());

        assertThrows(com.chatbot.core.identity.exception.IdentityException.class,
            () -> service.register(registerReq("u@t.com", "pass", "pass")));
    }

    // ---------- login ----------

    @Test
    void loginBlockedByRateLimit() {
        when(rateLimitService.isLoginAllowed("u@t.com")).thenReturn(false);
        when(rateLimitService.getTimeUntilReset("u@t.com")).thenReturn(10L);

        AuthenticationException ex = assertThrows(AuthenticationException.class,
            () -> service.login(loginReq("u@t.com", "pass", null)));
        assertTrue(ex.getMessage().contains("Too many login attempts"));
        verify(auditService).logLoginFailure("u@t.com", "Rate limit exceeded");
    }

    @Test
    void loginThrowsWhenUserNotFound() {
        when(rateLimitService.isLoginAllowed("ghost@t.com")).thenReturn(true);
        when(authRepository.findByEmail("ghost@t.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
            () -> service.login(loginReq("ghost@t.com", "pass", null)));
        verify(auditService).logLoginFailure("ghost@t.com", "User not found");
    }

    @Test
    void loginThrowsOnWrongPassword() {
        when(rateLimitService.isLoginAllowed("u@t.com")).thenReturn(true);
        when(authRepository.findByEmail("u@t.com")).thenReturn(Optional.of(user(1L, "u@t.com", "hash")));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThrows(AuthenticationException.class,
            () -> service.login(loginReq("u@t.com", "wrong", null)));
        verify(auditService).logLoginFailure("u@t.com", "Invalid password");
    }

    @Test
    void loginRequiresTotpWhen2faEnabled() {
        User u = user(1L, "u@t.com", "hash");
        when(rateLimitService.isLoginAllowed("u@t.com")).thenReturn(true);
        when(authRepository.findByEmail("u@t.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("pass", "hash")).thenReturn(true);
        when(twoFactorService.isEnabled(u)).thenReturn(true);

        AuthenticationException ex = assertThrows(AuthenticationException.class,
            () -> service.login(loginReq("u@t.com", "pass", null)));
        assertEquals("2FA_REQUIRED", ex.getMessage());
    }

    @Test
    void loginRejectsInvalidTotpCode() {
        User u = user(1L, "u@t.com", "hash");
        when(rateLimitService.isLoginAllowed("u@t.com")).thenReturn(true);
        when(authRepository.findByEmail("u@t.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("pass", "hash")).thenReturn(true);
        when(twoFactorService.isEnabled(u)).thenReturn(true);
        when(twoFactorService.verifyCode(any(), eq("000000"))).thenReturn(false);

        AuthenticationException ex = assertThrows(AuthenticationException.class,
            () -> service.login(loginReq("u@t.com", "pass", "000000")));
        assertTrue(ex.getMessage().contains("Invalid 2FA code"));
    }

    @Test
    void loginSuccessWith2fa() {
        User u = user(1L, "u@t.com", "hash");
        when(rateLimitService.isLoginAllowed("u@t.com")).thenReturn(true);
        when(authRepository.findByEmail("u@t.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("pass", "hash")).thenReturn(true);
        when(twoFactorService.isEnabled(u)).thenReturn(true);
        when(twoFactorService.verifyCode(any(), eq("123456"))).thenReturn(true);
        when(jwtService.generateToken("u@t.com")).thenReturn("jwt");
        when(refreshTokenService.createRefreshToken(u)).thenReturn(refreshToken(1L, "refresh"));

        var resp = service.login(loginReq("u@t.com", "pass", "123456"));

        assertEquals("jwt", resp.getToken());
        assertEquals("refresh", resp.getRefreshToken());
        verify(rateLimitService).resetLoginAttempts("u@t.com");
        verify(auditService).logLoginSuccess("u@t.com");
    }

    @Test
    void loginSuccessWithout2fa() {
        User u = user(1L, "u@t.com", "hash");
        when(rateLimitService.isLoginAllowed("u@t.com")).thenReturn(true);
        when(authRepository.findByEmail("u@t.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("pass", "hash")).thenReturn(true);
        when(twoFactorService.isEnabled(u)).thenReturn(false);
        when(jwtService.generateToken("u@t.com")).thenReturn("jwt");
        when(refreshTokenService.createRefreshToken(u)).thenReturn(refreshToken(1L, "refresh"));

        var resp = service.login(loginReq("u@t.com", "pass", null));

        assertEquals("jwt", resp.getToken());
        verify(rateLimitService).resetLoginAttempts("u@t.com");
    }

    // ---------- changePassword ----------

    @Test
    void changePasswordRejectsMismatch() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setNewPassword("new1");
        req.setConfirmPassword("new2");
        assertThrows(ValidationException.class, () -> service.changePassword("u@t.com", req));
    }

    @Test
    void changePasswordRejectsWrongOldPassword() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("old");
        req.setNewPassword("new");
        req.setConfirmPassword("new");
        when(authRepository.findByEmail("u@t.com")).thenReturn(Optional.of(user(1L, "u@t.com", "hash")));
        when(passwordEncoder.matches("old", "hash")).thenReturn(false);

        assertThrows(AuthenticationException.class, () -> service.changePassword("u@t.com", req));
    }

    @Test
    void changePasswordSuccessRevokesAndReturnsNewToken() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("old");
        req.setNewPassword("new");
        req.setConfirmPassword("new");
        User u = user(1L, "u@t.com", "hash");
        when(authRepository.findByEmail("u@t.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("old", "hash")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("newhash");
        when(authRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken("u@t.com")).thenReturn("new-jwt");

        var resp = service.changePassword("u@t.com", req);

        assertEquals("new-jwt", resp.getToken());
        assertNull(resp.getRefreshToken()); // changePassword returns UserResponse(token, userDto)
        verify(jwtService).revokeAllUserTokens("u@t.com");
        verify(auditService).logPasswordChange("u@t.com");
    }

    // ---------- changeRole ----------

    @Test
    void changeRoleThrowsWhenUserMissing() {
        when(authRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class,
            () -> service.changeRole(99L, SystemRole.ADMIN));
    }

    @Test
    void changeRoleUpdatesAndAudits() {
        User u = user(1L, "u@t.com", "hash");
        u.setSystemRole(SystemRole.USER);
        when(authRepository.findById(1L)).thenReturn(Optional.of(u));
        when(authRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var dto = service.changeRole(1L, SystemRole.ADMIN);

        assertEquals("ADMIN", dto.getSystemRole());
        verify(auditService).logRoleChange(eq("admin"), eq("u@t.com"), eq("USER"), eq("ADMIN"));
    }

    // ---------- logout / logoutByToken / refreshToken ----------

    @Test
    void logoutDeletesRefreshTokens() {
        when(authRepository.findByEmail("u@t.com")).thenReturn(Optional.of(user(1L, "u@t.com", "hash")));
        service.logout("u@t.com");
        verify(refreshTokenService).deleteByUserId(1L);
        verify(auditService).logSecurityEvent(eq("USER_LOGOUT"), eq("u@t.com"), anyString());
    }

    @Test
    void logoutThrowsWhenUserMissing() {
        when(authRepository.findByEmail("ghost@t.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> service.logout("ghost@t.com"));
    }

    @Test
    void logoutByTokenRevokesAndAudits() {
        when(jwtService.extractEmail("tok")).thenReturn("u@t.com");
        service.logoutByToken("tok");
        verify(jwtService).revokeToken("tok");
        verify(auditService).logTokenRevocation("u@t.com", "User logout");
    }

    @Test
    void logoutByTokenStillRevokesWhenEmailExtractionFails() {
        when(jwtService.extractEmail("bad")).thenThrow(new RuntimeException("invalid"));
        service.logoutByToken("bad");
        verify(jwtService).revokeToken("bad");
        verify(auditService, never()).logTokenRevocation(any(), any());
    }

    @Test
    void refreshTokenRotatesTokens() {
        when(refreshTokenService.refreshAccessToken("old-refresh")).thenReturn("new-access");
        when(refreshTokenService.findByToken("old-refresh")).thenReturn(Optional.of(refreshToken(1L, "old-refresh")));
        when(refreshTokenService.createRefreshToken(1L)).thenReturn(refreshToken(1L, "new-refresh"));

        var resp = service.refreshToken("old-refresh");

        assertEquals("new-access", resp.getAccessToken());
        assertEquals("new-refresh", resp.getRefreshToken());
        assertEquals(86400, resp.getExpiresIn());
    }

    @Test
    void getRemainingLoginAttemptsDelegates() {
        when(rateLimitService.getRemainingAttempts("u@t.com")).thenReturn(3);
        assertEquals(3, service.getRemainingLoginAttempts("u@t.com"));
    }
}
