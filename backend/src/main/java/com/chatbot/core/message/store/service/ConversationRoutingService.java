package com.chatbot.core.message.store.service;

import com.chatbot.core.message.store.model.Conversation;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for routing conversations based on attributes
 * Implements attribute-based routing logic for Phase 1.3
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationRoutingService {

    private final ObjectMapper objectMapper;

    /**
     * Determine routing priority based on conversation attributes
     * Returns a priority level: urgent, high, medium, low
     */
    public String determineRoutingPriority(Conversation conversation) {
        // VIP customers get high priority
        if ("VIP".equals(conversation.getCustomerTier()) || "Enterprise".equals(conversation.getCustomerTier())) {
            return "high";
        }

        // Check custom attributes for priority flags
        if (conversation.getCustomAttributes() != null) {
            // Parse custom attributes if needed
            // For now, return medium priority
        }

        return "medium";
    }

    /**
     * Determine if conversation should be routed to specific team based on language
     * Returns team name or null for default routing
     */
    public String determineRoutingTeam(Conversation conversation) {
        String language = conversation.getLanguage();
        
        if (language != null) {
            switch (language.toLowerCase()) {
                case "vi":
                    return "vietnamese_support";
                case "en":
                    return "english_support";
                case "ja":
                    return "japanese_support";
                default:
                    return "default_support";
            }
        }

        return null;
    }

    /**
     * Update conversation attributes based on user info
     * This can be called when creating or updating a conversation
     */
    public void updateConversationAttributes(Conversation conversation, Map<String, Object> userInfo) {
        if (userInfo == null) return;

        // Update language if provided
        if (userInfo.containsKey("language")) {
            conversation.setLanguage((String) userInfo.get("language"));
        }

        // Update customer tier if provided
        if (userInfo.containsKey("customerTier")) {
            conversation.setCustomerTier((String) userInfo.get("customerTier"));
        }

        // Update custom attributes if provided
        if (userInfo.containsKey("customAttributes")) {
            try {
                conversation.setCustomAttributes(objectMapper.writeValueAsString(userInfo.get("customAttributes")));
            } catch (Exception e) {
                log.error("Failed to serialize custom attributes", e);
            }
        }
    }

    /**
     * Check if conversation should be escalated based on attributes
     * Returns true if escalation is needed
     */
    @SuppressWarnings("unchecked")
    public boolean shouldEscalate(Conversation conversation) {
        // VIP customers always get priority handling
        if ("VIP".equals(conversation.getCustomerTier())) {
            return true;
        }

        // Check for escalation flags in custom attributes
        if (conversation.getCustomAttributes() != null) {
            try {
                Map<String, Object> attrs = objectMapper.readValue(
                    conversation.getCustomAttributes(),
                    Map.class
                );
                if (Boolean.TRUE.equals(attrs.get("escalationRequired"))) {
                    return true;
                }
            } catch (Exception e) {
                log.error("Failed to parse custom attributes", e);
            }
        }

        return false;
    }
}
