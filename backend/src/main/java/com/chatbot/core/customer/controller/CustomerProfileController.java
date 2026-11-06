package com.chatbot.core.customer.controller;

import com.chatbot.core.customer.dto.*;
import com.chatbot.core.customer.model.CustomerNote;
import com.chatbot.core.customer.service.CustomerNoteService;
import com.chatbot.core.customer.service.CustomerProfileService;
import com.chatbot.core.customer.service.CustomerSegmentationService;
import com.chatbot.core.customer.service.CustomerTimelineService;
import com.chatbot.core.tenant.infra.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer/profiles")
@RequiredArgsConstructor
@Slf4j
public class CustomerProfileController {

    private final CustomerProfileService customerProfileService;
    private final CustomerNoteService customerNoteService;
    private final CustomerTimelineService customerTimelineService;
    private final CustomerSegmentationService customerSegmentationService;

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
        // Phase 4: auto-upgrade tier after stats change (never downgrades)
        try {
            customerSegmentationService.recalculateTier(id, tenantId, false);
        } catch (Exception e) {
            log.warn("[Segmentation] Auto-recalc failed for customer {}: {}", id, e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    // ===== Phase 4: Timeline =====

    @GetMapping("/{id}/timeline")
    public ResponseEntity<List<TimelineEventDTO>> getCustomerTimeline(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        List<TimelineEventDTO> timeline = customerTimelineService.getTimeline(id, tenantId);
        return ResponseEntity.ok(timeline);
    }

    // ===== Phase 4: Notes =====

    @GetMapping("/{id}/notes")
    public ResponseEntity<List<CustomerNoteDTO>> getCustomerNotes(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        List<CustomerNoteDTO> notes = customerNoteService.getNotesForCustomer(id, tenantId)
            .stream().map(CustomerNoteDTO::from).toList();
        return ResponseEntity.ok(notes);
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<CustomerNoteDTO> createCustomerNote(
            @PathVariable Long id,
            @Valid @RequestBody CreateCustomerNoteRequest request) {
        Long tenantId = TenantContext.getTenantId();
        CustomerNote note = customerNoteService.createNote(
            id, request.content(), request.authorId(), request.authorName(), tenantId);
        return ResponseEntity.ok(CustomerNoteDTO.from(note));
    }

    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<Void> deleteCustomerNote(@PathVariable Long noteId) {
        Long tenantId = TenantContext.getTenantId();
        customerNoteService.deleteNote(noteId, tenantId);
        return ResponseEntity.ok().build();
    }

    // ===== Phase 4: Segmentation =====

    @PostMapping("/{id}/recalculate-tier")
    public ResponseEntity<?> recalculateTier(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean force) {
        Long tenantId = TenantContext.getTenantId();
        try {
            var profile = customerSegmentationService.recalculateTier(id, tenantId, force);
            return ResponseEntity.ok(Map.of(
                "id", profile.getId(),
                "tier", profile.getTier() != null ? profile.getTier().name() : null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/recalculate-tiers")
    public ResponseEntity<?> recalculateAllTiers(
            @RequestParam(defaultValue = "false") boolean force) {
        Long tenantId = TenantContext.getTenantId();
        int changed = customerSegmentationService.recalculateAllForTenant(tenantId, force);
        return ResponseEntity.ok(Map.of("changed", changed));
    }
}
