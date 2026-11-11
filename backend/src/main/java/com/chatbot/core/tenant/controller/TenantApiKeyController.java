package com.chatbot.core.tenant.controller;

import com.chatbot.core.tenant.model.TenantApiKey;
import com.chatbot.core.tenant.service.TenantApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Tenant API key management (JWT-authenticated admin surface).
 */
@RestController
@RequestMapping("/api/tenant/api-keys")
@RequiredArgsConstructor
public class TenantApiKeyController {

    private final TenantApiKeyService apiKeyService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'EDITOR')")
    public ResponseEntity<List<TenantApiKey>> listApiKeys() {
        return ResponseEntity.ok(apiKeyService.listApiKeys());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Map<String, Object>> createApiKey(@RequestBody CreateKeyRequest request) {
        return ResponseEntity.ok(apiKeyService.createApiKey(
                request.name(), request.role(), request.expiresAt()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<TenantApiKey> revokeApiKey(@PathVariable Long id) {
        return ResponseEntity.ok(apiKeyService.revokeApiKey(id));
    }

    public record CreateKeyRequest(String name, TenantApiKey.KeyRole role, LocalDateTime expiresAt) {}
}
