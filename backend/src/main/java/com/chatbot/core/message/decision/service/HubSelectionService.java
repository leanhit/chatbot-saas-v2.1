package com.chatbot.core.message.decision.service;

import com.chatbot.core.message.decision.model.ProcessingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Hub Selection Service - Selects appropriate hub for message processing
 */
@Service
@Slf4j
public class HubSelectionService {
    
    private final Map<String, HubInfo> availableHubs;
    
    public HubSelectionService() {
        this.availableHubs = initializeHubs();
    }
    
    /**
     * Select hub for message processing
     */
    public HubSelection selectHub(ProcessingContext context) {
        log.debug("Selecting hub for message: {}", context.getMessageId());
        
        HubSelection selection = new HubSelection();
        selection.setMessageId(context.getMessageId());
        selection.setTimestamp(System.currentTimeMillis());
        
        // Analyze context for hub selection
        HubAnalysis analysis = analyzeContextForHub(context);
        selection.setAnalysis(analysis);
        
        // Get candidate hubs
        List<HubInfo> candidates = getCandidateHubs(analysis);
        selection.setCandidates(candidates);
        
        // Select best hub
        HubInfo selectedHub = selectBestHub(candidates, analysis, context);
        selection.setSelectedHub(selectedHub);
        
        // Calculate selection confidence
        selection.setConfidence(calculateSelectionConfidence(selectedHub, analysis, context));
        
        // Set fallback hubs
        selection.setFallbackHubs(getFallbackHubs(candidates, selectedHub));
        
        log.info("Hub selection for {}: primary={}, fallbacks={}, confidence={}", 
                 context.getMessageId(), 
                 selectedHub.getName(),
                 selection.getFallbackHubs().stream().map(HubInfo::getName).toList(),
                 selection.getConfidence());
        
        return selection;
    }
    
    /**
     * Analyze context for hub selection
     */
    private HubAnalysis analyzeContextForHub(ProcessingContext context) {
        HubAnalysis analysis = new HubAnalysis();
        
        // Message characteristics
        analysis.setMessageType(context.getMessageType());
        analysis.setContentLength(context.getContent().length());
        analysis.setHasAttachments(hasAttachments(context));
        analysis.setRequiresRealTime(requiresRealTime(context));
        
        // User characteristics
        analysis.setUserId(context.getUserId());
        analysis.setIsPremiumUser(isPremiumUser(context.getUserId()));
        analysis.setIsReturningUser(isReturningUser(context.getUserId()));
        
        // Conversation characteristics
        analysis.setConversationId(context.getConversationId());
        analysis.setEscalated(isEscalatedConversation(context.getConversationId()));
        analysis.setActiveTakeover(hasActiveTakeover(context.getConversationId()));
        
        // System characteristics
        analysis.setSystemLoad(getCurrentSystemLoad());
        analysis.setAvailableHubs(getAvailableHubCount());
        analysis.setBusinessHours(isBusinessHours());
        
        return analysis;
    }
    
    /**
     * Get candidate hubs based on analysis
     */
    private List<HubInfo> getCandidateHubs(HubAnalysis analysis) {
        List<HubInfo> candidates = new ArrayList<>();
        
        // Always include message hub as default
        candidates.add(availableHubs.get("message"));
        
        // Add specialized hubs based on analysis
        if (analysis.getHasActiveTakeover()) {
            candidates.add(availableHubs.get("agent"));
        }
        
        if (analysis.getRequiresRealTime()) {
            candidates.add(availableHubs.get("realtime"));
        }
        
        if (analysis.getIsEscalated()) {
            candidates.add(availableHubs.get("escalation"));
        }
        
        if (analysis.getIsPremiumUser()) {
            candidates.add(availableHubs.get("premium"));
        }
        
        // Filter by availability and load
        return candidates.stream()
                .filter(hub -> hub.isAvailable() && hub.getCurrentLoad() < hub.getMaxCapacity())
                .toList();
    }
    
    /**
     * Select best hub from candidates
     */
    private HubInfo selectBestHub(List<HubInfo> candidates, HubAnalysis analysis, ProcessingContext context) {
        if (candidates.isEmpty()) {
            return availableHubs.get("message"); // Fallback to message hub
        }
        
        // Score each candidate
        Map<HubInfo, Double> scores = new HashMap<>();
        for (HubInfo hub : candidates) {
            double score = calculateHubScore(hub, analysis, context);
            scores.put(hub, score);
        }
        
        // Select hub with highest score
        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(candidates.get(0));
    }
    
