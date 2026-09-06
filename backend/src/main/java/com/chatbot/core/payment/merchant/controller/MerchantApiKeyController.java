package com.chatbot.core.payment.merchant.controller;

import com.chatbot.core.payment.merchant.model.MerchantApiKey;
import com.chatbot.core.payment.merchant.repository.MerchantApiKeyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/merchant/keys")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Merchant API Key Management", description = "Admin endpoints for managing merchant API keys")
public class MerchantApiKeyController {

    private final MerchantApiKeyRepository merchantApiKeyRepository;

    /**
     * Get all merchant API keys
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get all merchant API keys (Admin)",
        description = "Get all merchant API keys - Admin only"
    )
    public ResponseEntity<List<MerchantApiKey>> getAllApiKeys() {
        log.info("🔑 Fetching all merchant API keys");
        
        List<MerchantApiKey> keys = merchantApiKeyRepository.findAll();
        // Hide secrets for security
        keys.forEach(key -> {
            key.setApiSecret("********");
            key.setWebhookSecret("********");
        });
        
        return ResponseEntity.ok(keys);
    }

    /**
     * Get API keys for a specific tenant
     */
    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get tenant API keys (Admin)",
        description = "Get API keys for a specific tenant - Admin only"
    )
    public ResponseEntity<List<MerchantApiKey>> getTenantApiKeys(@PathVariable Long tenantId) {
        log.info("🔑 Fetching API keys for tenant: {}", tenantId);
        
        List<MerchantApiKey> keys = merchantApiKeyRepository.findByTenantId(tenantId);
        // Hide secrets for security
        keys.forEach(key -> {
            key.setApiSecret("********");
            key.setWebhookSecret("********");
        });
        
        return ResponseEntity.ok(keys);
    }

    /**
     * Create new merchant API key
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create API key (Admin)",
        description = "Create a new merchant API key - Admin only"
    )
    public ResponseEntity<MerchantApiKey> createApiKey(@RequestBody MerchantApiKey apiKey) {
        log.info("🔑 Creating new API key for tenant: {}", apiKey.getTenantId());
        
        try {
            // Auto-generate keys if not provided
            if (apiKey.getApiKey() == null) {
                apiKey.setApiKey("pk_live_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 24));
            }
            if (apiKey.getApiSecret() == null) {
                apiKey.setApiSecret("sk_live_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 24));
            }
            if (apiKey.getWebhookSecret() == null) {
                apiKey.setWebhookSecret(java.util.UUID.randomUUID().toString().replace("-", ""));
            }
            
            MerchantApiKey created = merchantApiKeyRepository.save(apiKey);
            
            // Return full keys only on creation
            return ResponseEntity.ok(created);
            
        } catch (Exception e) {
            log.error("❌ Failed to create API key: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update merchant API key
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update API key (Admin)",
        description = "Update an existing merchant API key - Admin only"
    )
    public ResponseEntity<MerchantApiKey> updateApiKey(
            @PathVariable Long id,
            @RequestBody MerchantApiKey apiKey) {
        log.info("🔑 Updating API key: {}", id);
        
        try {
            MerchantApiKey existing = merchantApiKeyRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("API key not found"));
            
            // Update fields (don't update keys themselves)
            existing.setName(apiKey.getName());
            existing.setDescription(apiKey.getDescription());
            existing.setWebhookUrl(apiKey.getWebhookUrl());
            existing.setIsActive(apiKey.getIsActive());
            existing.setRateLimitPerMinute(apiKey.getRateLimitPerMinute());
            existing.setExpiresAt(apiKey.getExpiresAt());
            
            MerchantApiKey updated = merchantApiKeyRepository.save(existing);
            
            // Hide secrets
            updated.setApiSecret("********");
            updated.setWebhookSecret("********");
            
            return ResponseEntity.ok(updated);
            
        } catch (Exception e) {
            log.error("❌ Failed to update API key: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete merchant API key
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete API key (Admin)",
        description = "Delete a merchant API key - Admin only"
    )
    public ResponseEntity<Map<String, String>> deleteApiKey(@PathVariable Long id) {
        log.info("🗑️ Deleting API key: {}", id);
        
        try {
            merchantApiKeyRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "API key deleted successfully"));
        } catch (Exception e) {
            log.error("❌ Failed to delete API key: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Regenerate API secret
     */
    @PostMapping("/{id}/regenerate-secret")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Regenerate API secret (Admin)",
        description = "Regenerate the API secret for a merchant - Admin only"
    )
    public ResponseEntity<Map<String, String>> regenerateSecret(@PathVariable Long id) {
        log.info("🔄 Regenerating API secret for key: {}", id);
        
        try {
            MerchantApiKey existing = merchantApiKeyRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("API key not found"));
            
            String newSecret = "sk_live_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 24);
            existing.setApiSecret(newSecret);
            
            merchantApiKeyRepository.save(existing);
            
            return ResponseEntity.ok(Map.of(
                "message", "API secret regenerated successfully",
                "apiSecret", newSecret
            ));
            
        } catch (Exception e) {
            log.error("❌ Failed to regenerate secret: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Regenerate webhook secret
     */
    @PostMapping("/{id}/regenerate-webhook-secret")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Regenerate webhook secret (Admin)",
        description = "Regenerate the webhook secret for a merchant - Admin only"
    )
    public ResponseEntity<Map<String, String>> regenerateWebhookSecret(@PathVariable Long id) {
        log.info("🔄 Regenerating webhook secret for key: {}", id);
        
        try {
            MerchantApiKey existing = merchantApiKeyRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("API key not found"));
            
            String newSecret = java.util.UUID.randomUUID().toString().replace("-", "");
            existing.setWebhookSecret(newSecret);
            
            merchantApiKeyRepository.save(existing);
            
            return ResponseEntity.ok(Map.of(
                "message", "Webhook secret regenerated successfully",
                "webhookSecret", newSecret
            ));
            
        } catch (Exception e) {
            log.error("❌ Failed to regenerate webhook secret: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Deactivate expired API keys
     */
    @PostMapping("/deactivate-expired")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Deactivate expired API keys (Admin)",
        description = "Deactivate all expired API keys - Admin only"
    )
    public ResponseEntity<Map<String, Object>> deactivateExpiredKeys() {
        log.info("🔄 Deactivating expired API keys");
        
        try {
            List<MerchantApiKey> expiredKeys = merchantApiKeyRepository.findActiveApiKeys(LocalDateTime.now())
                    .stream()
                    .filter(key -> !key.isValid())
                    .toList();
            
            for (MerchantApiKey key : expiredKeys) {
                key.setIsActive(false);
                merchantApiKeyRepository.save(key);
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "Expired API keys deactivated",
                "count", expiredKeys.size()
            ));
            
        } catch (Exception e) {
            log.error("❌ Failed to deactivate expired keys: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
