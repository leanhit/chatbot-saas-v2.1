package com.chatbot.core.message.router.service;

import com.chatbot.core.message.decision.model.ProcessingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Single Decision Point Service - Centralized decision making for message routing
 */
@Service
@Slf4j
public class SingleDecisionPointService {
    
    private final RoutingDecisionService routingDecisionService;
    private final Map<String, DecisionCache> decisionCache;
    
    public SingleDecisionPointService(RoutingDecisionService routingDecisionService) {
        this.routingDecisionService = routingDecisionService;
        this.decisionCache = new ConcurrentHashMap<>();
    }
    
    /**
     * Make single decision point for message processing
     */
    public SingleDecision makeSingleDecision(ProcessingContext context) {
        String messageId = context.getMessageId();
        
        log.debug("Making single decision point for message: {}", messageId);
        
        // Check cache first
        DecisionCache cached = decisionCache.get(messageId);
        if (cached != null && !cached.isExpired()) {
            log.debug("Using cached decision for message: {}", messageId);
            return cached.getDecision();
        }
        
        // Make new decision
        SingleDecision decision = new SingleDecision();
        decision.setMessageId(messageId);
        decision.setConversationId(context.getConversationId());
        decision.setTimestamp(System.currentTimeMillis());
        
        // Analyze context
        ContextAnalysis analysis = analyzeContext(context);
        decision.setContextAnalysis(analysis);
        
        // Make routing decision
        RoutingDecisionService.RoutingDecision routingDecision = 
            routingDecisionService.makeRoutingDecision(messageId, context.getContent(), context.getSystemData());
        decision.setRoutingDecision(routingDecision);
        
        // Make processing decision
        ProcessingDecision processingDecision = makeProcessingDecision(context, routingDecision);
        decision.setProcessingDecision(processingDecision);
        
        // Make quality decision
        QualityDecision qualityDecision = makeQualityDecision(context, routingDecision);
        decision.setQualityDecision(qualityDecision);
        
        // Calculate overall confidence
        decision.setOverallConfidence(calculateOverallConfidence(decision));
        
        // Cache the decision
        decisionCache.put(messageId, new DecisionCache(decision, System.currentTimeMillis() + 300000)); // 5 minutes
        
        log.info("Single decision point for {}: route={}, process={}, quality={} (confidence: {})", 
                 messageId, 
                 routingDecision.getDestination().getName(),
                 processingDecision.getAction(),
                 qualityDecision.getAction(),
                 decision.getOverallConfidence());
        
        return decision;
    }
    
    /**
     * Analyze processing context
     */
    private ContextAnalysis analyzeContext(ProcessingContext context) {
        ContextAnalysis analysis = new ContextAnalysis();
        
        // Basic context info
        analysis.setMessageId(context.getMessageId());
        analysis.setConversationId(context.getConversationId());
        analysis.setChannel(context.getChannel());
        analysis.setMessageType(context.getMessageType());
        
        // User context analysis
        analysis.setUserId(context.getUserId());
        analysis.setSessionId(context.getSessionId());
        analysis.setReturningUser(isReturningUser(context.getUserId()));
        analysis.setHighValueUser(isHighValueUser(context.getUserId()));
        
        // Conversation context analysis
        analysis.setConversationLength(getConversationLength(context.getConversationId()));
        analysis.setEscalated(isEscalatedConversation(context.getConversationId()));
        analysis.setActiveTakeover(hasActiveTakeover(context.getConversationId()));
        
        // Content analysis
        analysis.setContentLength(context.getContent().length());
        analysis.setWordCount(context.getContent().split("\\s+").length);
        analysis.setHasUrgentKeywords(hasUrgentKeywords(context.getContent()));
        analysis.setRequiresHumanAttention(requiresHumanAttention(context));
        
        // System context
        analysis.setSystemLoad(getCurrentSystemLoad());
        analysis.setBusinessHours(isBusinessHours());
        analysis.setAvailableAgents(getAvailableAgentCount());
        
        return analysis;
    }
    
