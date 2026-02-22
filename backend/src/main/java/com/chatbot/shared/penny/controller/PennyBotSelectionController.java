package com.chatbot.shared.penny.controller;

import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.shared.penny.service.PennyBotManager;
import com.chatbot.shared.penny.model.PennyBot;
import com.chatbot.shared.penny.model.PennyBotType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Penny Bot Selection Controller - API for selecting bots in connections
 */
@RestController
@RequestMapping("/api/penny/bots-selection")
@Slf4j
public class PennyBotSelectionController {
    
    private final PennyBotManager pennyBotManager;
    
    public PennyBotSelectionController(PennyBotManager pennyBotManager) {
        this.pennyBotManager = pennyBotManager;
    }
    
    /**
     * Get available bots for current tenant (for connection selection)
     */
    @GetMapping("/available")
    public ResponseEntity<List<Map<String, Object>>> getAvailableBots(Principal principal) {
        // ✅ Validate tenant context
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(List.of(Map.of(
                "error", "Tenant context not found. Please provide X-Tenant-Key header"
            )));
        }
        
        String ownerId = principal.getName();
        log.info("📋 Getting available Penny bots for owner: {} in tenant: {}", ownerId, tenantId);
        
        List<PennyBot> bots = pennyBotManager.getBotsForCurrentTenant();
        
        List<Map<String, Object>> response = bots.stream()
            .map(bot -> {
                Map<String, Object> botMap = new java.util.HashMap<>();
                botMap.put("botId", bot.getId().toString());
                botMap.put("botName", bot.getBotName());
                botMap.put("botType", bot.getBotType().name());
                botMap.put("botTypeDisplay", bot.getBotType().getDisplayName());
                botMap.put("botpressBotId", bot.getBotpressBotId());
                botMap.put("isActive", bot.isActive());
                botMap.put("isEnabled", bot.isEnabled());
                botMap.put("createdAt", bot.getCreatedAt().toString());
                botMap.put("description", bot.getDescription() != null ? bot.getDescription() : "");
                return botMap;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get bot types available for creation
     */
    @GetMapping("/bot-types")
    public ResponseEntity<List<Map<String, Object>>> getBotTypes() {
        List<Map<String, Object>> botTypes = java.util.Arrays.stream(PennyBotType.values())
            .map(type -> {
                Map<String, Object> typeMap = new java.util.HashMap<>();
                typeMap.put("type", type.name());
                typeMap.put("displayName", type.getDisplayName());
                typeMap.put("botpressBotId", type.getBotpressBotId());
                return typeMap;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(botTypes);
    }
    
    /**
     * Check if tenant can create bot of specific type
     */
    @GetMapping("/can-create/{botType}")
    public ResponseEntity<Map<String, Object>> canCreateBotType(@PathVariable String botType) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Tenant context not found. Please provide X-Tenant-Key header"
            ));
        }
        
        PennyBotType type = PennyBotType.fromString(botType);
        
        // Check if tenant already has bot of this type
        List<PennyBot> existingBots = pennyBotManager.getBotsForCurrentTenant();
        boolean hasType = existingBots.stream()
            .anyMatch(bot -> bot.getBotType() == type);
        
        return ResponseEntity.ok(Map.of(
            "canCreate", !hasType,
            "botType", type.name(),
            "displayName", type.getDisplayName(),
            "message", hasType ? 
                "Tenant already has " + type.getDisplayName() + " bot" : 
                "Can create " + type.getDisplayName() + " bot"
        ));
    }
}
