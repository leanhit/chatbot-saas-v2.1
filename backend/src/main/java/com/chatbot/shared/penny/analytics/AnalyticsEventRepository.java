package com.chatbot.shared.penny.analytics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for AnalyticsEvent entities
 */
@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, UUID> {

    /**
     * Find events by bot ID within time range
     */
    List<AnalyticsEvent> findByBotIdAndTimestampBetween(
        UUID botId, 
        Instant startTime, 
        Instant endTime
    );

    /**
     * Find events by event type
     */
    List<AnalyticsEvent> findByEventType(String eventType);

    /**
     * Count events by bot ID
     */
    long countByBotId(UUID botId);

    /**
     * Find recent events for a bot
     */
    List<AnalyticsEvent> findTop100ByBotIdOrderByTimestampDesc(UUID botId);

    /**
     * Get message count by bot in time range
     */
    @Query("SELECT COUNT(e) FROM AnalyticsEvent e WHERE e.botId = :botId AND e.eventType = 'message_processed' AND e.timestamp BETWEEN :startTime AND :endTime")
    long countMessagesByBotInTimeRange(
        @Param("botId") UUID botId,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );

    /**
     * Get error count by bot in time range
     */
    @Query("SELECT COUNT(e) FROM AnalyticsEvent e WHERE e.botId = :botId AND e.eventType = 'error' AND e.timestamp BETWEEN :startTime AND :endTime")
    long countErrorsByBotInTimeRange(
        @Param("botId") UUID botId,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );

    /**
     * Delete events older than specified timestamp
     */
    void deleteByTimestampBefore(Instant timestamp);
}
