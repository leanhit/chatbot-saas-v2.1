package com.chatbot.modules.identity.config;

import com.chatbot.modules.identity.model.Credential;
import com.chatbot.modules.identity.model.User;
import com.chatbot.modules.identity.model.UserStatus;
import com.chatbot.modules.identity.repository.CredentialRepository;
import com.chatbot.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!minimal")
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String email = "test@identityhub.com";
        String rawPassword = "123456";

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            log.warn("DataInitializer: User not found - creating default user: {}", email);
            
            // Create user if not exists
            user = User.builder()
                    .email(email)
                    .status(UserStatus.ACTIVE)
                    .build();
            user = userRepository.save(user);
            log.info("DataInitializer: Created default user with ID: {} for email: {}", user.getId(), email);
        }

        if (credentialRepository.existsByUserId(user.getId())) {
            log.info("DataInitializer: Credential already exists for user {}", email);
            return;
        }

        String encoded = passwordEncoder.encode(rawPassword);

        Credential credential = Credential.builder()
                .userId(user.getId())
                .user(user)
                .password(encoded)
                .source("DATA_INITIALIZER")
                .provider("LOCAL")
                .isActive(true)
                .failedAttempts(0)
                .lockedUntil(null)
                .passwordChangedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        credentialRepository.save(credential);

        log.info("✅ DataInitializer: Created credential for {} | RAW={} | HASH={}",
                email, rawPassword, encoded);
    }
}
