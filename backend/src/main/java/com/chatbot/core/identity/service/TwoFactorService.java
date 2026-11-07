package com.chatbot.core.identity.service;

import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.AuthRepository;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * TOTP two-factor authentication service (RFC 6238).
 * Secrets are stored per-user; verification allows ±1 time
 * step for clock drift.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TwoFactorService {

    private final AuthRepository authRepository;

    @Value("${app.name:ChatbotSaaS}")
    private String appName;

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeVerifier verifier = new DefaultCodeVerifier(
        new DefaultCodeGenerator(HashingAlgorithm.SHA1), timeProvider);

    /**
     * Generate a new TOTP secret for the user (not yet enabled).
     * Returns the secret and otpauth:// URI for QR code generation.
     */
    @Transactional(value = "userTransactionManager")
    public TwoFactorSetup generateSecret(String email) {
        User user = authRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        String secret = secretGenerator.generate();
        user.setTotpSecret(secret);
        user.setTotpEnabled(false);
        authRepository.save(user);

        String uri = String.format(
            "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
            appName, email, secret, appName);

        log.info("[2FA] Generated TOTP secret for user {}", email);
        return new TwoFactorSetup(secret, uri);
    }

    /**
     * Verify a TOTP code and enable 2FA for the user.
     */
    @Transactional(value = "userTransactionManager")
    public boolean enable(String email, String code) {
        User user = authRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        if (user.getTotpSecret() == null) {
            throw new IllegalStateException("No TOTP secret found. Call /2fa/setup first.");
        }
        if (!verifyCode(user.getTotpSecret(), code)) {
            return false;
        }
        user.setTotpEnabled(true);
        authRepository.save(user);
        log.info("[2FA] Enabled TOTP for user {}", email);
        return true;
    }

    /**
     * Disable 2FA for the user (requires valid current code).
     */
    @Transactional(value = "userTransactionManager")
    public boolean disable(String email, String code) {
        User user = authRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        if (!Boolean.TRUE.equals(user.getTotpEnabled()) || user.getTotpSecret() == null) {
            return false;
        }
        if (!verifyCode(user.getTotpSecret(), code)) {
            return false;
        }
        user.setTotpEnabled(false);
        user.setTotpSecret(null);
        authRepository.save(user);
        log.info("[2FA] Disabled TOTP for user {}", email);
        return true;
    }

    /**
     * Verify a TOTP code against a secret (used during login).
     */
    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null || code.isBlank()) {
            return false;
        }
        try {
            return verifier.isValidCode(secret, code.trim());
        } catch (Exception e) {
            log.warn("[2FA] TOTP verification error: {}", e.getMessage());
            return false;
        }
    }

    public boolean isEnabled(User user) {
        return Boolean.TRUE.equals(user.getTotpEnabled()) && user.getTotpSecret() != null;
    }

    public Optional<User> findUser(String email) {
        return authRepository.findByEmail(email);
    }

    public record TwoFactorSetup(String secret, String otpAuthUri) {}
}
