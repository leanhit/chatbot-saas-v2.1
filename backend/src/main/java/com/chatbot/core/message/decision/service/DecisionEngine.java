package com.chatbot.core.message.decision.service;

import com.chatbot.core.message.decision.model.Decision;
import com.chatbot.core.message.decision.model.DecisionType;
import com.chatbot.core.message.decision.model.ProcessingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Decision Engine - Makes routing decisions for messages
 */
@Service
@Slf4j
public class DecisionEngine {
    
    @Autowired
    private HubSelectionService hubSelectionService;
    
    /**
     * Make comprehensive routing decision for message
     */
    public Decision makeDecision(ProcessingContext context) {
        log.info("🧠 [DecisionEngine] Making decision for message: {}", context.getMessageId());
        
        Decision decision = new Decision();
        decision.setMessageId(context.getMessageId());
        decision.setConversationId(context.getConversationId());
        decision.setCreatedAt(java.time.LocalDateTime.now());
        
        try {
            // Analyze message context
            MessageAnalysis analysis = analyzeMessage(context);
            
            // Determine decision type
            DecisionType decisionType = determineDecisionType(analysis, context);
            decision.setDecisionType(decisionType.name());
            
            // Select appropriate hub
            var hubSelection = hubSelectionService.selectHub(context);
            decision.setSelectedHub(hubSelection.getSelectedHub().getName());
            decision.setSelectedService(determineSelectedService(analysis, hubSelection));
            
            // Set reasoning
            decision.setReason(generateReasoning(analysis, hubSelection, decisionType));
            
            // Set context data
            decision.setContext(createDecisionContext(analysis, hubSelection, context));
            
            log.info("✅ [DecisionEngine] Decision made for {}: {} -> {} (type: {})", 
                     context.getMessageId(), decision.getSelectedHub(), 
                     decision.getSelectedService(), decision.getDecisionType());
            
            return decision;
            
        } catch (Exception e) {
            log.error("❌ [DecisionEngine] Decision making failed for message: {}", context.getMessageId(), e);
            
            // Fallback decision
            decision.setDecisionType(DecisionType.ROUTE_TO_HUB.name());
            decision.setSelectedHub("message");
            decision.setSelectedService("message-store");
            decision.setReason("Fallback decision due to error: " + e.getMessage());
            decision.setContext(new HashMap<>());
            
            return decision;
        }
    }
    
    /**
     * Legacy method for simple destination decision
     */
    public String decideDestination(String messageContent, String context) {
        try {
            log.info("🔄 [DecisionEngine] Legacy destination decision for message");
            
            // Create basic context
            ProcessingContext processingContext = new ProcessingContext();
            processingContext.setMessageId("legacy-" + System.currentTimeMillis());
            processingContext.setContent(messageContent);
            processingContext.setMessageType("text");
            processingContext.setSystemData(Map.of("legacyContext", context));
            
            // Make full decision
            Decision decision = makeDecision(processingContext);
            
            return decision.getSelectedHub();
            
        } catch (Exception e) {
            log.error("❌ [DecisionEngine] Legacy decision failed", e);
            return "message"; // Default fallback
        }
    }
    
    /**
     * Analyze message for decision making
     */
    private MessageAnalysis analyzeMessage(ProcessingContext context) {
        MessageAnalysis analysis = new MessageAnalysis();
        
        String content = context.getContent();
        if (content == null) content = "";
        
        // Basic content analysis
        analysis.setContent(content);
        analysis.setLength(content.length());
        analysis.setWordCount(content.split("\\s+").length);
        
        // Content characteristics
        analysis.setHasUrls(containsUrls(content));
        analysis.setHasEmails(containsEmails(content));
        analysis.setHasPhoneNumbers(containsPhoneNumbers(content));
        analysis.setHasAttachments(hasAttachments(context));
        analysis.setRequiresRealTime(requiresRealTime(context));
        
        // User and conversation analysis
        analysis.setUserId(context.getUserId());
        analysis.setIsPremiumUser(isPremiumUser(context.getUserId()));
        analysis.setIsReturningUser(isReturningUser(context.getUserId()));
        analysis.setConversationId(context.getConversationId());
        analysis.setEscalated(isEscalatedConversation(context.getConversationId()));
        analysis.setActiveTakeover(hasActiveTakeover(context.getConversationId()));
        
        // System analysis
        analysis.setSystemLoad(getCurrentSystemLoad());
        analysis.setBusinessHours(isBusinessHours());
        
        return analysis;
    }
    
