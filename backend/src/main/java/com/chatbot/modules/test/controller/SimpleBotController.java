package com.chatbot.modules.test.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Simple Bot Controller for testing (no authentication required)
 */
@RestController
@RequestMapping("/api/test/simple-bots")
public class SimpleBotController {
    
    /**
     * Create bot without authentication
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createBot(@RequestBody Map<String, String> request) {
        String botName = request.get("botName");
        String botType = request.getOrDefault("botType", "CUSTOMER_SERVICE");
        String description = request.getOrDefault("description", "Test bot created via API");
        
        UUID botId = UUID.randomUUID();
        
        Map<String, Object> response = Map.of(
            "id", botId.toString(),
            "botName", botName,
            "botType", botType,
            "description", description,
            "status", "ACTIVE",
            "isActive", true,
            "isEnabled", true,
            "createdAt", System.currentTimeMillis(),
            "message", "Bot created successfully"
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all bots
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getBots() {
        Map<String, Object> response = Map.of(
            "bots", java.util.List.of(
                Map.of(
                    "id", UUID.randomUUID().toString(),
                    "botName", "Customer Service Bot",
                    "botType", "CUSTOMER_SERVICE",
                    "status", "ACTIVE",
                    "isActive", true,
                    "isEnabled", true,
                    "createdAt", System.currentTimeMillis()
                ),
                Map.of(
                    "id", UUID.randomUUID().toString(),
                    "botName", "Sales Bot",
                    "botType", "SALES",
                    "status", "ACTIVE",
                    "isActive", true,
                    "isEnabled", true,
                    "createdAt", System.currentTimeMillis()
                )
            ),
            "total", 2,
            "message", "Bots retrieved successfully"
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get bot by ID
     */
    @GetMapping("/{botId}")
    public ResponseEntity<Map<String, Object>> getBot(@PathVariable String botId) {
        Map<String, Object> bot = Map.of(
            "id", botId,
            "botName", "Test Bot " + botId.substring(0, 8),
            "botType", "CUSTOMER_SERVICE",
            "status", "ACTIVE",
            "isActive", true,
            "isEnabled", true,
            "description", "Test bot description",
            "createdAt", System.currentTimeMillis(),
            "message", "Bot retrieved successfully"
        );
        
        return ResponseEntity.ok(bot);
    }
}
