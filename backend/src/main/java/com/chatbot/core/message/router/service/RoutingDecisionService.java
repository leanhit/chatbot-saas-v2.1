package com.chatbot.core.message.router.service;

import com.chatbot.core.message.router.model.Route;
import com.chatbot.core.message.router.model.RoutingRule;
import com.chatbot.core.message.router.model.Destination;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Routing Decision Service - Makes routing decisions for messages
 */
@Service
@Slf4j
public class RoutingDecisionService {
    
    private final MessageRouterService messageRouterService;
    
    public RoutingDecisionService(MessageRouterService messageRouterService) {
        this.messageRouterService = messageRouterService;
    }
    
    /**
     * Make routing decision for message
     */
    public RoutingDecision makeRoutingDecision(String messageId, String content, Map<String, Object> context) {
        log.debug("Making routing decision for message: {}", messageId);
        
        RoutingDecision decision = new RoutingDecision();
        decision.setMessageId(messageId);
        decision.setTimestamp(System.currentTimeMillis());
        
        // Analyze message content
        MessageAnalysis analysis = analyzeMessage(content, context);
        decision.setAnalysis(analysis);
        
        // Apply routing rules
        RoutingRule selectedRule = selectRoutingRule(analysis, context);
        decision.setAppliedRule(selectedRule);
        
        // Select destination
        Destination destination = selectDestination(selectedRule, analysis, context);
        decision.setDestination(destination);
        
        // Set decision confidence
        decision.setConfidence(calculateConfidence(analysis, selectedRule));
        
        log.info("Routing decision for {}: {} -> {} (confidence: {})", 
                 messageId, destination.getName(), decision.getConfidence());
        
        return decision;
    }
    
    /**
     * Analyze message content
     */
    private MessageAnalysis analyzeMessage(String content, Map<String, Object> context) {
        MessageAnalysis analysis = new MessageAnalysis();
        
        // Basic analysis
        analysis.setContent(content);
        analysis.setLength(content.length());
        analysis.setWordCount(content.split("\\s+").length);
        
        // Content type analysis
        analysis.setHasUrls(containsUrls(content));
        analysis.setHasEmails(containsEmails(content));
        analysis.setHasPhoneNumbers(containsPhoneNumbers(content));
        analysis.setHasSpecialChars(containsSpecialChars(content));
        
        // Sentiment analysis (simplified)
        analysis.setSentiment(analyzeSentiment(content));
        
        // Intent analysis (simplified)
        analysis.setIntent(detectIntent(content));
        
        // Context analysis
        analysis.setChannel((String) context.getOrDefault("channel", "unknown"));
        analysis.setUserId((String) context.getOrDefault("userId", "unknown"));
        analysis.setSessionId((String) context.getOrDefault("sessionId", "unknown"));
        
        return analysis;
    }
    
    /**
     * Select routing rule based on analysis
     */
    private RoutingRule selectRoutingRule(MessageAnalysis analysis, Map<String, Object> context) {
        // This would typically query a database of routing rules
        // For now, implement basic rule selection logic
        
        // Priority-based rule selection
        List<RoutingRule> applicableRules = getApplicableRules(analysis, context);
        
        if (applicableRules.isEmpty()) {
            return createDefaultRule();
        }
        
        // Select highest priority rule
        return applicableRules.stream()
                .min((r1, r2) -> r1.getPriority().compareTo(r2.getPriority()))
                .orElse(createDefaultRule());
    }
    
    /**
     * Get applicable routing rules
     */
    private List<RoutingRule> getApplicableRules(MessageAnalysis analysis, Map<String, Object> context) {
        List<RoutingRule> rules = new ArrayList<>();
        
        // Intent-based rules
        if ("question".equals(analysis.getIntent())) {
            rules.add(createIntentRule("question", "botpress"));
        }
        
        // Content-based rules
        if (analysis.getHasUrls()) {
            rules.add(createContentRule("url", "human_agent"));
        }
        
        // Channel-specific rules
        if ("facebook".equals(analysis.getChannel())) {
            rules.add(createChannelRule("facebook", "facebook_handler"));
        }
        
        // Sentiment-based rules
        if ("negative".equals(analysis.getSentiment())) {
            rules.add(createSentimentRule("negative", "human_agent"));
        }
        
        return rules.stream()
                .filter(rule -> rule.getIsActive())
                .toList();
    }
    
    /**
     * Select destination based on rule and analysis
     */
    private Destination selectDestination(RoutingRule rule, MessageAnalysis analysis, Map<String, Object> context) {
        if (rule == null) {
            return createDefaultDestination();
        }
        
        // Create destination based on rule
        Destination destination = Destination.builder()
                .name(rule.getDestination())
                .type(determineDestinationType(rule).name())
                .endpoint(determineEndpoint(rule))
                .isActive(true)
                .timeout(30000)
                .retryCount(3)
                .build();
        
        return destination;
    }
    
    /**
     * Calculate decision confidence
     */
    private double calculateConfidence(MessageAnalysis analysis, RoutingRule rule) {
        double confidence = 0.5; // Base confidence
        
        // Increase confidence based on rule match
        if (rule != null) {
            confidence += 0.3;
        }
        
        // Increase confidence based on content clarity
        if (analysis.getWordCount() > 1) {
            confidence += 0.1;
        }
        
        // Increase confidence based on intent detection
        if (analysis.getIntent() != null && !analysis.getIntent().equals("unknown")) {
            confidence += 0.1;
        }
        
        return Math.min(confidence, 1.0);
    }
    
