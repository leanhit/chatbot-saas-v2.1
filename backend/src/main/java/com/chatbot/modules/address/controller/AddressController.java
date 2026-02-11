package com.chatbot.modules.address.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chatbot.modules.address.dto.*;
import com.chatbot.modules.address.dto.AddressDetailResponseDTO;
import com.chatbot.modules.address.model.OwnerType;
import com.chatbot.modules.address.service.AddressService;
import com.chatbot.core.tenant.infra.TenantContext;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
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
    public ResponseEntity<AddressDetailResponseDTO> getById(
            @PathVariable Long id) {
        
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(addressService.getAddressDetail(tenantId, id));
    }

    @PutMapping("/{id}")
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
