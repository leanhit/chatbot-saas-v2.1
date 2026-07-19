package com.chatbot.core.tenant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for tenant statistics displayed on the Overview page.
 * Aggregates data from members, bots, knowledge base storage, and message history.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantStatsResponse {

    /** Number of active workspace members */
    private long activeUsers;

    /** Number of active Penny bots configured for this tenant */
    private long totalBots;

    /** Total knowledge base storage used (human-readable, e.g. "4.2 MB") */
    private String storageUsed;

    /** Total number of messages processed (used as a proxy for API calls) */
    private long totalMessages;
}
