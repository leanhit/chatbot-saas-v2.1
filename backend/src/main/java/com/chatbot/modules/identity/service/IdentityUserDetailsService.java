package com.chatbot.modules.identity.service;

import com.chatbot.modules.auth.security.CustomUserDetails;
import com.chatbot.modules.identity.model.User;
import com.chatbot.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Identity UserDetailsService for Identity Hub users
 * This service loads users from identity_users table
 */
@Service("identityUserDetailsService")
@RequiredArgsConstructor
@Slf4j
public class IdentityUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user from identity_users table for email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password("") // No password needed for JWT validation
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }

    /**
     * Load user by ID for Identity Hub tokens
     */
    public UserDetails loadUserByUserId(Long userId) throws UsernameNotFoundException {
        log.debug("Loading user from identity_users table for ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        // Create CustomUserDetails instead of Spring Security User
        return new CustomUserDetails(user);
    }
}
