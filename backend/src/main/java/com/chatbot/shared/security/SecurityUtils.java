package com.chatbot.shared.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public class SecurityUtils {

    public static Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return Optional.of(((UserDetails) authentication.getPrincipal()).getUsername());
        }
        return Optional.empty();
    }

    public static Optional<String> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return Optional.of(userDetails.getUsername());
        }
        return Optional.empty();
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getPrincipal());
    }

    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    public static boolean hasAnyRole(String... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> {
                    String authorityRole = authority.getAuthority();
                    for (String role : roles) {
                        if (authorityRole.equals("ROLE_" + role)) {
                            return true;
                        }
                    }
                    return false;
                });
    }

    public static void clearContext() {
        SecurityContextHolder.clearContext();
    }

    public static String getBearerToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof String) {
            return (String) authentication.getCredentials();
        }
        return null;
    }

    /**
     * Extract tenant ID from JWT claims or user details
     * Returns Optional<Long> to handle cases where tenant ID is not available
     */
    public static Optional<Long> getCurrentTenantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        // Try to get tenant ID from custom user details if available
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            // Check if UserDetails has tenantId property via reflection or custom interface
            try {
                java.lang.reflect.Field tenantIdField = principal.getClass().getDeclaredField("tenantId");
                tenantIdField.setAccessible(true);
                Object tenantIdValue = tenantIdField.get(principal);
                if (tenantIdValue instanceof Long) {
                    return Optional.of((Long) tenantIdValue);
                } else if (tenantIdValue instanceof Integer) {
                    return Optional.of(((Integer) tenantIdValue).longValue());
                } else if (tenantIdValue instanceof String) {
                    return Optional.of(Long.parseLong((String) tenantIdValue));
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // Field not found, try alternative methods
            }
        }

        // Try to get from authentication details (JWT claims)
        Object details = authentication.getDetails();
        if (details != null) {
            try {
                // Check for tenantId in details
                java.lang.reflect.Field tenantIdField = details.getClass().getDeclaredField("tenantId");
                tenantIdField.setAccessible(true);
                Object tenantIdValue = tenantIdField.get(details);
                if (tenantIdValue instanceof Long) {
                    return Optional.of((Long) tenantIdValue);
                } else if (tenantIdValue instanceof Integer) {
                    return Optional.of(((Integer) tenantIdValue).longValue());
                } else if (tenantIdValue instanceof String) {
                    return Optional.of(Long.parseLong((String) tenantIdValue));
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // Field not found
            }
        }

        // Try to get from custom authorities or attributes
        if (authentication.getAuthorities() != null) {
            for (var authority : authentication.getAuthorities()) {
                String auth = authority.getAuthority();
                if (auth.startsWith("TENANT_")) {
                    try {
                        return Optional.of(Long.parseLong(auth.substring(7)));
                    } catch (NumberFormatException e) {
                        // Invalid format, continue
                    }
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Get current tenant ID with fallback to default
     * WARNING: Only use this for development/testing. In production, handle missing tenant ID properly.
     */
    public static Long getCurrentTenantIdOrDefault(Long defaultTenantId) {
        return getCurrentTenantId().orElse(defaultTenantId);
    }
}
