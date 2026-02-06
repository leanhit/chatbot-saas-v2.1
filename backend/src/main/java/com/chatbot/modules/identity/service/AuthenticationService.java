package com.chatbot.modules.identity.service;

import com.chatbot.modules.identity.dto.LoginRequest;
import com.chatbot.modules.identity.dto.LoginResponse;
import com.chatbot.modules.identity.dto.RefreshRequest;
import com.chatbot.modules.identity.dto.RefreshResponse;
import com.chatbot.modules.identity.dto.RegisterRequest;
import com.chatbot.modules.identity.dto.RegisterResponse;
import com.chatbot.modules.auth.dto.UserDto;
import com.chatbot.modules.identity.exception.DuplicateEmailException;
import com.chatbot.modules.identity.model.Credential;
import com.chatbot.modules.identity.model.User;
import com.chatbot.modules.identity.model.UserStatus;
import com.chatbot.modules.identity.repository.CredentialRepository;
import com.chatbot.modules.identity.repository.UserRepository;
import com.chatbot.modules.identity.repository.UserTenantRepository;
import com.chatbot.modules.tenant.core.dto.CreateTenantRequest;
import com.chatbot.modules.tenant.core.dto.TenantResponse;
import com.chatbot.modules.tenant.core.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final UserTenantRepository userTenantRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final TenantService tenantService;

    // =========================
    // REGISTER
    // =========================
    
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        log.info("REGISTER: Starting registration for email: {}", request.getEmail());
        
        // 1. Check if user already exists in identity_users
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("REGISTER: Email already exists in identity_users: {}", request.getEmail());
            throw new DuplicateEmailException("Email already exists: " + request.getEmail());
        }
        
        // 2. Create identity_user with credential
        String passwordHash = passwordEncoder.encode(request.getPassword());
        
        Credential credential = Credential.builder()
                .password(passwordHash)
                .source("IDENTITY")
                .provider("LOCAL")
                .isActive(true)
                .build();
        
        User user = User.builder()
                .email(request.getEmail())
                .status(UserStatus.ACTIVE)
                .credential(credential)
                .build();
        
        // Explicitly set BOTH sides of the relationship for @MapsId
        credential.setUser(user);
        user.setCredential(credential);
        
        User savedUser = userRepository.save(user);
        log.info("REGISTER: Created identity_user with ID: {} for email: {}", savedUser.getId(), savedUser.getEmail());
        log.info("REGISTER: Created user_credentials for user ID: {} email: {}", savedUser.getId(), savedUser.getEmail());
        
        // 3. Create default tenant for new user
        createDefaultTenantForNewUser(savedUser);
        
        return RegisterResponse.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .message("Registration successful")
                .build();
    }

    // =========================
    // LOGIN
    // =========================
    @Transactional(readOnly = true)
    public LoginResponse authenticate(LoginRequest request) {
        log.info("AUTHENTICATE called for email={}", request.getEmail());
        log.info("LOGIN 500 DEBUG: Starting authentication attempt for email: {}", request.getEmail());

        // 1. Try load User from Identity Hub users table
        log.info("LOGIN 500 DEBUG: Loading user from users table for email: {}", request.getEmail());
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElse(null);

        // 2. IF user exists in users table
        if (user != null) {
            log.info("LOGIN 500 DEBUG: USER FOUND in users table - ID: {} email: {}", user.getId(), user.getEmail());
            
            return authenticateExistingUser(user, request);
        }

        // 3. IF user NOT exists in users table - REJECT (no legacy fallback)
        log.info("LOGIN 500 DEBUG: USER NOT FOUND in users table - rejecting login for email: {}", request.getEmail());
        
        throw new BadCredentialsException("Invalid email or password");
        
        // Legacy fallback commented out:
        // return authenticateLegacyOnlyUser(request);
    }

    /**
     * Authenticate existing user from users table
     * Handles both identity login and legacy fallback with migration
     */
    private LoginResponse authenticateExistingUser(User user, LoginRequest request) {
        try {
            log.info("LOGIN 500 DEBUG: authenticateExistingUser - Starting for user ID: {} email: {}", user.getId(), user.getEmail());
            
            // 2. Check for Identity Hub credential
            log.info("LOGIN 500 DEBUG: Loading credential for user ID: {}", user.getId());
            Credential credential = credentialRepository
                    .findByUserId(user.getId())
                    .orElse(null);

            // =========================
            // PRIMARY: IDENTITY HUB LOGIN
            // =========================
            if (credential != null) {
                log.info("LOGIN 500 DEBUG: CREDENTIAL FOUND for user ID: {} email: {}", user.getId(), user.getEmail());

                log.info("LOGIN 500 DEBUG: Checking account status for user ID: {}", user.getId());
                if (!Boolean.TRUE.equals(credential.getIsActive())) {
                    log.warn("LOGIN 500 DEBUG: Account disabled for user ID: {} email: {}", user.getId(), user.getEmail());
                    throw new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Account disabled"
                    );
                }

                if (credential.getLockedUntil() != null &&
                    credential.getLockedUntil().isAfter(LocalDateTime.now())) {
                    log.warn("LOGIN 500 DEBUG: Account locked for user ID: {} email: {} until: {}", 
                            user.getId(), user.getEmail(), credential.getLockedUntil());
                    throw new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Account locked"
                    );
                }

                log.info("LOGIN 500 DEBUG: Starting password verification for user ID: {}", user.getId());
                boolean matches = passwordEncoder.matches(
                        request.getPassword(),
                        credential.getPassword()
                );

                log.info("LOGIN 500 DEBUG: Password verification result={} for user ID: {} email: {}", 
                        matches, user.getId(), user.getEmail());

                if (!matches) {
                    log.warn("LOGIN 500 DEBUG: Password mismatch for user ID: {} email: {}", user.getId(), user.getEmail());
                    throw new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Invalid email or password"
                    );
                }

                log.info("LOGIN 500 DEBUG: Identity login SUCCESS for user ID: {} email: {}", user.getId(), user.getEmail());
                return generateTokensForUser(user);
            }

            // =========================
            // LEGACY FALLBACK - DISABLED
            // =========================
            log.warn("LOGIN 500 DEBUG: CREDENTIAL NOT FOUND for user ID: {} email: {} - rejecting login", 
                    user.getId(), user.getEmail());

            throw new BadCredentialsException("Invalid email or password");
            
            // Legacy fallback commented out:
            // return legacyAuthenticateAndMigrate(user, request);
            
        } catch (ResponseStatusException e) {
            // Re-throw ResponseStatusException (already 401)
            throw e;
        } catch (BadCredentialsException e) {
            // Re-throw BadCredentialsException
            throw e;
        }
    }

    /**
     * Authenticate legacy-only user (exists only in auth table) - DISABLED
     * Creates Identity Hub user and credential on successful authentication
     */
    private LoginResponse authenticateLegacyOnlyUser(LoginRequest request) {
        throw new BadCredentialsException("Legacy authentication disabled");
    }

    /**
     * LEGACY AUTHENTICATION WITH AUTO-MIGRATION - DISABLED
     * 
     * This method is disabled because legacy dependencies have been removed.
     * Identity Hub now operates independently without legacy auth support.
     */
    private LoginResponse legacyAuthenticateAndMigrate(User user, LoginRequest request) {
        throw new BadCredentialsException("Legacy authentication disabled");
    }

    private LoginResponse generateTokensForUser(User user) {
        log.info("LOGIN 500 DEBUG: Identity login SUCCESS for user ID: {} email: {}", user.getId(), user.getEmail());
        
        List<UUID> tenantIds = userTenantRepository.findActiveTenantIdsByUserId(user.getId());
        log.info("User {} tenants={}", user.getId(), tenantIds);

        LoginResponse tokenResponse = tokenService.generateTokens(user.getId(), tenantIds);
        
        // Add user info to response
        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .systemRole(user.getStatus().name())
                .locale("vi")
                .build();
        
        return LoginResponse.of(
            tokenResponse.getToken(),
            tokenResponse.getRefreshToken(),
            userDto,
            tokenResponse.getExpiresIn()
        );
    }

    // =========================
    // REFRESH TOKEN
    // =========================
    @Transactional
    public RefreshResponse refreshTokens(RefreshRequest request) {

        Long userId = tokenService.extractUserId(request.getRefreshToken());

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Invalid refresh token"
                        )
                );

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User inactive"
            );
        }

        List<UUID> tenantIds =
                userTenantRepository.findActiveTenantIdsByUserId(user.getId());

        return tokenService.refreshTokens(request.getRefreshToken(), tenantIds);
    }

    public User validateTokenAndGetUser(String token) {
        Long userId = tokenService.extractUserId(token);
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Invalid token"
                        )
                );
    }

    public List<UUID> getUserTenantIds(String token) {
        return tokenService.extractTenantIds(token);
    }

    /**
     * Create default tenant for newly registered user
     * This method is called after successful user registration
     */
    private void createDefaultTenantForNewUser(User user) {
        try {
            log.info("Creating default tenant for new user: {} (ID: {})", user.getEmail(), user.getId());
            
            // Generate default tenant name based on user email
            String defaultTenantName = generateDefaultTenantName(user.getEmail());
            
            // Create tenant request
            CreateTenantRequest tenantRequest = CreateTenantRequest.builder()
                    .name(defaultTenantName)
                    .build();
            
            // Create tenant using TenantService with explicit user context
            // No SecurityContext setup needed - we pass userId and userEmail directly
            TenantResponse tenantResponse = tenantService.createTenantForUser(
                tenantRequest, 
                UUID.nameUUIDFromBytes(("user_" + user.getId()).getBytes()), 
                user.getEmail()
            );
            
            log.info("Successfully created default tenant '{}' (ID: {}) for user: {} (ID: {})", 
                    tenantResponse.getName(), tenantResponse.getId(), user.getEmail(), user.getId());
            
        } catch (Exception e) {
            log.error("Failed to create default tenant for user: {} (ID: {})", user.getEmail(), user.getId(), e);
            // Don't throw exception to avoid breaking registration flow
            // Tenant creation failure should not prevent user registration
        }
    }
    
    /**
     * Generate default tenant name from user email
     */
    private String generateDefaultTenantName(String email) {
        // Extract username from email (everything before @)
        String username = email.split("@")[0];
        return username + "'s Workspace";
    }
}