    /**
     * Determine decision type based on analysis
     */
    private DecisionType determineDecisionType(MessageAnalysis analysis, ProcessingContext context) {
        // Priority-based decision type selection
        
        // 1. Escalation has highest priority
        if (analysis.isEscalated() || analysis.isActiveTakeover()) {
            return DecisionType.ROUTE_TO_HUB; // Route to agent hub
        }
        
        // 2. Real-time requirements
        if (analysis.getRequiresRealTime()) {
            return DecisionType.ROUTE_TO_HUB; // Route to realtime hub
        }
        
        // 3. Premium user routing
        if (analysis.getIsPremiumUser()) {
            return DecisionType.ROUTE_TO_HUB; // Route to premium hub
        }
        
        // 4. Complex content (attachments, URLs)
        if (analysis.getHasAttachments() || analysis.getHasUrls()) {
            return DecisionType.ROUTE_TO_SERVICE; // Route to processing service
        }
        
        // 5. Default routing
        return DecisionType.ROUTE_TO_HUB;
    }
    
    /**
     * Determine selected service
     */
    private String determineSelectedService(MessageAnalysis analysis, HubSelectionService.HubSelection hubSelection) {
        String hubName = hubSelection.getSelectedHub().getName();
        
        switch (hubName) {
            case "agent":
                return "agent-service";
            case "realtime":
                return "realtime-service";
            case "escalation":
                return "escalation-service";
            case "premium":
                return "premium-service";
            case "message":
            default:
                return "message-store";
        }
    }
    
    /**
     * Generate reasoning for decision
     */
    private String generateReasoning(MessageAnalysis analysis, HubSelectionService.HubSelection hubSelection, DecisionType decisionType) {
        StringBuilder reasoning = new StringBuilder();
        
        reasoning.append("Decision type: ").append(decisionType.name()).append(". ");
        
        // Add hub selection reasoning
        reasoning.append("Selected hub '").append(hubSelection.getSelectedHub().getName())
                .append("' with confidence ").append(String.format("%.2f", hubSelection.getConfidence())).append(". ");
        
        // Add key factors
        if (analysis.isActiveTakeover()) {
            reasoning.append("Active agent takeover detected. ");
        }
        
        if (analysis.getRequiresRealTime()) {
            reasoning.append("Real-time processing required. ");
        }
        
        if (analysis.getIsPremiumUser()) {
            reasoning.append("Premium user routing. ");
        }
        
        if (analysis.getHasAttachments()) {
            reasoning.append("Message contains attachments. ");
        }
        
        if (analysis.getSystemLoad() > 0.8) {
            reasoning.append("High system load considered. ");
        }
        
        return reasoning.toString().trim();
    }
    
    /**
     * Create decision context
     */
    private Map<String, Object> createDecisionContext(MessageAnalysis analysis, HubSelectionService.HubSelection hubSelection, ProcessingContext originalContext) {
        Map<String, Object> decisionContext = new HashMap<>();
        
        // Analysis results
        decisionContext.put("messageLength", analysis.getLength());
        decisionContext.put("wordCount", analysis.getWordCount());
        decisionContext.put("hasUrls", analysis.getHasUrls());
        decisionContext.put("hasEmails", analysis.getHasEmails());
        decisionContext.put("hasAttachments", analysis.getHasAttachments());
        decisionContext.put("requiresRealTime", analysis.getRequiresRealTime());
        
        // User context
        decisionContext.put("isPremiumUser", analysis.getIsPremiumUser());
        decisionContext.put("isReturningUser", analysis.getIsReturningUser());
        decisionContext.put("isEscalated", analysis.isEscalated());
        decisionContext.put("hasActiveTakeover", analysis.isActiveTakeover());
        
        // Hub selection results
        decisionContext.put("selectedHub", hubSelection.getSelectedHub().getName());
        decisionContext.put("hubConfidence", hubSelection.getConfidence());
        decisionContext.put("fallbackHubs", hubSelection.getFallbackHubs().stream()
                .map(h -> h.getName()).toList());
        
        // System context
        decisionContext.put("systemLoad", analysis.getSystemLoad());
        decisionContext.put("businessHours", analysis.isBusinessHours());
        
        return decisionContext;
    }
    