    /**
     * Make processing decision
     */
    private ProcessingDecision makeProcessingDecision(ProcessingContext context, RoutingDecisionService.RoutingDecision routingDecision) {
        ProcessingDecision decision = new ProcessingDecision();
        
        // Determine processing action
        String action = determineProcessingAction(context, routingDecision);
        decision.setAction(action);
        
        // Set processing priority
        ProcessingPriority priority = determineProcessingPriority(context, routingDecision);
        decision.setPriority(priority);
        
        // Set processing requirements
        decision.setRequiresValidation(requiresValidation(context));
        decision.setRequiresTransformation(requiresTransformation(context));
        decision.setRequiresEnrichment(requiresEnrichment(context));
        decision.setRequiresArchival(requiresArchival(context));
        
        // Set processing constraints
        decision.setMaxProcessingTime(determineMaxProcessingTime(context, priority));
        decision.setMaxRetries(determineMaxRetries(context, priority));
        
        return decision;
    }
    
    /**
     * Make quality decision
     */
    private QualityDecision makeQualityDecision(ProcessingContext context, RoutingDecisionService.RoutingDecision routingDecision) {
        QualityDecision decision = new QualityDecision();
        
        // Determine quality action
        String action = determineQualityAction(context, routingDecision);
        decision.setAction(action);
        
        // Set quality checks
        decision.setRequiresProfanityCheck(requiresProfanityCheck(context));
        decision.setRequiresSpamCheck(requiresSpamCheck(context));
        decision.setRequiresSecurityCheck(requiresSecurityCheck(context));
        decision.setRequiresContentFilter(requiresContentFilter(context));
        
        // Set quality thresholds
        decision.setMinConfidenceThreshold(determineMinConfidenceThreshold(context));
        decision.setMaxContentLength(determineMaxContentLength(context));
        decision.setAllowedContentTypes(determineAllowedContentTypes(context));
        
        return decision;
    }
    
    /**
     * Calculate overall confidence
     */
    private double calculateOverallConfidence(SingleDecision decision) {
        double routingConfidence = decision.getRoutingDecision().getConfidence();
        double contextScore = calculateContextScore(decision.getContextAnalysis());
        double processingScore = calculateProcessingScore(decision.getProcessingDecision());
        double qualityScore = calculateQualityScore(decision.getQualityDecision());
        
        // Weighted average
        double overallConfidence = (routingConfidence * 0.4) + 
                                 (contextScore * 0.2) + 
                                 (processingScore * 0.2) + 
                                 (qualityScore * 0.2);
        
        return Math.min(overallConfidence, 1.0);
    }
    
    // Helper methods for context analysis
    private boolean isReturningUser(String userId) {
        // Implementation would check user history
        return false;
    }
    
    private boolean isHighValueUser(String userId) {
        // Implementation would check user value
        return false;
    }
    
    private int getConversationLength(String conversationId) {
        // Implementation would get conversation message count
        return 1;
    }
    
    private boolean isEscalatedConversation(String conversationId) {
        // Implementation would check escalation status
        return false;
    }
    
    private boolean hasActiveTakeover(String conversationId) {
        // Implementation would check takeover status
        return false;
    }
    
    private boolean hasUrgentKeywords(String content) {
        String lowerContent = content.toLowerCase();
        return lowerContent.contains("urgent") || 
               lowerContent.contains("emergency") || 
               lowerContent.contains("asap");
    }
    
    private boolean requiresHumanAttention(ProcessingContext context) {
        return hasUrgentKeywords(context.getContent()) || 
               isEscalatedConversation(context.getConversationId());
    }
    
    private double getCurrentSystemLoad() {
        // Implementation would get actual system load
        return 0.5;
    }
    
    private boolean isBusinessHours() {
        // Implementation would check business hours
        return true;
    }
    
    private int getAvailableAgentCount() {
        // Implementation would get available agent count
        return 5;
    }
    
