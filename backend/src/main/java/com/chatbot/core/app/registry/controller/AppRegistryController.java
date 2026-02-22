package com.chatbot.core.app.registry.controller;

import com.chatbot.core.app.registry.dto.AppResponse;
import com.chatbot.core.app.registry.dto.RegisterAppRequest;
import com.chatbot.core.app.registry.model.AppStatus;
import com.chatbot.core.app.registry.service.AppRegistryService;
import com.chatbot.shared.dto.ApiResponse;
import com.chatbot.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// import io.swagger.v3.oas.annotations.responses.ApiResponse; // Use fully qualified name to avoid conflict
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/apps")
@Tag(name = "App Registry", description = "App Registry Management API")
public class AppRegistryController {
    
    @Autowired
    private AppRegistryService appRegistryService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('APP_MANAGER')")
    @Operation(
        summary = "Register a new application", 
        description = "Register a new application in the registry",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "App registered successfully",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
        }
    )
    public ResponseEntity<ApiResponse<AppResponse>> registerApp(
            @Valid @RequestBody RegisterAppRequest request) {
        
        // TODO: Get current user ID from security context
        Long currentUserId = 1L; // Placeholder
        
        AppResponse response = appRegistryService.registerApp(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "App registered successfully"));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get app by ID", description = "Retrieve application details by ID")
    public ResponseEntity<ApiResponse<AppResponse>> getAppById(
            @Parameter(description = "App ID") @PathVariable Long id) {
        
        AppResponse response = appRegistryService.getAppById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/name/{name}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get app by name", description = "Retrieve application details by name")
    public ResponseEntity<ApiResponse<AppResponse>> getAppByName(
            @Parameter(description = "App name") @PathVariable String name) {
        
        AppResponse response = appRegistryService.getAppByName(name);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Search apps", description = "Search applications with filters")
    public ResponseEntity<ApiResponse<PageResponse<AppResponse>>> searchApps(
            @Parameter(description = "Search by name") @RequestParam(required = false) String name,
            @Parameter(description = "Filter by app type") @RequestParam(required = false) String appType,
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AppResponse> appPage = appRegistryService.searchApps(name, appType, status, isActive, pageable);
        
        PageResponse<AppResponse> pageResponse = PageResponse.<AppResponse>builder()
                .content(appPage.getContent())
                .page(appPage.getNumber())
                .size(appPage.getSize())
                .totalElements(appPage.getTotalElements())
                .totalPages(appPage.getTotalPages())
                .first(appPage.isFirst())
                .last(appPage.isLast())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }
    
    @GetMapping("/type/{appType}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get apps by type", description = "Retrieve all applications of a specific type")
    public ResponseEntity<ApiResponse<List<AppResponse>>> getAppsByType(
            @Parameter(description = "App type") @PathVariable String appType) {
        
        List<AppResponse> response = appRegistryService.getAppsByType(appType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/active")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get active apps", description = "Retrieve all active applications")
    public ResponseEntity<ApiResponse<List<AppResponse>>> getActiveApps() {
        List<AppResponse> response = appRegistryService.getActiveApps();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/public")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Get public apps", description = "Retrieve all public applications")
    public ResponseEntity<ApiResponse<List<AppResponse>>> getPublicApps() {
        List<AppResponse> response = appRegistryService.getPublicApps();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('APP_MANAGER')")
    @Operation(summary = "Update app", description = "Update application details")
    public ResponseEntity<ApiResponse<AppResponse>> updateApp(
            @Parameter(description = "App ID") @PathVariable Long id,
            @Valid @RequestBody RegisterAppRequest request) {
        
        // TODO: Get current user ID from security context
        Long currentUserId = 1L; // Placeholder
        
        AppResponse response = appRegistryService.updateApp(id, request, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(response, "App updated successfully"));
    }
    
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('APP_MANAGER')")
    @Operation(summary = "Update app status", description = "Update application status")
    public ResponseEntity<ApiResponse<AppResponse>> updateAppStatus(
            @Parameter(description = "App ID") @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam AppStatus status) {
        
        // TODO: Get current user ID from security context
        Long currentUserId = 1L; // Placeholder
        
        AppResponse response = appRegistryService.updateAppStatus(id, status, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(response, "App status updated successfully"));
    }
    
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('APP_MANAGER')")
    @Operation(summary = "Toggle app activation", description = "Activate or deactivate an application")
    public ResponseEntity<ApiResponse<AppResponse>> toggleAppActivation(
            @Parameter(description = "App ID") @PathVariable Long id,
            @Parameter(description = "Active status") @RequestParam Boolean isActive) {
        
        // TODO: Get current user ID from security context
        Long currentUserId = 1L; // Placeholder
        
        AppResponse response = appRegistryService.toggleAppActivation(id, isActive, currentUserId);
        String message = isActive ? "App activated successfully" : "App deactivated successfully";
        return ResponseEntity.ok(ApiResponse.success(response, message));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete app", description = "Delete an application from the registry")
    public ResponseEntity<ApiResponse<Void>> deleteApp(
            @Parameter(description = "App ID") @PathVariable Long id) {
        
        appRegistryService.deleteApp(id);
        return ResponseEntity.ok(ApiResponse.success(null, "App deleted successfully"));
    }
}