    // Helper methods
    private boolean containsUrls(String content) {
        return content.toLowerCase().matches(".*https?://.*");
    }
    
    private boolean containsEmails(String content) {
        return content.toLowerCase().matches(".*[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}.*");
    }
    
    private boolean containsPhoneNumbers(String content) {
        return content.matches(".*\\b\\d{10,}\\b.*");
    }
    
    private boolean hasAttachments(ProcessingContext context) {
        return context.getSystemData() != null && 
               context.getSystemData().containsKey("hasAttachments") &&
               (Boolean) context.getSystemData().get("hasAttachments");
    }
    
    private boolean requiresRealTime(ProcessingContext context) {
        return context.getSystemData() != null && 
               context.getSystemData().containsKey("requiresRealTime") &&
               (Boolean) context.getSystemData().get("requiresRealTime");
    }
    
    private boolean isPremiumUser(String userId) {
        // Implementation would check user premium status
        return false;
    }
    
    private boolean isReturningUser(String userId) {
        // Implementation would check user history
        return false;
    }
    
    private boolean isEscalatedConversation(String conversationId) {
        // Implementation would check escalation status
        return false;
    }
    
    private boolean hasActiveTakeover(String conversationId) {
        // Implementation would check takeover status
        return false;
    }
    
    private double getCurrentSystemLoad() {
        // Implementation would get actual system load
        return 0.4;
    }
    
    private boolean isBusinessHours() {
        // Implementation would check business hours
        return true;
    }
    
    // Data classes
    public static class MessageAnalysis {
        private String content;
        private int length;
        private int wordCount;
        private boolean hasUrls;
        private boolean hasEmails;
        private boolean hasPhoneNumbers;
        private boolean hasAttachments;
        private boolean requiresRealTime;
        private String userId;
        private boolean isPremiumUser;
        private boolean isReturningUser;
        private String conversationId;
        private boolean isEscalated;
        private boolean hasActiveTakeover;
        private double systemLoad;
        private boolean businessHours;
        
        // Getters and setters
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public int getLength() { return length; }
        public void setLength(int length) { this.length = length; }
        
        public int getWordCount() { return wordCount; }
        public void setWordCount(int wordCount) { this.wordCount = wordCount; }
        
        public boolean getHasUrls() { return hasUrls; }
        public void setHasUrls(boolean hasUrls) { this.hasUrls = hasUrls; }
        
        public boolean getHasEmails() { return hasEmails; }
        public void setHasEmails(boolean hasEmails) { this.hasEmails = hasEmails; }
        
        public boolean getHasPhoneNumbers() { return hasPhoneNumbers; }
        public void setHasPhoneNumbers(boolean hasPhoneNumbers) { this.hasPhoneNumbers = hasPhoneNumbers; }
        
        public boolean getHasAttachments() { return hasAttachments; }
        public void setHasAttachments(boolean hasAttachments) { this.hasAttachments = hasAttachments; }
        
        public boolean getRequiresRealTime() { return requiresRealTime; }
        public void setRequiresRealTime(boolean requiresRealTime) { this.requiresRealTime = requiresRealTime; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public boolean getIsPremiumUser() { return isPremiumUser; }
        public void setIsPremiumUser(boolean isPremiumUser) { this.isPremiumUser = isPremiumUser; }
        
        public boolean getIsReturningUser() { return isReturningUser; }
        public void setIsReturningUser(boolean isReturningUser) { this.isReturningUser = isReturningUser; }
        
        public String getConversationId() { return conversationId; }
        public void setConversationId(String conversationId) { this.conversationId = conversationId; }
        
        public boolean getIsEscalated() { return isEscalated; }
        public void setEscalated(boolean isEscalated) { this.isEscalated = isEscalated; }
        
        public boolean getHasActiveTakeover() { return hasActiveTakeover; }
        public void setActiveTakeover(boolean activeTakeover) { this.hasActiveTakeover = activeTakeover; }
        
        public boolean isEscalated() { return isEscalated; }
        public boolean isActiveTakeover() { return hasActiveTakeover; }
        
        public double getSystemLoad() { return systemLoad; }
        public void setSystemLoad(double systemLoad) { this.systemLoad = systemLoad; }
        
        public boolean isBusinessHours() { return businessHours; }
        public void setBusinessHours(boolean businessHours) { this.businessHours = businessHours; }
    }
}
