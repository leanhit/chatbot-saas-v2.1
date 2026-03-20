package com.chatbot.core.message.store.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConversationStatisticsDTO {
    
    private Long totalConversations;
    private Long activeTakeovers;
    private Long pendingMessages;
    private Long todayMessages;
    
    // Growth metrics
    private Double growthRate;
    private Long activeUsers;
    private Double userGrowth;
    
    // Bot metrics
    private Long botResponses;
    private Double responseRate;
    private Long activeConnections;
    
    // Additional metrics for dashboard
    private Long averageResponseTime;
    private Long totalMessages;
    private Double satisfactionRate;
}
