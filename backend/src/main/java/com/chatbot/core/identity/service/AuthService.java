package com.chatbot.core.identity.service;

import com.chatbot.core.identity.constants.IdentityConstants;
import com.chatbot.core.identity.dto.*;
import com.chatbot.core.identity.exception.*;
import com.chatbot.core.identity.model.SystemRole;
import com.chatbot.core.identity.model.RefreshToken;
import com.chatbot.core.identity.repository.AuthRepository;
import com.chatbot.core.identity.security.CustomUserDetails;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.service.UserService;
import com.chatbot.shared.address.service.AddressService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuthService implements UserDetailsService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AddressService addressService;
    private final UserService userService;
    private final AuditService auditService;
    private final RateLimitService rateLimitService;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Cacheable(value = "userSessions", key = "#email")
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(IdentityConstants.USER_NOT_FOUND + ": " + email));

        return new CustomUserDetails(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public UserResponse register(RegisterRequest request) {
        
        // Validate password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException(IdentityConstants.PASSWORD_MISMATCH);
        }

        if (authRepository.existsByEmail(request.getEmail())) {
            auditService.logRegistrationFailure(request.getEmail(), "Email already exists");
            throw new EmailAlreadyExistsException(IdentityConstants.EMAIL_ALREADY_EXISTS);
        }

        boolean isFirstUser = authRepository.count() == 0;

        // tạo User entity
        User userEntity = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .systemRole(isFirstUser ? SystemRole.ADMIN : SystemRole.USER)
                .build();

        // tạo UserProfile với cascade relationship
        com.chatbot.core.user.profile.UserProfile userProfile = 
            com.chatbot.core.user.profile.UserProfile.builder()
                .user(userEntity) // Only set user, ID will be auto-generated
                .build();
        
        // Set bidirectional relationship
        userEntity.setProfile(userProfile);

        // save User entity - UserProfile sẽ được cascade save
        User savedUser = authRepository.save(userEntity);
        
        // tạo address (không ảnh hưởng transaction chính)
        try {
            addressService.getOrCreateUserAddress(com.chatbot.shared.address.model.OwnerType.USER, savedUser.getId());
        } catch (Exception e) {
            log.error("Không thể tạo địa chỉ trống cho user {}: {}", savedUser.getId(), e.getMessage());
        }

        String token = jwtService.generateToken(savedUser.getEmail());
        
        // Create refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

        UserDto userDto = new UserDto(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getSystemRole().name()
        );

        auditService.logRegistrationSuccess(savedUser.getEmail());
        
        return new UserResponse(token, refreshToken.getToken(), userDto);
    }

    public UserResponse login(LoginRequest request) {
        // Check rate limiting
        if (!rateLimitService.isLoginAllowed(request.getEmail())) {
            auditService.logLoginFailure(request.getEmail(), "Rate limit exceeded");
            throw new AuthenticationException("Quá số lần đăng nhập cho phép. Vui lòng thử lại sau " + 
                rateLimitService.getTimeUntilReset(request.getEmail()) + " phút");
        }

        User user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    auditService.logLoginFailure(request.getEmail(), "User not found");
                    throw new UserNotFoundException(IdentityConstants.USER_NOT_FOUND);
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            auditService.logLoginFailure(request.getEmail(), "Invalid password");
            throw new AuthenticationException(IdentityConstants.INVALID_CREDENTIALS);
        }

        // Reset rate limit on successful login
        rateLimitService.resetLoginAttempts(request.getEmail());
        
        String token = jwtService.generateToken(user.getEmail());
        
        // Create refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        
        UserDto userDto = new UserDto(user.getId(), user.getEmail(), user.getSystemRole().name());
        
        auditService.logLoginSuccess(user.getEmail());
        
        return new UserResponse(token, refreshToken.getToken(), userDto);
    }

    public UserResponse changePassword(String email, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException(IdentityConstants.PASSWORD_MISMATCH);
        }

        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(IdentityConstants.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            auditService.logLoginFailure(email, "Invalid old password for password change");
            throw new AuthenticationException(IdentityConstants.INVALID_OLD_PASSWORD);
        }

        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        authRepository.save(user);

        // Revoke all existing tokens for this user
        jwtService.revokeAllUserTokens(email);

        // --- TẠO LẠI TOKEN MỚI VÀ TRẢ VỀ GIỐNG LOGIN ---
        String newToken = jwtService.generateToken(user.getEmail());
        UserDto userDto = new UserDto(user.getId(), user.getEmail(), user.getSystemRole().name());
        
        auditService.logPasswordChange(email);
        
        return new UserResponse(newToken, userDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserDto changeRole(Long userId, SystemRole newRole) {
        // 1. Tìm người dùng cần đổi role
        User user = authRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        SystemRole oldRole = user.getSystemRole();

        // 2. Cập nhật role mới
        user.setSystemRole(newRole);
        authRepository.save(user);

        log.info("Admin đã thay đổi quyền của user {} thành {}", user.getEmail(), newRole);
        auditService.logRoleChange("admin", user.getEmail(), oldRole.name(), newRole.name());

        // 3. Trả về thông tin user sau khi cập nhật
        return new UserDto(user.getId(), user.getEmail(), user.getSystemRole().name());
    }
    
    /**
     * Logout user by revoking token
     */
    public void logoutByToken(String token) {
        jwtService.revokeToken(token);
        // Extract email from token for audit logging
        try {
            String email = jwtService.extractEmail(token);
            auditService.logTokenRevocation(email, "User logout");
        } catch (Exception e) {
            log.warn("Could not extract email from token during logout: {}", e.getMessage());
        }
    }
    
    /**
     * Get remaining login attempts for user
     */
    public int getRemainingLoginAttempts(String email) {
        return rateLimitService.getRemainingAttempts(email);
    }

    @Transactional
    public TokenRefreshResponse refreshToken(String refreshToken) {
        String newAccessToken = refreshTokenService.refreshAccessToken(refreshToken);
        
        // Get refresh token details to create new refresh token
        RefreshToken refresh = refreshTokenService.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token không hợp lệ"));
        
        // Create new refresh token (rotate for security)
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(refresh.getUser());
        
        return new TokenRefreshResponse(
                newAccessToken,
                newRefreshToken.getToken(),
                86400 // 24 hours in seconds
        );
    }

    @Transactional
    public void logout(String email) {
        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(IdentityConstants.USER_NOT_FOUND + ": " + email));
        
        // Revoke refresh token
        refreshTokenService.deleteByUserId(user.getId());
        
        // Audit log
        auditService.logSecurityEvent("USER_LOGOUT", email, "User logged out successfully");
    }
}
