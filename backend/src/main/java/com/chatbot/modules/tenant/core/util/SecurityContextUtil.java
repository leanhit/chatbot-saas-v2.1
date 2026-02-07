package com.chatbot.modules.tenant.core.util;

import com.chatbot.modules.auth.security.CustomUserDetails;
import com.chatbot.modules.auth.model.Auth;
import com.chatbot.modules.auth.repository.AuthRepository;
import com.chatbot.modules.identity.model.User;
import com.chatbot.modules.identity.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Utility class to extract current user information from SecurityContext
 * Updated to handle both CustomUserDetails and String principals with database lookup
 */
@Component
@Slf4j
public class SecurityContextUtil {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;

    public SecurityContextUtil(UserRepository userRepository, AuthRepository authRepository) {
        this.userRepository = userRepository;
        this.authRepository = authRepository;
    }

    /**
     * Get current user ID from SecurityContext
     * @return UUID ID of current user, or null if not authenticated
     */
    public UUID getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authenticated user found in SecurityContext");
                return null;
            }

            Object principal = authentication.getPrincipal();
            
            if (principal instanceof CustomUserDetails customUserDetails) {
                UUID userId = customUserDetails.getUserId();
                log.debug("Found authenticated user with ID: {}", userId);
                return userId;
            }
            
            // Handle String principal (email addresses) with database lookup
            if (principal instanceof String) {
                String emailPrincipal = (String) principal;
                log.debug("Found String principal (email): {}", emailPrincipal);
                
                try {
                    // First try to find in identity_users table
                    User user = userRepository.findByEmail(emailPrincipal)
                            .orElse(null);
                    
                    if (user != null) {
                        log.debug("Found user in identity_users table: {}", emailPrincipal);
                        return user.getId();
                    }
                    
                    // If not found in identity_users, try auth_users table
                    Auth auth = authRepository.findByEmail(emailPrincipal)
                            .orElse(null);
                            
                    if (auth != null) {
                        log.debug("Found user in auth_users table (legacy): {}", emailPrincipal);
                        return auth.getUserId(); // Use the new UUID field
                    }
                    
                    log.warn("User not found in either table for email: {}", emailPrincipal);
                    return null;
                } catch (Exception e) {
                    log.error("Error looking up user by email: {}", emailPrincipal, e);
                    return null;
                }
            }
            
            log.error("Unsupported principal type: {}", principal.getClass().getSimpleName());
            throw new IllegalStateException("Unsupported principal type: " + principal.getClass().getSimpleName());
            
        } catch (Exception e) {
            log.error("Error extracting current user ID from SecurityContext", e);
            throw new IllegalStateException("Failed to extract user ID from SecurityContext", e);
        }
    }
    
    /**
     * Get current user ID as UUID from SecurityContext (legacy compatible)
     * @return UUID ID of current user, or null if not authenticated
     */
    public UUID getCurrentUserIdUUID() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authenticated user found in SecurityContext");
                return null;
            }

            Object principal = authentication.getPrincipal();
            
            if (principal instanceof CustomUserDetails customUserDetails) {
                UUID userId = customUserDetails.getUserIdUUID();
                log.debug("Found authenticated user with UUID: {}", userId);
                return userId;
            }
            
            // Handle String principal (email addresses) with database lookup
            if (principal instanceof String) {
                String emailPrincipal = (String) principal;
                log.debug("Found String principal (email): {}", emailPrincipal);
                
                try {
                    // First try to find in identity_users table
                    User user = userRepository.findByEmail(emailPrincipal)
                            .orElse(null);
                    
                    if (user != null) {
                        log.debug("Found user in identity_users table: {}", emailPrincipal);
                        return user.getId();
                    }
                    
                    // If not found in identity_users, try auth_users table
                    Auth auth = authRepository.findByEmail(emailPrincipal)
                            .orElse(null);
                            
                    if (auth != null) {
                        log.debug("Found user in auth_users table (legacy): {}", emailPrincipal);
                        return auth.getUserId(); // Use the new UUID field
                    }
                    
                    log.warn("User not found in either table for email: {}", emailPrincipal);
                    return null;
                } catch (Exception e) {
                    log.error("Error looking up user by email: {}", emailPrincipal, e);
                    return null;
                }
            }
            
            log.error("Unsupported principal type: {}", principal.getClass().getSimpleName());
            throw new IllegalStateException("Unsupported principal type: " + principal.getClass().getSimpleName());
            
        } catch (Exception e) {
            log.error("Error extracting current user ID from SecurityContext", e);
            throw new IllegalStateException("Failed to extract user ID from SecurityContext", e);
        }
    }

    /**
     * Get current user email from SecurityContext
     * @return email of current user, or null if not authenticated
     */
    public String getCurrentUserEmail() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            Object principal = authentication.getPrincipal();
            
            if (principal instanceof CustomUserDetails customUserDetails) {
                return customUserDetails.getUsername();
            }
            
            // Handle Spring Security User object
            if (principal instanceof org.springframework.security.core.userdetails.User user) {
                return user.getUsername();
            }
            
            // Handle String principal (fallback)
            if (principal instanceof String) {
                return (String) principal;
            }
            
            log.error("Unsupported principal type: {}", principal.getClass().getSimpleName());
            throw new IllegalStateException("Unsupported principal type: " + principal.getClass().getSimpleName());
            
        } catch (Exception e) {
            log.error("Error extracting current user email from SecurityContext", e);
            throw new IllegalStateException("Failed to extract user email from SecurityContext", e);
        }
    }

    /**
     * Check if current user is authenticated
     * @return true if authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}
