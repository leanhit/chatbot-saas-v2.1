package com.chatbot.modules.identity.controller;

import com.chatbot.modules.auth.dto.UserDto;
import com.chatbot.modules.identity.dto.LoginRequest;
import com.chatbot.modules.identity.dto.LoginResponse;
import com.chatbot.modules.identity.dto.RefreshRequest;
import com.chatbot.modules.identity.dto.RefreshResponse;
import com.chatbot.modules.identity.dto.RegisterRequest;
import com.chatbot.modules.identity.dto.RegisterResponse;
import com.chatbot.modules.identity.model.User;
import com.chatbot.modules.identity.repository.UserRepository;
import com.chatbot.modules.identity.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Identity Hub controller for authentication endpoints
 * TODO: Add rate limiting
 * TODO: Add request/response logging
 * TODO: Add API versioning
 */
@RestController
@RequestMapping("/api/identity")
@RequiredArgsConstructor
@Slf4j
public class IdentityController {
    
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    
    /**
     * Register new user
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("REGISTER request received for email: {}", request.getEmail());
        
        RegisterResponse response = authenticationService.register(request);
        log.info("Registration successful for email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Authenticate user and return tokens
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());
        
        try {
            LoginResponse response = authenticationService.authenticate(request);
            log.info("Login successful for email: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            log.warn("Login failed for email: {} - {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            log.error("IDENTITY LOGIN FAILED", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Refresh access token using refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        log.debug("Token refresh request received");
        
        try {
            RefreshResponse response = authenticationService.refreshTokens(request);
            log.debug("Token refresh successful");
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            log.warn("Token refresh failed - {}", e.getMessage());
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            log.error("Token refresh error", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Get current user info (authenticated endpoint)
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof com.chatbot.modules.auth.security.CustomUserDetails cud) {
            User user;
            if (cud.getAuth() != null) {
                // Legacy auth path - convert to User
                user = userRepository.findByEmail(cud.getAuth().getEmail())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
            } else {
                // Identity user path - direct User object
                user = cud.getUser();
            }

            return ResponseEntity.ok(
                UserDto.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .systemRole(user.getStatus().name())
                    .locale("vi")
                    .build()
            );
        } else {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unsupported principal type: " + principal.getClass()
            );
        }
    }

    /**
     * Validate token and return user info (for internal services)
     * TODO: Add proper authentication for this endpoint
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }
        
        String token = authHeader.substring(7);
        
        try {
            var user = authenticationService.validateTokenAndGetUser(token);
            var tenantIds = authenticationService.getUserTenantIds(token);
            
            // Create response object properly
            var response = new Object() {
                public final Long userId = user.getId();
                public final String email = user.getEmail();
                public final List<UUID> tenantIds = authenticationService.getUserTenantIds(token);
                public final String status = user.getStatus().name();
            };
            
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            log.warn("Token validation failed - {}", e.getMessage());
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            log.error("Token validation error", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(new Object() {
            public final String status = "healthy";
            public final String service = "identity-hub";
            public final long timestamp = System.currentTimeMillis();
        });
    }
}
