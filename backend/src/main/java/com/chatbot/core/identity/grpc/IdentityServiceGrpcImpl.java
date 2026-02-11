package com.chatbot.core.identity.grpc;

import com.chatbot.core.identity.model.Auth;
import com.chatbot.core.identity.repository.AuthRepository;
import com.chatbot.core.identity.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * gRPC Service Implementation for Identity Hub
 * This will be fully implemented after protobuf compilation
 * For now, this serves as a placeholder structure
 */
@Service
@Slf4j
public class IdentityServiceGrpcImpl {
    
    @Autowired
    private AuthRepository authRepository;
    
    @Autowired
    private JwtService jwtService;

    /**
     * Validate JWT token for inter-hub communication
     */
    public boolean validateToken(String token) {
        try {
            String email = jwtService.extractUsername(token);
            Optional<Auth> userOpt = authRepository.findByEmail(email);
            return userOpt.isPresent() && jwtService.isTokenValid(token, userOpt.get());
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get user profile information
     */
    public Auth getUserProfile(String userId) {
        try {
            Long id = Long.parseLong(userId);
            Optional<Auth> userOpt = authRepository.findById(id);
            return userOpt.orElse(null);
        } catch (Exception e) {
            log.error("Failed to get user profile: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Validate user existence and status
     */
    public boolean validateUser(String userId) {
        try {
            Long id = Long.parseLong(userId);
            Optional<Auth> userOpt = authRepository.findById(id);
            return userOpt.map(Auth::getIsActive).orElse(false);
        } catch (Exception e) {
            log.error("User validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get user role for authorization
     */
    public String getUserRole(String userId) {
        try {
            Long id = Long.parseLong(userId);
            Optional<Auth> userOpt = authRepository.findById(id);
            return userOpt.map(user -> user.getSystemRole().name()).orElse(null);
        } catch (Exception e) {
            log.error("Failed to get user role: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Check if user is active
     */
    public boolean isUserActive(String userId) {
        try {
            Long id = Long.parseLong(userId);
            Optional<Auth> userOpt = authRepository.findById(id);
            return userOpt.map(Auth::getIsActive).orElse(false);
        } catch (Exception e) {
            log.error("Failed to check user status: {}", e.getMessage());
            return false;
        }
    }
}
