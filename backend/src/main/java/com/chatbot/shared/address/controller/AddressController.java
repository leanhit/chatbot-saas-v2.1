package com.chatbot.shared.address.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// import io.swagger.v3.oas.annotations.responses.ApiResponse; // Use fully qualified name to avoid conflict
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chatbot.shared.address.dto.*;
import com.chatbot.shared.address.dto.AddressDetailResponseDTO;
import com.chatbot.shared.address.model.OwnerType;
import com.chatbot.shared.address.service.AddressService;
import com.chatbot.core.tenant.infra.TenantContext;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "Address Management", description = "Address and location management operations")
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    @Operation(
        summary = "Create address",
        description = "Create a new address for the tenant",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Address created successfully",
                content = @Content(schema = @Schema(implementation = AddressResponseDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid address data or missing tenant context")
        }
    )
    public ResponseEntity<AddressResponseDTO> create(
            @Valid @RequestBody AddressRequestDTO dto) {
        
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(addressService.createAddress(tenantId, dto));
    }

    // Lấy địa chỉ duy nhất của owner (single address)
    @GetMapping("/owner/{type}/{id}")
    @Operation(
        summary = "Get single address by owner",
        description = "Retrieve a single address by owner type and ID",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Address retrieved successfully",
                content = @Content(schema = @Schema(implementation = AddressDetailResponseDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing tenant context"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Address not found")
        }
    )
    public ResponseEntity<AddressDetailResponseDTO> getSingleByOwner(
            @PathVariable OwnerType type,
            @PathVariable Long id) {
        
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            return ResponseEntity.ok(addressService.getSingleAddressByOwner(tenantId, type, id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Lấy hoặc tạo địa chỉ duy nhất cho owner
    @GetMapping("/owner/{type}/{id}/ensure")
    @Operation(
        summary = "Get or create single address",
        description = "Retrieve existing address or create new one for owner",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Address retrieved or created successfully",
                content = @Content(schema = @Schema(implementation = AddressDetailResponseDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing tenant context")
        }
    )
    public ResponseEntity<AddressDetailResponseDTO> getOrCreateSingleAddress(
            @PathVariable OwnerType type,
            @PathVariable Long id) {
        
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok(addressService.getOrCreateSingleAddress(tenantId, type, id));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get address by ID",
        description = "Retrieve address details by ID",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Address retrieved successfully",
                content = @Content(schema = @Schema(implementation = AddressDetailResponseDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing tenant context"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Address not found")
        }
    )
    public ResponseEntity<AddressDetailResponseDTO> getById(
            @PathVariable Long id) {
        
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(addressService.getAddressDetail(tenantId, id));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update address",
        description = "Update an existing address",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Address updated successfully",
                content = @Content(schema = @Schema(implementation = AddressResponseDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid address data or missing tenant context"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Address not found")
        }
    )
    public ResponseEntity<AddressResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequestDTO dto) {
        
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(addressService.updateAddress(tenantId, id, dto));
    }

    // Update address fields - no ownerType/ownerId required
    @PutMapping("/{id}/fields")
    @Operation(
        summary = "Update address fields",
        description = "Update specific fields of an address",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Address fields updated successfully",
                content = @Content(schema = @Schema(implementation = AddressDetailResponseDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid address data or missing tenant context"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Address not found")
        }
    )
    public ResponseEntity<AddressDetailResponseDTO> updateAddressFields(
            @PathVariable Long id,
            @Valid @RequestBody AddressUpdateRequest dto) {
        
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(addressService.updateAddressFields(tenantId, id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete address",
        description = "Delete an address",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Address deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing tenant context"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Address not found")
        }
    )
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {
        
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        addressService.deleteAddress(tenantId, id);
        return ResponseEntity.noContent().build();
    }

    // User-specific endpoints (no tenant required)
    @GetMapping("/user/{type}/{id}")
    public ResponseEntity<AddressDetailResponseDTO> getUserAddress(
            @PathVariable OwnerType type,
            @PathVariable Long id) {
        
        try {
            return ResponseEntity.ok(addressService.getUserAddress(type, id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{type}/{id}/ensure")
    public ResponseEntity<AddressDetailResponseDTO> getOrCreateUserAddress(
            @PathVariable OwnerType type,
            @PathVariable Long id) {
        
        return ResponseEntity.ok(addressService.getOrCreateUserAddress(type, id));
    }

    @PutMapping("/user/{type}/{ownerId}")
    public ResponseEntity<AddressDetailResponseDTO> updateUserAddress(
            @PathVariable OwnerType type,
            @PathVariable Long ownerId,
            @Valid @RequestBody AddressUpdateRequest dto) {
        
        return ResponseEntity.ok(addressService.updateUserAddress(type, ownerId, dto));
    }

    // Tenant-specific update endpoint by tenantKey (UUID)
    @PutMapping("/tenant/{tenantKey}")
    public ResponseEntity<AddressDetailResponseDTO> updateTenantAddress(
            @PathVariable String tenantKey,
            @Valid @RequestBody AddressUpdateRequest dto) {
        
        return ResponseEntity.ok(addressService.updateTenantAddress(tenantKey, dto));
    }
}