    // Helper methods for processing decisions
    private String determineProcessingAction(ProcessingContext context, RoutingDecisionService.RoutingDecision routingDecision) {
        if (requiresHumanAttention(context)) {
            return "ROUTE_TO_AGENT";
        }
        
        if (routingDecision.getDestination().getName().equals("botpress")) {
            return "PROCESS_WITH_BOT";
        }
        
        return "STANDARD_PROCESSING";
    }
    
    private ProcessingPriority determineProcessingPriority(ProcessingContext context, RoutingDecisionService.RoutingDecision routingDecision) {
        if (hasUrgentKeywords(context.getContent())) {
            return ProcessingPriority.HIGH;
        }
        
        if (isHighValueUser(context.getUserId())) {
            return ProcessingPriority.MEDIUM;
        }
        
        return ProcessingPriority.LOW;
    }
    
    private boolean requiresValidation(ProcessingContext context) {
        return true; // Always validate
    }
    
    private boolean requiresTransformation(ProcessingContext context) {
        return !"api".equals(context.getChannel()); // Transform non-API messages
    }
    
    private boolean requiresEnrichment(ProcessingContext context) {
        return isReturningUser(context.getUserId()); // Enrich for returning users
    }
    
    private boolean requiresArchival(ProcessingContext context) {
        return true; // Always archive
    }
    
    private long determineMaxProcessingTime(ProcessingContext context, ProcessingPriority priority) {
        return switch (priority) {
            case HIGH -> 5000;   // 5 seconds
            case MEDIUM -> 10000; // 10 seconds
            case LOW -> 30000;    // 30 seconds
        };
    }
    
