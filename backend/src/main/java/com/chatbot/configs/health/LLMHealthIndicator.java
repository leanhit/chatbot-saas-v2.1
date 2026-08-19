package com.chatbot.configs.health;

import com.chatbot.spokes.pennybot.service.ClaudeProviderService;
import com.chatbot.spokes.pennybot.service.GptProviderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * LLM Health Indicator - Checks connectivity to OpenAI and Claude providers
 * This indicator is always UP if no providers are configured, and only reports DOWN
 * if configured providers are actually unhealthy.
 */
@Component
@Slf4j
public class LLMHealthIndicator implements HealthIndicator {

    @Autowired(required = false)
    private GptProviderService gptProviderService;

    @Autowired(required = false)
    private ClaudeProviderService claudeProviderService;

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        
        try {
            boolean openaiHealthy = checkOpenAIHealth(details);
            boolean claudeHealthy = checkClaudeHealth(details);
            
            // If neither provider is configured, return UP with a message
            if (!openaiHealthy && !claudeHealthy && 
                "NOT_CONFIGURED".equals(details.get("openai")) && 
                "NOT_CONFIGURED".equals(details.get("claude"))) {
                details.put("status", "NO_LLM_PROVIDERS_CONFIGURED");
                return Health.up()
                    .withDetails(details)
                    .build();
            }
            
            boolean overallHealthy = openaiHealthy || claudeHealthy;
            
            if (overallHealthy) {
                return Health.up()
                    .withDetails(details)
                    .build();
            } else {
                return Health.down()
                    .withDetails(details)
                    .build();
            }
        } catch (Exception e) {
            log.error("❌ LLM health check unexpected error: {}", e.getMessage());
            details.put("status", "ERROR");
            details.put("error", e.getMessage());
            // Return UP to avoid bringing down the entire health check
            return Health.up()
                .withDetails(details)
                .build();
        }
    }

    private boolean checkOpenAIHealth(Map<String, Object> details) {
        try {
            if (gptProviderService == null) {
                details.put("openai", "NOT_CONFIGURED");
                return false;
            }
            
            boolean healthy = gptProviderService.healthCheck("system-check");
            details.put("openai", healthy ? "UP" : "DOWN");
            details.put("openaiModel", "gpt-4o-mini");
            
            if (healthy) {
                log.debug("✅ OpenAI health check passed");
            } else {
                log.warn("⚠️ OpenAI health check failed");
            }
            
            return healthy;
        } catch (Exception e) {
            log.error("❌ OpenAI health check error: {}", e.getMessage());
            details.put("openai", "ERROR");
            details.put("openaiError", e.getMessage());
            return false;
        }
    }

    private boolean checkClaudeHealth(Map<String, Object> details) {
        try {
            if (claudeProviderService == null) {
                details.put("claude", "NOT_CONFIGURED");
                return false;
            }
            
            boolean healthy = claudeProviderService.healthCheck("system-check");
            details.put("claude", healthy ? "UP" : "DOWN");
            details.put("claudeModel", "claude-3-haiku-20240307");
            
            if (healthy) {
                log.debug("✅ Claude health check passed");
            } else {
                log.warn("⚠️ Claude health check failed");
            }
            
            return healthy;
        } catch (Exception e) {
            log.error("❌ Claude health check error: {}", e.getMessage());
            details.put("claude", "ERROR");
            details.put("claudeError", e.getMessage());
            return false;
        }
    }
}
