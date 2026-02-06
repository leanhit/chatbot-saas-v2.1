package com.chatbot.modules.test.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Simple Bot Rule Controller for testing
 */
@RestController
@RequestMapping("/api/test/bot-rules")
public class BotRuleTestController {
    
    /**
     * Create bot rule
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createBotRule(@RequestBody Map<String, Object> request) {
        String ruleName = (String) request.get("ruleName");
        String ruleType = (String) request.getOrDefault("ruleType", "KEYWORD_MATCH");
        String pattern = (String) request.get("pattern");
        String response = (String) request.get("response");
        String botId = (String) request.get("botId");
        
        UUID ruleId = UUID.randomUUID();
        
        Map<String, Object> rule = Map.of(
            "id", ruleId.toString(),
            "ruleName", ruleName,
            "ruleType", ruleType,
            "pattern", pattern,
            "response", response,
            "botId", botId,
            "isActive", true,
            "priority", 1,
            "createdAt", System.currentTimeMillis(),
            "message", "Bot rule created successfully"
        );
        
        return ResponseEntity.ok(rule);
    }
    
    /**
     * Get all rules for a bot
     */
    @GetMapping("/bot/{botId}")
    public ResponseEntity<Map<String, Object>> getBotRules(@PathVariable String botId) {
        Map<String, Object> response = Map.of(
            "botId", botId,
            "rules", java.util.List.of(
                Map.of(
                    "id", UUID.randomUUID().toString(),
                    "ruleName", "Greeting Rule",
                    "ruleType", "KEYWORD_MATCH",
                    "pattern", "hello|hi|xin chào",
                    "response", "Xin chào! Tôi có thể giúp gì cho bạn?",
                    "isActive", true,
                    "priority", 1
                ),
                Map.of(
                    "id", UUID.randomUUID().toString(),
                    "ruleName", "Help Rule",
                    "ruleType", "KEYWORD_MATCH", 
                    "pattern", "help|giúp đỡ|hỗ trợ",
                    "response", "Tôi có thể giúp bạn tìm thông tin sản phẩm, đặt hàng, và giải đáp thắc mắc.",
                    "isActive", true,
                    "priority", 2
                )
            ),
            "message", "Bot rules retrieved successfully"
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update bot rule
     */
    @PutMapping("/{ruleId}")
    public ResponseEntity<Map<String, Object>> updateBotRule(
            @PathVariable String ruleId,
            @RequestBody Map<String, Object> request) {
        
        Map<String, Object> updatedRule = Map.of(
            "id", ruleId,
            "ruleName", request.get("ruleName"),
            "ruleType", request.getOrDefault("ruleType", "KEYWORD_MATCH"),
            "pattern", request.get("pattern"),
            "response", request.get("response"),
            "isActive", request.getOrDefault("isActive", true),
            "priority", request.getOrDefault("priority", 1),
            "updatedAt", System.currentTimeMillis(),
            "message", "Bot rule updated successfully"
        );
        
        return ResponseEntity.ok(updatedRule);
    }
    
    /**
     * Delete bot rule
     */
    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Map<String, String>> deleteBotRule(@PathVariable String ruleId) {
        return ResponseEntity.ok(Map.of(
            "message", "Bot rule deleted successfully",
            "ruleId", ruleId
        ));
    }
}