    private int determineMaxRetries(ProcessingContext context, ProcessingPriority priority) {
        return switch (priority) {
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
    }
    
    // Helper methods for quality decisions
    private String determineQualityAction(ProcessingContext context, RoutingDecisionService.RoutingDecision routingDecision) {
        if (hasUrgentKeywords(context.getContent())) {
            return "EXPEDITED_PROCESSING";
        }
        
        return "STANDARD_QUALITY_CHECK";
    }
    
    private boolean requiresProfanityCheck(ProcessingContext context) {
        return true; // Always check profanity
    }
    
    private boolean requiresSpamCheck(ProcessingContext context) {
        return !isReturningUser(context.getUserId()); // Check spam for new users
    }
    
    private boolean requiresSecurityCheck(ProcessingContext context) {
        return true; // Always security check
    }
    
    private boolean requiresContentFilter(ProcessingContext context) {
        return "facebook".equals(context.getChannel()); // Filter social media content
    }
    
    private double determineMinConfidenceThreshold(ProcessingContext context) {
        if (hasUrgentKeywords(context.getContent())) {
            return 0.3; // Lower threshold for urgent messages
        }
        
        return 0.7; // Standard threshold
    }
    
    private int determineMaxContentLength(ProcessingContext context) {
        return switch (context.getChannel()) {
            case "facebook" -> 800;
            case "twitter" -> 280;
            default -> 1000;
        };
    }
    
    private java.util.List<String> determineAllowedContentTypes(ProcessingContext context) {
        return java.util.List.of("text", "image", "file");
    }
    
    // Helper methods for scoring
    private double calculateContextScore(ContextAnalysis analysis) {
        double score = 0.5;
        
        if (analysis.isReturningUser()) score += 0.1;
        if (analysis.isHighValueUser()) score += 0.1;
        if (analysis.isBusinessHours()) score += 0.1;
        if (analysis.getAvailableAgents() > 0) score += 0.1;
        
        return Math.min(score, 1.0);
    }
    
    private double calculateProcessingScore(ProcessingDecision decision) {
        double score = 0.5;
        
        if (decision.isRequiresValidation()) score += 0.1;
        if (decision.isRequiresTransformation()) score += 0.1;
        if (decision.isRequiresEnrichment()) score += 0.1;
        
        return Math.min(score, 1.0);
    }
    
    private double calculateQualityScore(QualityDecision decision) {
        double score = 0.5;
        
        if (decision.isRequiresProfanityCheck()) score += 0.1;
        if (decision.isRequiresSpamCheck()) score += 0.1;
        if (decision.isRequiresSecurityCheck()) score += 0.1;
        
        return Math.min(score, 1.0);
    }
    
    // Data classes
    public static class SingleDecision {
        private String messageId;
        private String conversationId;
        private long timestamp;
        private ContextAnalysis contextAnalysis;
        private RoutingDecisionService.RoutingDecision routingDecision;
        private ProcessingDecision processingDecision;
        private QualityDecision qualityDecision;
        private double overallConfidence;
        
        // Getters and setters
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        
        public String getConversationId() { return conversationId; }
        public void setConversationId(String conversationId) { this.conversationId = conversationId; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public ContextAnalysis getContextAnalysis() { return contextAnalysis; }
        public void setContextAnalysis(ContextAnalysis contextAnalysis) { this.contextAnalysis = contextAnalysis; }
        
        public RoutingDecisionService.RoutingDecision getRoutingDecision() { return routingDecision; }
        public void setRoutingDecision(RoutingDecisionService.RoutingDecision routingDecision) { this.routingDecision = routingDecision; }
        
        public ProcessingDecision getProcessingDecision() { return processingDecision; }
        public void setProcessingDecision(ProcessingDecision processingDecision) { this.processingDecision = processingDecision; }
        
        public QualityDecision getQualityDecision() { return qualityDecision; }
        public void setQualityDecision(QualityDecision qualityDecision) { this.qualityDecision = qualityDecision; }
        
        public double getOverallConfidence() { return overallConfidence; }
        public void setOverallConfidence(double overallConfidence) { this.overallConfidence = overallConfidence; }
    }
    
    public static class DecisionCache {
        private final SingleDecision decision;
        private final long expiryTime;
        
        public DecisionCache(SingleDecision decision, long expiryTime) {
            this.decision = decision;
            this.expiryTime = expiryTime;
        }
        
        public SingleDecision getDecision() { return decision; }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }
    
    public static class ContextAnalysis {
        private String messageId;
        private String conversationId;
        private String channel;
        private String messageType;
        private String userId;
        private String sessionId;
        private boolean isReturningUser;
        private boolean isHighValueUser;
        private int conversationLength;
        private boolean isEscalated;
        private boolean hasActiveTakeover;
        private int contentLength;
        private int wordCount;
        private boolean hasUrgentKeywords;
        private boolean requiresHumanAttention;
        private double systemLoad;
        private boolean isBusinessHours;
        private int availableAgents;
        
        // Getters and setters (abbreviated for brevity)
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        
        public String getConversationId() { return conversationId; }
        public void setConversationId(String conversationId) { this.conversationId = conversationId; }
        
        public String getChannel() { return channel; }
        public void setChannel(String channel) { this.channel = channel; }
        
        public String getMessageType() { return messageType; }
        public void setMessageType(String messageType) { this.messageType = messageType; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        
        public boolean isReturningUser() { return isReturningUser; }
        public void setReturningUser(boolean returningUser) { isReturningUser = returningUser; }
        
        public boolean isHighValueUser() { return isHighValueUser; }
        public void setHighValueUser(boolean highValueUser) { isHighValueUser = highValueUser; }
        
        public int getConversationLength() { return conversationLength; }
        public void setConversationLength(int conversationLength) { this.conversationLength = conversationLength; }
        
        public boolean isEscalated() { return isEscalated; }
        public void setEscalated(boolean escalated) { isEscalated = escalated; }
        
        public boolean hasActiveTakeover() { return hasActiveTakeover; }
        public void setActiveTakeover(boolean activeTakeover) { hasActiveTakeover = activeTakeover; }
        
        public int getContentLength() { return contentLength; }
        public void setContentLength(int contentLength) { this.contentLength = contentLength; }
        
        public int getWordCount() { return wordCount; }
        public void setWordCount(int wordCount) { this.wordCount = wordCount; }
        
        public boolean getHasUrgentKeywords() { return hasUrgentKeywords; }
        public void setHasUrgentKeywords(boolean hasUrgentKeywords) { this.hasUrgentKeywords = hasUrgentKeywords; }
        
        public boolean getRequiresHumanAttention() { return requiresHumanAttention; }
        public void setRequiresHumanAttention(boolean requiresHumanAttention) { this.requiresHumanAttention = requiresHumanAttention; }
        
        public double getSystemLoad() { return systemLoad; }
        public void setSystemLoad(double systemLoad) { this.systemLoad = systemLoad; }
        
        public boolean isBusinessHours() { return isBusinessHours; }
        public void setBusinessHours(boolean businessHours) { isBusinessHours = businessHours; }
        
        public int getAvailableAgents() { return availableAgents; }
        public void setAvailableAgents(int availableAgents) { this.availableAgents = availableAgents; }
    }
    
    public static class ProcessingDecision {
        private String action;
        private ProcessingPriority priority;
        private boolean requiresValidation;
        private boolean requiresTransformation;
        private boolean requiresEnrichment;
        private boolean requiresArchival;
        private long maxProcessingTime;
        private int maxRetries;
        
        // Getters and setters
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        
        public ProcessingPriority getPriority() { return priority; }
        public void setPriority(ProcessingPriority priority) { this.priority = priority; }
        
        public boolean isRequiresValidation() { return requiresValidation; }
        public void setRequiresValidation(boolean requiresValidation) { this.requiresValidation = requiresValidation; }
        
        public boolean isRequiresTransformation() { return requiresTransformation; }
        public void setRequiresTransformation(boolean requiresTransformation) { this.requiresTransformation = requiresTransformation; }
        
        public boolean isRequiresEnrichment() { return requiresEnrichment; }
        public void setRequiresEnrichment(boolean requiresEnrichment) { this.requiresEnrichment = requiresEnrichment; }
        
        public boolean isRequiresArchival() { return requiresArchival; }
        public void setRequiresArchival(boolean requiresArchival) { this.requiresArchival = requiresArchival; }
        
        public long getMaxProcessingTime() { return maxProcessingTime; }
        public void setMaxProcessingTime(long maxProcessingTime) { this.maxProcessingTime = maxProcessingTime; }
        
        public int getMaxRetries() { return maxRetries; }
        public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    }
    
    public static class QualityDecision {
        private String action;
        private boolean requiresProfanityCheck;
        private boolean requiresSpamCheck;
        private boolean requiresSecurityCheck;
        private boolean requiresContentFilter;
        private double minConfidenceThreshold;
        private int maxContentLength;
        private java.util.List<String> allowedContentTypes;
        
        // Getters and setters
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        
        public boolean isRequiresProfanityCheck() { return requiresProfanityCheck; }
        public void setRequiresProfanityCheck(boolean requiresProfanityCheck) { this.requiresProfanityCheck = requiresProfanityCheck; }
        
        public boolean isRequiresSpamCheck() { return requiresSpamCheck; }
        public void setRequiresSpamCheck(boolean requiresSpamCheck) { this.requiresSpamCheck = requiresSpamCheck; }
        
        public boolean isRequiresSecurityCheck() { return requiresSecurityCheck; }
        public void setRequiresSecurityCheck(boolean requiresSecurityCheck) { this.requiresSecurityCheck = requiresSecurityCheck; }
        
        public boolean isRequiresContentFilter() { return requiresContentFilter; }
        public void setRequiresContentFilter(boolean requiresContentFilter) { this.requiresContentFilter = requiresContentFilter; }
        
        public double getMinConfidenceThreshold() { return minConfidenceThreshold; }
        public void setMinConfidenceThreshold(double minConfidenceThreshold) { this.minConfidenceThreshold = minConfidenceThreshold; }
        
        public int getMaxContentLength() { return maxContentLength; }
        public void setMaxContentLength(int maxContentLength) { this.maxContentLength = maxContentLength; }
        
        public java.util.List<String> getAllowedContentTypes() { return allowedContentTypes; }
        public void setAllowedContentTypes(java.util.List<String> allowedContentTypes) { this.allowedContentTypes = allowedContentTypes; }
    }
    
    public enum ProcessingPriority {
        LOW, MEDIUM, HIGH
    }
}
