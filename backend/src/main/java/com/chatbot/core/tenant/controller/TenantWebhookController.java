package com.chatbot.core.tenant.controller;

import com.chatbot.core.tenant.dto.TenantWebhookDeliveryResponse;
import com.chatbot.core.tenant.dto.TenantWebhookRequest;
import com.chatbot.core.tenant.dto.TenantWebhookResponse;
import com.chatbot.core.tenant.model.TenantWebhook;
import com.chatbot.core.tenant.model.TenantWebhookDelivery;
import com.chatbot.core.tenant.service.TenantWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.validation.Valid;

/**
 * Tenant outbound webhook management (conversation events).
 */
@RestController
@RequestMapping("/api/tenant/webhooks")
@RequiredArgsConstructor
public class TenantWebhookController {

    private final TenantWebhookService webhookService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'EDITOR')")
    public ResponseEntity<List<TenantWebhookResponse>> listWebhooks() {
        return ResponseEntity.ok(webhookService.listWebhooks().stream()
                .map(TenantWebhookResponse::from).toList());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'EDITOR')")
    public ResponseEntity<TenantWebhookResponse> createWebhook(@Valid @RequestBody TenantWebhookRequest request) {
        return ResponseEntity.ok(TenantWebhookResponse.from(webhookService.createWebhook(toEntity(request))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'EDITOR')")
    public ResponseEntity<TenantWebhookResponse> updateWebhook(@PathVariable Long id,
                                                       @Valid @RequestBody TenantWebhookRequest request) {
        return ResponseEntity.ok(TenantWebhookResponse.from(webhookService.updateWebhook(id, toEntity(request))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Void> deleteWebhook(@PathVariable Long id) {
        webhookService.deleteWebhook(id);
        return ResponseEntity.noContent().build();
    }

    // ===================== DELIVERY MONITORING (DLQ) =====================

    @GetMapping("/deliveries")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'EDITOR')")
    public ResponseEntity<List<TenantWebhookDeliveryResponse>> listDeliveries(
            @RequestParam(required = false) TenantWebhookDelivery.Status status,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(webhookService.listDeliveries(status, limit).stream()
                .map(TenantWebhookDeliveryResponse::from).toList());
    }

    @GetMapping("/deliveries/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'EDITOR')")
    public ResponseEntity<java.util.Map<String, Long>> deliveryStats() {
        return ResponseEntity.ok(webhookService.deliveryStats());
    }

    private TenantWebhook toEntity(TenantWebhookRequest r) {
        TenantWebhook webhook = new TenantWebhook();
        webhook.setUrl(r.getUrl());
        webhook.setSecret(r.getSecret());
        webhook.setEvents(r.getEvents());
        webhook.setIsActive(r.getIsActive());
        return webhook;
    }
}
