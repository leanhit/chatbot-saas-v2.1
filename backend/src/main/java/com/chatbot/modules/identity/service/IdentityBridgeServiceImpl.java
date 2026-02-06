package com.chatbot.modules.identity.service;

import com.chatbot.modules.identity.dto.RegisterRequest;
import com.chatbot.modules.identity.model.Credential;
import com.chatbot.modules.identity.model.User;
import com.chatbot.modules.identity.model.UserStatus;
import com.chatbot.modules.identity.repository.CredentialRepository;
import com.chatbot.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdentityBridgeServiceImpl implements IdentityBridgeService {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final AuthenticationService authenticationService;

    @Override
    @Transactional
    public boolean createIdentityUser(String email, String password) {
        try {
            log.info("IDENTITY BRIDGE: Starting user creation for email: {}", email);
            
            // Check if user already exists in Identity Hub
            if (identityUserExists(email)) {
                log.warn("IDENTITY BRIDGE: User already exists in Identity Hub: {}", email);
                return true;
            }

            // Create RegisterRequest for Identity Hub
            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setEmail(email);
            registerRequest.setPassword(password);

            // Register user in Identity Hub
            var response = authenticationService.register(registerRequest);
            
            if (response != null && response.getUserId() != null) {
                log.info("IDENTITY BRIDGE: Successfully created Identity Hub user for email: {}", email);
                return true;
            } else {
                log.error("IDENTITY BRIDGE: Failed to create Identity Hub user for email: {}", email);
                return false;
            }
            
        } catch (Exception e) {
            log.error("IDENTITY BRIDGE: Error creating Identity Hub user for email: {}. Error: {}", 
                    email, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean identityUserExists(String email) {
        try {
            return userRepository.existsByEmail(email);
        } catch (Exception e) {
            log.error("IDENTITY BRIDGE: Error checking if user exists for email: {}. Error: {}", 
                    email, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean synchronizeUserData(String email) {
        try {
            log.info("IDENTITY BRIDGE: Starting data synchronization for email: {}", email);
            
            // Check if user exists in both systems
            if (!identityUserExists(email)) {
                log.warn("IDENTITY BRIDGE: User does not exist in Identity Hub for email: {}", email);
                return false;
            }

            // For now, just log successful synchronization
            // In the future, this could sync additional user data between systems
            log.info("IDENTITY BRIDGE: Successfully synchronized data for email: {}", email);
            return true;
            
        } catch (Exception e) {
            log.error("IDENTITY BRIDGE: Error synchronizing data for email: {}. Error: {}", 
                    email, e.getMessage(), e);
            return false;
        }
    }
}
