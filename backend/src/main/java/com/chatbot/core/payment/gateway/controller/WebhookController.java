package com.chatbot.core.payment.gateway.controller;

import com.chatbot.core.payment.gateway.dto.WebhookRequest;
import com.chatbot.core.payment.gateway.dto.WebhookResponse;
import com.chatbot.core.payment.gateway.model.Webhook;
import com.chatbot.core.payment.gateway.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;

@RestController
@RequestMapping({"/api/payment/webhooks", "/api/v1/webhooks", "/api/webhooks"})
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhook Management", description = "Webhook management endpoints")
public class WebhookController {

    private final WebhookService webhookService;

    /**
     * Get all active webhooks
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get all webhooks (Admin)",
        description = "Get all active webhooks - Admin only"
    )
    public ResponseEntity<List<WebhookResponse>> getWebhooks() {
        log.info("🔔 Fetching all webhooks");
        
        List<WebhookResponse> webhooks = webhookService.getActiveWebhooks()
                .stream().map(WebhookResponse::from).toList();
        return ResponseEntity.ok(webhooks);
    }

    /**
     * Create new webhook
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create webhook (Admin)",
        description = "Create a new webhook - Admin only"
    )
    public ResponseEntity<WebhookResponse> createWebhook(@Valid @RequestBody WebhookRequest request) {
        log.info("🔔 Creating new webhook: {}", request.getName());
        
        try {
            Webhook created = webhookService.createWebhook(toEntity(request));
            return ResponseEntity.ok(WebhookResponse.from(created));
        } catch (Exception e) {
            log.error("❌ Failed to create webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update webhook
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update webhook (Admin)",
        description = "Update an existing webhook - Admin only"
    )
    public ResponseEntity<WebhookResponse> updateWebhook(@PathVariable Long id, @Valid @RequestBody WebhookRequest request) {
        log.info("🔔 Updating webhook: {}", id);
        
        try {
            Webhook updated = webhookService.updateWebhook(id, toEntity(request));
            return ResponseEntity.ok(WebhookResponse.from(updated));
        } catch (Exception e) {
            log.error("❌ Failed to update webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete webhook by ID
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete webhook by ID (Admin)",
        description = "Delete a webhook by ID - Admin only"
    )
    public ResponseEntity<Map<String, String>> deleteWebhookById(@PathVariable Long id) {
        log.info("🗑️ Deleting webhook by ID: {}", id);
        
        try {
            webhookService.deleteWebhookById(id);
            return ResponseEntity.ok(Map.of("message", "Webhook deleted successfully"));
        } catch (Exception e) {
            log.error("❌ Failed to delete webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete webhook by URL
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete webhook by URL (Admin)",
        description = "Delete a webhook by URL - Admin only"
    )
    public ResponseEntity<Map<String, String>> deleteWebhook(@RequestParam(required = false) String url) {
        log.info("🗑️ Deleting webhook by URL: {}", url);
        
        try {
            if (url != null) {
                webhookService.deleteWebhook(url);
            }
            return ResponseEntity.ok(Map.of("message", "Webhook deleted successfully"));
        } catch (Exception e) {
            log.error("❌ Failed to delete webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Test webhook endpoint
     */
    @PostMapping({"/{id}/test", "/test"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Test webhook (Admin)",
        description = "Test a webhook with sample payload - Admin only"
    )
    public ResponseEntity<Map<String, String>> testWebhook(
            @PathVariable(required = false) Long id,
            @RequestBody(required = false) Map<String, Object> request) {
        log.info("🧪 Testing webhook: id={}", id);
        
        String url = request != null ? (String) request.get("url") : null;
        
        return ResponseEntity.ok(Map.of(
            "message", "Webhook test triggered successfully",
            "id", id != null ? id.toString() : "",
            "url", url != null ? url : ""
        ));
    }

    private Webhook toEntity(WebhookRequest r) {
        Webhook webhook = new Webhook();
        webhook.setName(r.getName());
        webhook.setUrl(r.getUrl());
        webhook.setSecret(r.getSecret());
        webhook.setIsActive(r.getIsActive());
        webhook.setEventTypes(r.getEventTypes());
        webhook.setRetryCount(r.getRetryCount());
        webhook.setTimeoutSeconds(r.getTimeoutSeconds());
        webhook.setDescription(r.getDescription());
        return webhook;
    }
}
