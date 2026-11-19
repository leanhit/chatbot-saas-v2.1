package com.chatbot.core.payment.merchant.controller;

import com.chatbot.core.payment.merchant.dto.MerchantApiKeyRequest;
import com.chatbot.core.payment.merchant.dto.MerchantApiKeyResponse;
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
import com.chatbot.shared.utils.DateUtils;
import jakarta.validation.Valid;

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
    public ResponseEntity<List<MerchantApiKeyResponse>> getAllApiKeys() {
        log.info("🔑 Fetching all merchant API keys");
        
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.status(403).build();
        }
        List<MerchantApiKeyResponse> keys = merchantApiKeyRepository.findByTenantId(tenantId)
                .stream().map(MerchantApiKeyResponse::from).toList();
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
    public ResponseEntity<List<MerchantApiKeyResponse>> getTenantApiKeys(@PathVariable Long tenantId) {
        log.info("🔑 Fetching API keys for tenant: {}", tenantId);
        
        List<MerchantApiKeyResponse> keys = merchantApiKeyRepository.findByTenantId(tenantId)
                .stream().map(MerchantApiKeyResponse::from).toList();
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
    public ResponseEntity<MerchantApiKeyResponse> createApiKey(@Valid @RequestBody MerchantApiKeyRequest request) {
        log.info("🔑 Creating new API key for tenant: {}", request.getTenantId());
        
        try {
            MerchantApiKey apiKey = new MerchantApiKey();
            apiKey.setTenantId(request.getTenantId());
            apiKey.setName(request.getName());
            apiKey.setDescription(request.getDescription());
            apiKey.setWebhookUrl(request.getWebhookUrl());
            apiKey.setIsActive(request.getIsActive());
            apiKey.setRateLimitPerMinute(request.getRateLimitPerMinute());
            apiKey.setExpiresAt(request.getExpiresAt());
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
            
            // Secrets are returned only on creation (show-once contract)
            return ResponseEntity.ok(MerchantApiKeyResponse.from(created, true));
            
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
    public ResponseEntity<MerchantApiKeyResponse> updateApiKey(
            @PathVariable Long id,
            @Valid @RequestBody MerchantApiKeyRequest request) {
        log.info("🔑 Updating API key: {}", id);
        
        try {
            MerchantApiKey existing = merchantApiKeyRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("API key not found"));
            
            // Update fields (don't update keys themselves)
            existing.setName(request.getName());
            existing.setDescription(request.getDescription());
            existing.setWebhookUrl(request.getWebhookUrl());
            existing.setIsActive(request.getIsActive());
            existing.setRateLimitPerMinute(request.getRateLimitPerMinute());
            existing.setExpiresAt(request.getExpiresAt());
            
            MerchantApiKey updated = merchantApiKeyRepository.save(existing);
            return ResponseEntity.ok(MerchantApiKeyResponse.from(updated));
            
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
            List<MerchantApiKey> expiredKeys = merchantApiKeyRepository.findActiveApiKeys(DateUtils.nowUtcLocalDateTime())
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
