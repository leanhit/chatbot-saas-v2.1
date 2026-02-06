package com.chatbot.modules.tenant.core.util;

import com.chatbot.modules.auth.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Utility class to extract current user information from SecurityContext
 * This replaces hardcoded user IDs in TenantService
 * Updated to work exclusively with CustomUserDetails from Identity module
 */
@Component
@Slf4j
public class SecurityContextUtil {

    /**
     * Get current user ID from SecurityContext
     * @return Long ID of current user, or null if not authenticated
     */
    public Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authenticated user found in SecurityContext");
                return null;
            }

            Object principal = authentication.getPrincipal();
            
            if (principal instanceof CustomUserDetails customUserDetails) {
                Long userId = customUserDetails.getUserId();
                log.debug("Found authenticated user with ID: {}", userId);
                return userId;
            }
            
            // Fallback for String principal (test purposes)
            if (principal instanceof String) {
                try {
                    return Long.parseLong((String) principal);
                } catch (NumberFormatException e) {
                    log.warn("Cannot parse String principal to Long: {}", principal);
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
     * Get current user ID as UUID from SecurityContext
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
            
            // Fallback for String principal (test purposes)
            if (principal instanceof String) {
                try {
                    Long longId = Long.parseLong((String) principal);
                    return UUID.nameUUIDFromBytes(("user_" + longId).getBytes());
                } catch (NumberFormatException e) {
                    log.warn("Cannot parse String principal to Long: {}", principal);
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
