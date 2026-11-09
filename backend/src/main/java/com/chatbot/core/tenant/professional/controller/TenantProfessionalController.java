package com.chatbot.core.tenant.professional.controller;

import com.chatbot.core.tenant.professional.dto.TenantProfessionalRequest;
import com.chatbot.core.tenant.professional.dto.TenantProfessionalResponse;
import com.chatbot.core.tenant.professional.service.TenantProfessionalService;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.tenant.exception.TenantNotFoundException;
import com.chatbot.core.tenant.exception.InsufficientPermissionException;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.tenant.service.TenantPermissionValidator;
import com.chatbot.shared.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tenant")
@RequiredArgsConstructor
public class TenantProfessionalController {

    private final TenantProfessionalService tenantProfessionalService;
    private final TenantRepository tenantRepository;
    private final TenantPermissionValidator permissionValidator;

    private Tenant resolveTenantForRead(String tenantKey) {
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with key: " + tenantKey));
        Long contextTenantId = TenantContext.getTenantId();
        if (contextTenantId == null || !contextTenantId.equals(tenant.getId())) {
            throw new InsufficientPermissionException(ErrorCode.CANNOT_ACCESS_TENANT,
                    "Tenant key does not match current tenant");
        }
        return tenant;
    }

    private Tenant resolveTenantForWrite(String tenantKey) {
        Tenant tenant = resolveTenantForRead(tenantKey);
        String email = permissionValidator.getCurrentUserEmail();
        if (email == null || !permissionValidator.isOwnerOrEditor(tenant.getId(), email)) {
            throw new InsufficientPermissionException(ErrorCode.INSUFFICIENT_PERMISSION,
                    "Only tenant owner or editor can modify professional info");
        }
        return tenant;
    }

    @GetMapping("/professional/{tenantKey}")
    public ResponseEntity<TenantProfessionalResponse> getProfessional(
            @PathVariable String tenantKey
    ) {
        Tenant tenant = resolveTenantForRead(tenantKey);
        
        TenantProfessionalResponse response = tenantProfessionalService.getProfessional(tenant.getId());
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/professional/{tenantKey}")
    public ResponseEntity<TenantProfessionalResponse> updateProfessional(
            @PathVariable String tenantKey,
            @RequestBody TenantProfessionalRequest request
    ) {
        Tenant tenant = resolveTenantForWrite(tenantKey);
        
        return ResponseEntity.ok(
                tenantProfessionalService.upsertProfessional(tenant.getId(), request)
        );
    }

    @DeleteMapping("/professional/{tenantKey}")
    public ResponseEntity<Void> deleteProfessional(
            @PathVariable String tenantKey
    ) {
        Tenant tenant = resolveTenantForWrite(tenantKey);
        
        tenantProfessionalService.deleteProfessional(tenant.getId());
        return ResponseEntity.noContent().build();
    }
}
