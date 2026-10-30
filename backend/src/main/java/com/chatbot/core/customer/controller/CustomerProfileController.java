package com.chatbot.core.customer.controller;

import com.chatbot.core.customer.dto.*;
import com.chatbot.core.customer.service.CustomerProfileService;
import com.chatbot.core.tenant.infra.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/profiles")
@RequiredArgsConstructor
@Slf4j
public class CustomerProfileController {

    private final CustomerProfileService customerProfileService;

    @PostMapping
    public ResponseEntity<CustomerProfileDTO> createCustomerProfile(@RequestBody CreateCustomerProfileRequest request) {
        Long tenantId = TenantContext.getTenantId();
        CustomerProfileDTO created = customerProfileService.createCustomerProfile(request, tenantId);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerProfileDTO> getCustomerProfile(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        CustomerProfileDTO profile = customerProfileService.getCustomerProfileById(id, tenantId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/external/{externalId}")
    public ResponseEntity<CustomerProfileDTO> getCustomerProfileByExternalId(@PathVariable String externalId) {
        Long tenantId = TenantContext.getTenantId();
        CustomerProfileDTO profile = customerProfileService.getCustomerProfileByExternalId(externalId, tenantId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping
    public ResponseEntity<List<CustomerProfileDTO>> getAllCustomerProfiles() {
        Long tenantId = TenantContext.getTenantId();
        List<CustomerProfileDTO> profiles = customerProfileService.getAllCustomerProfiles(tenantId);
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CustomerProfileDTO>> getCustomerProfilesByStatus(@PathVariable String status) {
        Long tenantId = TenantContext.getTenantId();
        List<CustomerProfileDTO> profiles = customerProfileService.getCustomerProfilesByStatus(tenantId, status);
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/tier/{tier}")
    public ResponseEntity<List<CustomerProfileDTO>> getCustomerProfilesByTier(@PathVariable String tier) {
        Long tenantId = TenantContext.getTenantId();
        List<CustomerProfileDTO> profiles = customerProfileService.getCustomerProfilesByTier(tenantId, tier);
        return ResponseEntity.ok(profiles);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerProfileDTO> updateCustomerProfile(
            @PathVariable Long id,
            @RequestBody UpdateCustomerProfileRequest request) {
        Long tenantId = TenantContext.getTenantId();
        CustomerProfileDTO updated = customerProfileService.updateCustomerProfile(id, request, tenantId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomerProfile(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        customerProfileService.deleteCustomerProfile(id, tenantId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/stats")
    public ResponseEntity<Void> updateConversationStats(
            @PathVariable Long id,
            @RequestBody UpdateStatsRequest request) {
        Long tenantId = TenantContext.getTenantId();
        customerProfileService.updateConversationStats(
                id, tenantId, request.getMessageCount(), request.getResponseTimeSeconds());
        return ResponseEntity.ok().build();
    }
}
