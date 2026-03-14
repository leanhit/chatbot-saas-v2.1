package com.chatbot.spokes.facebook.token.controller;

import com.chatbot.spokes.facebook.token.service.FacebookTokenManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/facebook/tokens")
@Slf4j
public class TokenHealthController {

    private final FacebookTokenManager tokenManager;

    public TokenHealthController(FacebookTokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    /**
     * Check token status for a specific page
     */
    @GetMapping("/status/{pageId}")
    public ResponseEntity<Map<String, Object>> getTokenStatus(@PathVariable String pageId) {
        try {
            FacebookTokenManager.TokenStatus status = tokenManager.getTokenStatus(pageId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("pageId", pageId);
            response.put("status", status.name());
            response.put("timestamp", System.currentTimeMillis());
            
            switch (status) {
                case VALID:
                    response.put("message", "Token is valid and ready to use");
                    break;
                case EXPIRING_SOON:
                    response.put("message", "Token is expiring soon and will be refreshed on next use");
                    response.put("warning", true);
                    break;
                case EXPIRED:
                    response.put("message", "Token is expired and needs refresh");
                    response.put("error", true);
                    break;
                case UNKNOWN:
                    response.put("message", "Token status unknown - no expiry information available");
                    response.put("warning", true);
                    break;
                case NOT_FOUND:
                    response.put("message", "No active connection found for this page");
                    response.put("error", true);
                    break;
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error checking token status for page: {}", pageId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("pageId", pageId);
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Failed to check token status: " + e.getMessage());
            errorResponse.put("error", true);
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Force refresh token for a specific page
     */
    @PostMapping("/refresh/{pageId}")
    public ResponseEntity<Map<String, Object>> forceRefreshToken(@PathVariable String pageId) {
        try {
            boolean success = tokenManager.forceRefreshToken(pageId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("pageId", pageId);
            response.put("success", success);
            response.put("timestamp", System.currentTimeMillis());
            
            if (success) {
                response.put("message", "Token successfully refreshed");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Token refresh failed - check logs for details");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("❌ Error force refreshing token for page: {}", pageId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("pageId", pageId);
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to refresh token: " + e.getMessage());
            errorResponse.put("error", true);
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get valid token (with JIT refresh if needed)
     */
    @GetMapping("/valid/{pageId}")
    public ResponseEntity<Map<String, Object>> getValidToken(@PathVariable String pageId) {
        try {
            String token = tokenManager.getValidToken(pageId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("pageId", pageId);
            response.put("token", token);
            response.put("tokenLength", token.length());
            response.put("timestamp", System.currentTimeMillis());
            
            // Don't return the full token in production for security
            // This endpoint is mainly for testing/debugging
            response.put("message", "Valid token retrieved (masked for security)");
            response.put("tokenPreview", token.substring(0, Math.min(10, token.length())) + "...");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error getting valid token for page: {}", pageId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("pageId", pageId);
            errorResponse.put("error", true);
            errorResponse.put("message", "Failed to get valid token: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
