package com.chatbot.core.identity.controller;

import com.chatbot.core.identity.security.CustomUserDetails;
import com.chatbot.core.identity.service.TwoFactorService;
import com.chatbot.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 2FA (TOTP) management endpoints.
 * Setup/enable/disable require an authenticated session;
 * verify is used during login when 2FA is enabled.
 */
@RestController
@RequestMapping("/api/auth/2fa")
@RequiredArgsConstructor
@Slf4j
public class TwoFactorController {

    private final TwoFactorService twoFactorService;

    /** Generate a new TOTP secret + otpauth URI (QR payload). */
    @PostMapping("/setup")
    public ResponseEntity<ApiResponse<Map<String, Object>>> setup(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        String email = currentUser.getUsername();
        TwoFactorService.TwoFactorSetup setup = twoFactorService.generateSecret(email);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
            "secret", setup.secret(),
            "otpAuthUri", setup.otpAuthUri()
        ), "TOTP secret generated. Scan the QR code then call /enable with a valid code."));
    }

    /** Enable 2FA after verifying a code from the authenticator app. */
    @PostMapping("/enable")
    public ResponseEntity<ApiResponse<Map<String, Object>>> enable(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody Map<String, String> body) {
        String email = currentUser.getUsername();
        String code = body.get("code");
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("code is required"));
        }
        boolean ok = twoFactorService.enable(email, code);
        if (!ok) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid TOTP code"));
        }
        return ResponseEntity.ok(ApiResponse.success(Map.of("enabled", true), "2FA enabled"));
    }

    /** Disable 2FA (requires current valid code). */
    @PostMapping("/disable")
    public ResponseEntity<ApiResponse<Map<String, Object>>> disable(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody Map<String, String> body) {
        String email = currentUser.getUsername();
        String code = body.get("code");
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("code is required"));
        }
        boolean ok = twoFactorService.disable(email, code);
        if (!ok) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid TOTP code or 2FA not enabled"));
        }
        return ResponseEntity.ok(ApiResponse.success(Map.of("enabled", false), "2FA disabled"));
    }

    /** Status check. */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        var user = twoFactorService.findUser(currentUser.getUsername()).orElse(null);
        boolean enabled = user != null && twoFactorService.isEnabled(user);
        boolean pending = user != null && user.getTotpSecret() != null && !enabled;
        return ResponseEntity.ok(ApiResponse.success(Map.of(
            "enabled", enabled,
            "pendingSetup", pending
        )));
    }
}
