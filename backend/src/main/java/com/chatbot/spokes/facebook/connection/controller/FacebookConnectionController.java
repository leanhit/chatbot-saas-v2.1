package com.chatbot.spokes.facebook.connection.controller;

import com.chatbot.spokes.facebook.connection.dto.CreateFacebookConnectionRequest;
import com.chatbot.spokes.facebook.connection.dto.FacebookConnectionResponse;
import com.chatbot.spokes.facebook.connection.dto.UpdateFacebookConnectionRequest;
import com.chatbot.spokes.facebook.connection.service.FacebookConnectionService;
import com.chatbot.core.tenant.infra.TenantContext;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/connection/facebook")
public class FacebookConnectionController {

    private final FacebookConnectionService facebookConnectionService;

    public FacebookConnectionController(FacebookConnectionService facebookConnectionService) {
        this.facebookConnectionService = facebookConnectionService;
    }

    @PostMapping
    public ResponseEntity<String> createConnection(@Valid @RequestBody CreateFacebookConnectionRequest request, Principal principal) {
        // ✅ Validate tenant context
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body("Tenant context not found. Please provide X-Tenant-Key header");
        }
        
        String ownerId = principal.getName();
        String connectionId = facebookConnectionService.createConnection(ownerId, request);
        return ResponseEntity.ok("Connection created with ID: " + connectionId);
    }

    @PutMapping("/{connectionId}")
    public ResponseEntity<String> updateConnection(@PathVariable UUID connectionId,
                                                   @Valid @RequestBody UpdateFacebookConnectionRequest request,
                                                   Principal principal) {
        String ownerId = principal.getName();
        facebookConnectionService.updateConnection(connectionId, ownerId, request);
        return ResponseEntity.ok("Connection updated successfully.");
    }
    
    @GetMapping
    public ResponseEntity<Page<FacebookConnectionResponse>> getConnections(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String ownerId = principal.getName();
        Page<FacebookConnectionResponse> connections = facebookConnectionService.getConnectionsByOwnerId(ownerId, page, size);
        return ResponseEntity.ok(connections);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<FacebookConnectionResponse>> getConnectionsAll(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String ownerId = principal.getName();
        Page<FacebookConnectionResponse> connections = facebookConnectionService.getConnectionsByOwnerIdAll(ownerId, page, size);
        return ResponseEntity.ok(connections);
    }    

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConnection(@PathVariable String id) {
        facebookConnectionService.deleteConnection(id);
        return ResponseEntity.noContent().build();
    }
}