    /**
     * Calculate hub score
     */
    private double calculateHubScore(HubInfo hub, HubAnalysis analysis, ProcessingContext context) {
        double score = 0.0;
        
        // Base score for hub type
        score += getHubTypeScore(hub.getType());
        
        // Load factor
        double loadFactor = 1.0 - (hub.getCurrentLoad() / hub.getMaxCapacity());
        score += loadFactor * 0.3;
        
        // Specialization match
        score += getSpecializationScore(hub, analysis) * 0.4;
        
        // Performance factor
        score += getPerformanceScore(hub) * 0.2;
        
        // Availability factor
        score += (hub.isAvailable() ? 0.1 : 0.0);
        
        return Math.min(score, 1.0);
    }
    
    /**
     * Get hub type score
     */
    private double getHubTypeScore(String hubType) {
        return switch (hubType) {
            case "message" -> 0.5;  // Standard hub
            case "agent" -> 0.8;   // High priority for human interaction
            case "realtime" -> 0.7; // Good for real-time needs
            case "escalation" -> 0.9; // High priority for escalations
            case "premium" -> 0.6;  // Moderate for premium users
            default -> 0.3;
        };
    }
    
    /**
     * Get specialization score
     */
    private double getSpecializationScore(HubInfo hub, HubAnalysis analysis) {
        double score = 0.0;
        
        // Check hub specializations
        for (String specialization : hub.getSpecializations()) {
            switch (specialization) {
                case "takeover":
                    if (analysis.getHasActiveTakeover()) score += 0.5;
                    break;
                case "realtime":
                    if (analysis.getRequiresRealTime()) score += 0.5;
                    break;
                case "escalation":
                    if (analysis.getIsEscalated()) score += 0.5;
                    break;
                case "premium":
                    if (analysis.getIsPremiumUser()) score += 0.5;
                    break;
                case "attachments":
                    if (analysis.getHasAttachments()) score += 0.5;
                    break;
            }
        }
        
        return Math.min(score, 1.0);
    }
    
    /**
     * Get performance score
     */
    private double getPerformanceScore(HubInfo hub) {
        // Calculate based on response time and success rate
        double responseTimeScore = Math.max(0, 1.0 - (hub.getAverageResponseTime() / 1000.0)); // 1s is baseline
        double successRateScore = hub.getSuccessRate() / 100.0;
        
        return (responseTimeScore + successRateScore) / 2.0;
    }
    
    /**
     * Calculate selection confidence
     */
    private double calculateSelectionConfidence(HubInfo selectedHub, HubAnalysis analysis, ProcessingContext context) {
        double confidence = 0.5; // Base confidence
        
        // Increase confidence based on hub availability
        if (selectedHub.isAvailable()) {
            confidence += 0.2;
        }
        
        // Increase confidence based on load
        double loadRatio = selectedHub.getCurrentLoad() / selectedHub.getMaxCapacity();
        confidence += (1.0 - loadRatio) * 0.2;
        
        // Increase confidence based on specialization match
        confidence += getSpecializationScore(selectedHub, analysis) * 0.1;
        
        return Math.min(confidence, 1.0);
    }
    
    /**
     * Get fallback hubs
     */
    private List<HubInfo> getFallbackHubs(List<HubInfo> candidates, HubInfo selectedHub) {
        return candidates.stream()
                .filter(hub -> !hub.equals(selectedHub))
                .sorted((h1, h2) -> Double.compare(
                    calculateHubScore(h2, new HubAnalysis(), new ProcessingContext()),
                    calculateHubScore(h1, new HubAnalysis(), new ProcessingContext())
                ))
                .limit(2) // Top 2 fallbacks
                .toList();
    }
    
