package com.chatbot.core.tenant.controller;

import com.chatbot.core.tenant.model.TenantWebhook;
import com.chatbot.core.tenant.service.TenantWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<TenantWebhook>> listWebhooks() {
        return ResponseEntity.ok(webhookService.listWebhooks());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'EDITOR')")
    public ResponseEntity<TenantWebhook> createWebhook(@RequestBody TenantWebhook webhook) {
        return ResponseEntity.ok(webhookService.createWebhook(webhook));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'EDITOR')")
    public ResponseEntity<TenantWebhook> updateWebhook(@PathVariable Long id,
                                                       @RequestBody TenantWebhook webhook) {
        return ResponseEntity.ok(webhookService.updateWebhook(id, webhook));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Void> deleteWebhook(@PathVariable Long id) {
        webhookService.deleteWebhook(id);
        return ResponseEntity.noContent().build();
    }
}
