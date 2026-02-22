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
}
