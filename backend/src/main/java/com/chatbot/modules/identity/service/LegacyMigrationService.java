package com.chatbot.modules.identity.service;

import com.chatbot.modules.identity.model.Credential;
import com.chatbot.modules.identity.model.User;
import com.chatbot.modules.identity.repository.CredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for migrating legacy users to Identity Hub credentials
 * Handles auto-migration during hybrid login process
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LegacyMigrationService {

    private final CredentialRepository credentialRepository;

    /**
     * Migrate legacy user to Identity Hub credential
     * This is called when a user successfully authenticates via legacy login
     * 
     * @param user The User entity (from Identity Hub)
     * @param legacyPassword The BCrypt password from legacy users table
     */
    @Transactional
    public Credential migrateLegacyUserToCredential(User user, String legacyPassword) {
        log.info("Starting migration for user: {}", user.getEmail());
        
        // Check if credential already exists (idempotent)
        if (credentialRepository.existsById(user.getId())) {
            log.info("Credential already exists for user: {}, skipping migration", user.getEmail());
            return credentialRepository.findById(user.getId()).orElse(null);
        }

        // Create credential from legacy data
        Credential credential = Credential.builder()
                .userId(user.getId())
                .user(user)
                .password(legacyPassword) // Keep BCrypt hash as-is
                .source("LEGACY")
                .provider("LOCAL")
                .isActive(true)
                .failedAttempts(0)
                .lockedUntil(null)
                .passwordChangedAt(user.getUpdatedAt() != null ? user.getUpdatedAt() : LocalDateTime.now())
                .metadata("{\"migration_source\": \"legacy_users\", \"migration_date\": \"" + LocalDateTime.now() + "\"}")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Credential savedCredential = credentialRepository.save(credential);
        
        log.info("Successfully migrated legacy user to credential: {} (source: LEGACY)", user.getEmail());
        return savedCredential;
    }

    /**
     * Check if user needs migration (no credential exists)
     */
    public boolean needsMigration(UUID userId) {
        return !credentialRepository.existsById(userId);
    }

    /**
     * Get credential for user, or null if not exists
     */
    public Credential getCredential(UUID userId) {
        return credentialRepository.findById(userId).orElse(null);
    }
}
