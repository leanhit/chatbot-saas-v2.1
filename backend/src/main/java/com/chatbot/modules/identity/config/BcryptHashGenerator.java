package com.chatbot.modules.identity.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Temporary BCrypt hash generator for testing
 * TODO: Remove after Step 1 completion
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BcryptHashGenerator implements CommandLineRunner {
    
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        String rawPassword = "123456";
        String hash = passwordEncoder.encode(rawPassword);
        
        System.out.println("RAW: " + rawPassword);
        System.out.println("HASH: " + hash);
        log.info("Generated BCrypt hash for '{}': {}", rawPassword, hash);
    }
}