    // Helper methods
    private Map<String, HubInfo> initializeHubs() {
        Map<String, HubInfo> hubs = new HashMap<>();
        
        // Message hub
        hubs.put("message", HubInfo.builder()
                .name("message")
                .type("message")
                .endpoint("message-store")
                .available(true)
                .currentLoad(0.3)
                .maxCapacity(1.0)
                .averageResponseTime(100)
                .successRate(95.0)
                .specializations(List.of("text", "basic"))
                .build());
        
        // Agent hub
        hubs.put("agent", HubInfo.builder()
                .name("agent")
                .type("agent")
                .endpoint("agent-service")
                .available(true)
                .currentLoad(0.2)
                .maxCapacity(0.8)
                .averageResponseTime(500)
                .successRate(90.0)
                .specializations(List.of("takeover", "human"))
                .build());
        
        // Realtime hub
        hubs.put("realtime", HubInfo.builder()
                .name("realtime")
                .type("realtime")
                .endpoint("realtime-service")
                .available(true)
                .currentLoad(0.4)
                .maxCapacity(0.9)
                .averageResponseTime(50)
                .successRate(98.0)
                .specializations(List.of("realtime", "websocket"))
                .build());
        
        // Escalation hub
        hubs.put("escalation", HubInfo.builder()
                .name("escalation")
                .type("escalation")
                .endpoint("escalation-service")
                .available(true)
                .currentLoad(0.1)
                .maxCapacity(0.5)
                .averageResponseTime(200)
                .successRate(92.0)
                .specializations(List.of("escalation", "urgent"))
                .build());
        
        // Premium hub
        hubs.put("premium", HubInfo.builder()
                .name("premium")
                .type("premium")
                .endpoint("premium-service")
                .available(true)
                .currentLoad(0.2)
                .maxCapacity(0.7)
                .averageResponseTime(150)
                .successRate(96.0)
                .specializations(List.of("premium", "priority"))
                .build());
        
        return hubs;
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
    
    private int getAvailableHubCount() {
        return (int) availableHubs.values().stream()
                .filter(HubInfo::isAvailable)
                .count();
    }
    
    private boolean isBusinessHours() {
        // Implementation would check business hours
        return true;
    }
    
    // Data classes
    public static class HubSelection {
        private String messageId;
        private long timestamp;
        private HubAnalysis analysis;
        private List<HubInfo> candidates;
        private HubInfo selectedHub;
        private List<HubInfo> fallbackHubs;
        private double confidence;
        
        // Getters and setters
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public HubAnalysis getAnalysis() { return analysis; }
        public void setAnalysis(HubAnalysis analysis) { this.analysis = analysis; }
        
        public List<HubInfo> getCandidates() { return candidates; }
        public void setCandidates(List<HubInfo> candidates) { this.candidates = candidates; }
        
        public HubInfo getSelectedHub() { return selectedHub; }
        public void setSelectedHub(HubInfo selectedHub) { this.selectedHub = selectedHub; }
        
        public List<HubInfo> getFallbackHubs() { return fallbackHubs; }
        public void setFallbackHubs(List<HubInfo> fallbackHubs) { this.fallbackHubs = fallbackHubs; }
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
    }
    
    public static class HubAnalysis {
        private String messageType;
        private int contentLength;
        private boolean hasAttachments;
        private boolean requiresRealTime;
        private String userId;
        private boolean isPremiumUser;
        private boolean isReturningUser;
        private String conversationId;
        private boolean isEscalated;
        private boolean hasActiveTakeover;
        private double systemLoad;
        private int availableHubs;
        private boolean isBusinessHours;
        
        // Getters and setters
        public String getMessageType() { return messageType; }
        public void setMessageType(String messageType) { this.messageType = messageType; }
        
        public int getContentLength() { return contentLength; }
        public void setContentLength(int contentLength) { this.contentLength = contentLength; }
        
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
        
        public double getSystemLoad() { return systemLoad; }
        public void setSystemLoad(double systemLoad) { this.systemLoad = systemLoad; }
        
        public int getAvailableHubs() { return availableHubs; }
        public void setAvailableHubs(int availableHubs) { this.availableHubs = availableHubs; }
        
        public boolean isBusinessHours() { return isBusinessHours; }
        public void setBusinessHours(boolean businessHours) { this.isBusinessHours = businessHours; }
    }
    
    public static class HubInfo {
        private String name;
        private String type;
        private String endpoint;
        private boolean available;
        private double currentLoad;
        private double maxCapacity;
        private long averageResponseTime;
        private double successRate;
        private List<String> specializations;
        
        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private final HubInfo hub = new HubInfo();
            
            public Builder name(String name) {
                hub.name = name;
                return this;
            }
            
            public Builder type(String type) {
                hub.type = type;
                return this;
            }
            
            public Builder endpoint(String endpoint) {
                hub.endpoint = endpoint;
                return this;
            }
            
            public Builder available(boolean available) {
                hub.available = available;
                return this;
            }
            
            public Builder currentLoad(double currentLoad) {
                hub.currentLoad = currentLoad;
                return this;
            }
            
            public Builder maxCapacity(double maxCapacity) {
                hub.maxCapacity = maxCapacity;
                return this;
            }
            
            public Builder averageResponseTime(long averageResponseTime) {
                hub.averageResponseTime = averageResponseTime;
                return this;
            }
            
            public Builder successRate(double successRate) {
                hub.successRate = successRate;
                return this;
            }
            
            public Builder specializations(List<String> specializations) {
                hub.specializations = specializations;
                return this;
            }
            
            public HubInfo build() {
                return hub;
            }
        }
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
        
        public double getCurrentLoad() { return currentLoad; }
        public void setCurrentLoad(double currentLoad) { this.currentLoad = currentLoad; }
        
        public double getMaxCapacity() { return maxCapacity; }
        public void setMaxCapacity(double maxCapacity) { this.maxCapacity = maxCapacity; }
        
        public long getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(long averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
        
        public List<String> getSpecializations() { return specializations; }
        public void setSpecializations(List<String> specializations) { this.specializations = specializations; }
    }
}
