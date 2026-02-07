package com.chatbot.modules.test.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

/**
 * Test Bot Controller - Simple bot creation test
 */
@RestController
@RequestMapping("/api/test/bots")
public class TestBotController {
    
    /**
     * Test bot creation endpoint
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createTestBot(
            @RequestBody Map<String, String> request,
            Principal principal) {
        
        String botName = request.get("botName");
        String botType = request.getOrDefault("botType", "CUSTOMER_SERVICE");
        String description = request.getOrDefault("description", "Test bot created via API");
        
        // Generate test bot data
        UUID botId = UUID.randomUUID();
        UUID userId = UUID.fromString(principal.getName());
        
        Map<String, Object> response = Map.of(
            "id", botId.toString(),
            "botName", botName,
            "botType", botType,
            "description", description,
            "userId", userId,
            "status", "ACTIVE",
            "createdAt", System.currentTimeMillis(),
            "message", "Test bot created successfully"
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get test bot info
     */
    @GetMapping("/{botId}")
    public ResponseEntity<Map<String, Object>> getTestBot(@PathVariable UUID botId) {
        Map<String, Object> bot = Map.of(
            "id", botId.toString(),
            "botName", "Test Bot " + botId.toString().substring(0, 8),
            "botType", "CUSTOMER_SERVICE",
            "status", "ACTIVE",
            "message", "Test bot retrieved successfully"
        );
        
        return ResponseEntity.ok(bot);
    }
}
