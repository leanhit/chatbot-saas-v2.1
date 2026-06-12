package com.chatbot.core.simplepayment.controller;

import com.chatbot.core.simplepayment.model.Webhook;
import com.chatbot.core.simplepayment.service.WebhookService;
import com.chatbot.shared.constants.ApiConstants;
import com.chatbot.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(ApiConstants.BASE_PATH + "/webhooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhook Management", description = "API for managing payment webhooks")
public class WebhookController {

    private final WebhookService webhookService;

    /**
     * Create new webhook (admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create webhook", description = "Create a new webhook (Admin only)")
    public ResponseEntity<ApiResponse<Webhook>> createWebhook(@Valid @RequestBody Webhook webhook) {
        log.info("🪝 Creating webhook: {}", webhook.getName());
        try {
            Webhook created = webhookService.createWebhook(webhook);
            return ResponseEntity.ok(ApiResponse.success(created, "Webhook created successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get all active webhooks (admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get active webhooks", description = "Get all active webhooks (Admin only)")
    public ResponseEntity<ApiResponse<List<Webhook>>> getActiveWebhooks() {
        log.info("📋 Fetching active webhooks");
        List<Webhook> webhooks = webhookService.getActiveWebhooks();
        return ResponseEntity.ok(ApiResponse.success(webhooks, "Webhooks retrieved successfully"));
    }

    /**
     * Update webhook (admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update webhook", description = "Update an existing webhook (Admin only)")
    public ResponseEntity<ApiResponse<Webhook>> updateWebhook(
            @PathVariable Long id,
            @Valid @RequestBody Webhook webhook) {
        log.info("🔄 Updating webhook: {}", id);
        try {
            Webhook updated = webhookService.updateWebhook(id, webhook);
            return ResponseEntity.ok(ApiResponse.success(updated, "Webhook updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Delete webhook (admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete webhook", description = "Delete a webhook (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteWebhook(@PathVariable Long id) {
        log.info("🗑️ Deleting webhook: {}", id);
        try {
            webhookService.deleteWebhook(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Webhook deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Test webhook (admin only)
     */
    @PostMapping("/{id}/test")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Test webhook", description = "Test a webhook endpoint (Admin only)")
    public ResponseEntity<ApiResponse<String>> testWebhook(@PathVariable Long id) {
        log.info("🧪 Testing webhook: {}", id);
        try {
            webhookService.testWebhook(id);
            return ResponseEntity.ok(ApiResponse.success("Webhook test successful", "Test completed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