    // Helper methods for creating rules and destinations
    private RoutingRule createDefaultRule() {
        return RoutingRule.builder()
                .name("default-rule")
                .destination("message-store")
                .ruleType(RoutingRule.RuleType.DEFAULT)
                .action(RoutingRule.Action.ROUTE)
                .isActive(true)
                .priority(999)
                .build();
    }
    
    private RoutingRule createIntentRule(String intent, String destination) {
        return RoutingRule.builder()
                .name("intent-rule-" + intent)
                .pattern(intent)
                .destination(destination)
                .ruleType(RoutingRule.RuleType.INTENT)
                .action(RoutingRule.Action.ROUTE)
                .isActive(true)
                .priority(1)
                .build();
    }
    
    private RoutingRule createContentRule(String contentType, String destination) {
        return RoutingRule.builder()
                .name("content-rule-" + contentType)
                .pattern(contentType)
                .destination(destination)
                .ruleType(RoutingRule.RuleType.KEYWORD)
                .action(RoutingRule.Action.ROUTE)
                .isActive(true)
                .priority(2)
                .build();
    }
    
    private RoutingRule createChannelRule(String channel, String destination) {
        return RoutingRule.builder()
                .name("channel-rule-" + channel)
                .source(channel)
                .destination(destination)
                .ruleType(RoutingRule.RuleType.CONDITION)
                .action(RoutingRule.Action.ROUTE)
                .isActive(true)
                .priority(3)
                .build();
    }
    
    private RoutingRule createSentimentRule(String sentiment, String destination) {
        return RoutingRule.builder()
                .name("sentiment-rule-" + sentiment)
                .pattern(sentiment)
                .destination(destination)
                .ruleType(RoutingRule.RuleType.CONDITION)
                .action(RoutingRule.Action.ROUTE)
                .isActive(true)
                .priority(2)
                .build();
    }
    
    private Destination createDefaultDestination() {
        return Destination.builder()
                .name("default-destination")
                .endpoint("message-store")
                .type(Destination.DestinationType.HUB.name())
                .destinationType(Destination.DestinationType.HUB)
                .connectionMethod(Destination.ConnectionMethod.GRPC)
                .isActive(true)
                .timeout(30000)
                .retryCount(3)
                .build();
    }
    
    private Destination.DestinationType determineDestinationType(RoutingRule rule) {
        return Destination.DestinationType.HUB; // Default to hub
    }
    
    private String determineEndpoint(RoutingRule rule) {
        return rule.getDestination(); // Use destination as endpoint
    }
    
    // Content analysis helper methods
    private boolean containsUrls(String content) {
        return content.toLowerCase().matches(".*https?://.*");
    }
    
    private boolean containsEmails(String content) {
        return content.toLowerCase().matches(".*[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}.*");
    }
    
    private boolean containsPhoneNumbers(String content) {
        return content.matches(".*\\b\\d{10,}\\b.*");
    }
    
    private boolean containsSpecialChars(String content) {
        return !content.matches("[a-zA-Z0-9\\s]*");
    }
    
    private String analyzeSentiment(String content) {
        String lowerContent = content.toLowerCase();
        
        // Simple sentiment analysis
        if (lowerContent.contains("angry") || lowerContent.contains("frustrated") || 
            lowerContent.contains("upset") || lowerContent.contains("disappointed")) {
            return "negative";
        }
        
        if (lowerContent.contains("happy") || lowerContent.contains("good") || 
            lowerContent.contains("great") || lowerContent.contains("excellent")) {
            return "positive";
        }
        
        return "neutral";
    }
    
    private String detectIntent(String content) {
        String lowerContent = content.toLowerCase();
        
        // Simple intent detection
        if (lowerContent.contains("?") || lowerContent.contains("how") || 
            lowerContent.contains("what") || lowerContent.contains("when")) {
            return "question";
        }
        
        if (lowerContent.contains("help") || lowerContent.contains("support")) {
            return "help";
        }
        
        if (lowerContent.contains("hello") || lowerContent.contains("hi")) {
            return "greeting";
        }
        
        if (lowerContent.contains("bye") || lowerContent.contains("goodbye")) {
            return "farewell";
        }
        
        return "unknown";
    }
    
    // Data classes
    public static class RoutingDecision {
        private String messageId;
        private MessageAnalysis analysis;
        private RoutingRule appliedRule;
        private Destination destination;
        private double confidence;
        private long timestamp;
        
        // Getters and setters
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        
        public MessageAnalysis getAnalysis() { return analysis; }
        public void setAnalysis(MessageAnalysis analysis) { this.analysis = analysis; }
        
        public RoutingRule getAppliedRule() { return appliedRule; }
        public void setAppliedRule(RoutingRule appliedRule) { this.appliedRule = appliedRule; }
        
        public Destination getDestination() { return destination; }
        public void setDestination(Destination destination) { this.destination = destination; }
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
    
    public static class MessageAnalysis {
        private String content;
        private int length;
        private int wordCount;
        private boolean hasUrls;
        private boolean hasEmails;
        private boolean hasPhoneNumbers;
        private boolean hasSpecialChars;
        private String sentiment;
        private String intent;
        private String channel;
        private String userId;
        private String sessionId;
        
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
        
        public boolean getHasSpecialChars() { return hasSpecialChars; }
        public void setHasSpecialChars(boolean hasSpecialChars) { this.hasSpecialChars = hasSpecialChars; }
        
        public String getSentiment() { return sentiment; }
        public void setSentiment(String sentiment) { this.sentiment = sentiment; }
        
        public String getIntent() { return intent; }
        public void setIntent(String intent) { this.intent = intent; }
        
        public String getChannel() { return channel; }
        public void setChannel(String channel) { this.channel = channel; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }
}
