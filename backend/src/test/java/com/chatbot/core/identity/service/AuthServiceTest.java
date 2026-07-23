package com.chatbot.core.identity.service;

import com.chatbot.core.identity.dto.*;
import com.chatbot.core.identity.exception.*;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AddressService addressService;

    @Mock
    private UserService userService;

    @Mock
    private AuditService auditService;

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setSystemRole(SystemRole.USER);
    }

    @Test
    void register_Success() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("Password123!");
        request.setConfirmPassword("Password123!");

        when(authRepository.existsByEmail(anyString())).thenReturn(false);
        when(authRepository.count()).thenReturn(1L); // Not first user
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(authRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(anyString())).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(createMockRefreshToken());

        // Act
        UserResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertNotNull(response.getRefreshToken());
        verify(authRepository, times(1)).save(any(User.class));
        verify(auditService, times(1)).logRegistrationSuccess(anyString());
    }

    @Test
    void register_PasswordMismatch() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("Password123!");
        request.setConfirmPassword("DifferentPassword123!");

        // Act & Assert
        assertThrows(ValidationException.class, () -> authService.register(request));
        verify(authRepository, never()).save(any(User.class));
    }

    @Test
    void register_EmailAlreadyExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("Password123!");
        request.setConfirmPassword("Password123!");

        when(authRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));
        verify(auditService, times(1)).logRegistrationFailure(anyString(), anyString());
    }

    @Test
    void register_FirstUserGetsAdminRole() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("firstuser@example.com");
        request.setPassword("Password123!");
        request.setConfirmPassword("Password123!");

        when(authRepository.existsByEmail(anyString())).thenReturn(false);
        when(authRepository.count()).thenReturn(0L); // First user
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(authRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtService.generateToken(anyString())).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(createMockRefreshToken());

        // Act
        UserResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        verify(authRepository).save(argThat(user -> SystemRole.ADMIN.equals(user.getSystemRole())));
    }

    @Test
    void login_Success() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123!");

        when(rateLimitService.isLoginAllowed(anyString())).thenReturn(true);
        when(authRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(anyString())).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(createMockRefreshToken());

        // Act
        UserResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        verify(rateLimitService, times(1)).resetLoginAttempts(anyString());
        verify(auditService, times(1)).logLoginSuccess(anyString());
    }

    @Test
    void login_RateLimitExceeded() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123!");

        when(rateLimitService.isLoginAllowed(anyString())).thenReturn(false);
        when(rateLimitService.getTimeUntilReset(anyString())).thenReturn(5L);

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> authService.login(request));
        verify(auditService, times(1)).logLoginFailure(anyString(), anyString());
    }

    @Test
    void login_UserNotFound() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("Password123!");

        when(rateLimitService.isLoginAllowed(anyString())).thenReturn(true);
        when(authRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> authService.login(request));
        verify(auditService, times(1)).logLoginFailure(anyString(), anyString());
    }

    @Test
    void login_InvalidPassword() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("WrongPassword");

        when(rateLimitService.isLoginAllowed(anyString())).thenReturn(true);
        when(authRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> authService.login(request));
        verify(auditService, times(1)).logLoginFailure(anyString(), anyString());
    }

    @Test
    void changePassword_Success() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("OldPassword123!");
        request.setNewPassword("NewPassword123!");
        request.setConfirmPassword("NewPassword123!");

        when(authRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(authRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(anyString())).thenReturn("new-jwt-token");

        // Act
        UserResponse response = authService.changePassword("test@example.com", request);

        // Assert
        assertNotNull(response);
        assertEquals("new-jwt-token", response.getToken());
        verify(jwtService, times(1)).revokeAllUserTokens(anyString());
        verify(auditService, times(1)).logPasswordChange(anyString());
    }

    @Test
    void changePassword_PasswordMismatch() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("OldPassword123!");
        request.setNewPassword("NewPassword123!");
        request.setConfirmPassword("DifferentPassword123!");

        // Act & Assert
        assertThrows(ValidationException.class, () -> 
            authService.changePassword("test@example.com", request));
    }

    @Test
    void changePassword_InvalidOldPassword() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("WrongOldPassword");
        request.setNewPassword("NewPassword123!");
        request.setConfirmPassword("NewPassword123!");

        when(authRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> 
            authService.changePassword("test@example.com", request));
    }

    @Test
    void logoutByToken_Success() {
        // Arrange
        String token = "valid-jwt-token";
        when(jwtService.extractEmail(anyString())).thenReturn("test@example.com");

        // Act
        authService.logoutByToken(token);

        // Assert
        verify(jwtService, times(1)).revokeToken(token);
        verify(auditService, times(1)).logTokenRevocation(anyString(), anyString());
    }

    @Test
    void logoutByToken_InvalidToken() {
        // Arrange
        String token = "invalid-token";
        when(jwtService.extractEmail(anyString())).thenThrow(new RuntimeException("Invalid token"));

        // Act
        authService.logoutByToken(token);

        // Assert
        verify(jwtService, times(1)).revokeToken(token);
        verify(auditService, never()).logTokenRevocation(anyString(), anyString());
    }

    @Test
    void getRemainingLoginAttempts_Success() {
        // Arrange
        when(rateLimitService.getRemainingAttempts(anyString())).thenReturn(5);

        // Act
        int attempts = authService.getRemainingLoginAttempts("test@example.com");

        // Assert
        assertEquals(5, attempts);
    }

    @Test
    void refreshToken_Success() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        RefreshToken mockRefreshToken = createMockRefreshToken();
        
        when(refreshTokenService.refreshAccessToken(anyString())).thenReturn("new-access-token");
        when(refreshTokenService.findByToken(anyString())).thenReturn(Optional.of(mockRefreshToken));
        when(refreshTokenService.createRefreshToken(anyLong())).thenReturn(mockRefreshToken);

        // Act
        TokenRefreshResponse response = authService.refreshToken(refreshToken);

        // Assert
        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals(86400, response.getExpiresIn());
    }

    @Test
    void refreshToken_InvalidToken() {
        // Arrange
        String refreshToken = "invalid-refresh-token";
        
        when(refreshTokenService.refreshAccessToken(anyString())).thenReturn("new-access-token");
        when(refreshTokenService.findByToken(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> 
            authService.refreshToken(refreshToken));
    }

    @Test
    void logout_Success() {
        // Arrange
        when(authRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        doNothing().when(refreshTokenService).deleteByUserId(anyLong());

        // Act
        authService.logout("test@example.com");

        // Assert
        verify(refreshTokenService, times(1)).deleteByUserId(testUser.getId());
        verify(auditService, times(1)).logSecurityEvent(anyString(), anyString(), anyString());
    }

    @Test
    void logout_UserNotFound() {
        // Arrange
        when(authRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> 
            authService.logout("nonexistent@example.com"));
    }

    @Test
    void loadUserByUsername_Success() {
        // Arrange
        when(authRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act
        var userDetails = authService.loadUserByUsername("test@example.com");

        // Assert
        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_UserNotFound() {
        // Arrange
        when(authRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> 
            authService.loadUserByUsername("nonexistent@example.com"));
    }

    private RefreshToken createMockRefreshToken() {
        RefreshToken token = new RefreshToken();
        token.setId(1L);
        token.setToken("refresh-token");
        token.setUserId(1L);
        return token;
    }
}
