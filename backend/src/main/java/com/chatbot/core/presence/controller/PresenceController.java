package com.chatbot.core.presence.controller;

import com.chatbot.core.presence.service.PresenceService;
import com.chatbot.core.tenant.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/presence")
@RequiredArgsConstructor
@Slf4j
public class PresenceController {

    private final PresenceService presenceService;
    private final TenantService tenantService;

    /**
     * API Lấy danh sách thành viên đang Online (Load lần đầu)
     * Endpoint: GET /api/presence/tenants/key/{tenantKey}/members/online
     */
    @GetMapping("/tenants/key/{tenantKey}/members/online")
    public ResponseEntity<?> getOnlineMembers(@PathVariable String tenantKey) {
        try {
            Long tenantId = tenantService.getTenantIdByKey(tenantKey);
            if (tenantId == null) {
                return ResponseEntity.notFound().build();
            }

            List<Map<String, Object>> onlineMembers = presenceService.getOnlineMembers(tenantId);
            log.info("📋 [Presence] Retrieved {} online members for tenant {}", onlineMembers.size(), tenantKey);
            
            return ResponseEntity.ok(onlineMembers);
        } catch (Exception e) {
            log.error("[Presence] Error getting online members: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to get online members",
                "message", e.getMessage()
            ));
        }
    }
}
