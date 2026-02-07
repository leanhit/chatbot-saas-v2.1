package com.chatbot.modules.identity.service;

import com.chatbot.modules.auth.security.CustomUserDetails;
import com.chatbot.modules.auth.model.Auth;
import com.chatbot.modules.auth.repository.AuthRepository;
import com.chatbot.modules.identity.model.User;
import com.chatbot.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Identity UserDetailsService for both Identity Hub and Legacy users
 * This service loads users from both identity_users and auth_users tables
 */
@Service("identityUserDetailsService")
@RequiredArgsConstructor
@Slf4j
public class IdentityUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user for email: {}", email);
        
        // First try to load from identity_users table
        try {
            User user = userRepository.findByEmail(email)
                    .orElse(null);
            
            if (user != null) {
                log.debug("Found user in identity_users table: {}", email);
                return new CustomUserDetails(user);
            }
        } catch (Exception e) {
            log.debug("Error loading from identity_users table", e);
        }
        
        // If not found in identity_users, try auth_users table (legacy)
        try {
            Auth auth = authRepository.findByEmail(email)
                    .orElse(null);
            
            if (auth != null) {
                log.debug("Found user in auth_users table (legacy): {}", email);
                return new CustomUserDetails(auth);
            }
        } catch (Exception e) {
            log.debug("Error loading from auth_users table", e);
        }
        
        throw new UsernameNotFoundException("User not found: " + email);
    }

    /**
     * Load user by ID for Identity Hub tokens
     */
    public UserDetails loadUserByUserId(UUID userId) throws UsernameNotFoundException {
        log.debug("Loading user from identity_users table for ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        // Create CustomUserDetails instead of Spring Security User
        return new CustomUserDetails(user);
    }
}
