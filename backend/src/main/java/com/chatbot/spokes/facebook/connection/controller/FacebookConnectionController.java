package com.chatbot.spokes.facebook.connection.controller;

import com.chatbot.spokes.facebook.connection.dto.CreateFacebookConnectionRequest;
import com.chatbot.spokes.facebook.connection.dto.FacebookConnectionResponse;
import com.chatbot.spokes.facebook.connection.dto.UpdateFacebookConnectionRequest;
import com.chatbot.spokes.facebook.connection.service.FacebookConnectionService;
import com.chatbot.core.tenant.infra.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// import io.swagger.v3.oas.annotations.responses.ApiResponse; // Use fully qualified name to avoid conflict
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/connection/facebook")
@Tag(name = "Facebook Connection", description = "Facebook platform connection management")
public class FacebookConnectionController {

    private final FacebookConnectionService facebookConnectionService;

    public FacebookConnectionController(FacebookConnectionService facebookConnectionService) {
        this.facebookConnectionService = facebookConnectionService;
    }

    @PostMapping
    @Operation(
        summary = "Create Facebook connection",
        description = "Create a new Facebook connection for the tenant",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Connection created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid connection data or missing tenant context"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
        }
    )
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
    @Operation(
        summary = "Update Facebook connection",
        description = "Update an existing Facebook connection",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Connection updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid connection data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Connection not found")
        }
    )
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
    @Operation(
        summary = "Get all Facebook connections",
        description = "Retrieve all Facebook connections including inactive ones",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "All connections retrieved successfully",
                content = @Content(schema = @Schema(implementation = Page.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
        }
    )
    public ResponseEntity<Page<FacebookConnectionResponse>> getConnectionsAll(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String ownerId = principal.getName();
        Page<FacebookConnectionResponse> connections = facebookConnectionService.getConnectionsByOwnerIdAll(ownerId, page, size);
        return ResponseEntity.ok(connections);
    }    

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete Facebook connection",
        description = "Delete a Facebook connection",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Connection deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Connection not found")
        }
    )
    public ResponseEntity<Void> deleteConnection(@PathVariable String id) {
        facebookConnectionService.deleteConnection(id);
        return ResponseEntity.noContent().build();
    }
}
