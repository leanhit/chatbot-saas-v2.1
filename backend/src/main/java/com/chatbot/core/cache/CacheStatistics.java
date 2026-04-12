package com.chatbot.core.cache;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Cache Statistics Data Transfer Object for Chatbot SaaS v2.1
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CacheStatistics {
    private int keyCount;
    private long usedMemory;
    private long maxMemory;
    private double hitRate;
    private long totalRequests;
    private long cacheHits;
    private long cacheMisses;
    private long evictions;
    private String lastUpdated;
}
