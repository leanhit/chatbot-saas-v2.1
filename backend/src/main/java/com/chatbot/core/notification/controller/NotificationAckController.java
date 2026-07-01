package com.chatbot.core.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

/**
 * REST controller for notification acknowledgment
 * Implements Phase 1.2: Notification acknowledgment
 *
 * Uses Redis to track acknowledged notification IDs (TTL: 24h).
 * Notifications are ephemeral (WebSocket-based), so ack state lives in Redis.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationAckController {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String ACK_PREFIX = "notification:ack:";
    private static final Duration ACK_TTL = Duration.ofHours(24);

    /**
     * Acknowledge a specific notification by ID.
     * Stores ack state in Redis with 24h TTL.
     *
     * POST /api/notifications/{notificationId}/ack
     *
     * @param notificationId  Client-side generated notification ID
     * @return 200 OK with acknowledgment confirmation
     */
    @PostMapping("/{notificationId}/ack")
    public ResponseEntity<Map<String, Object>> acknowledgeNotification(
            @PathVariable String notificationId) {
        String redisKey = ACK_PREFIX + notificationId;
        redisTemplate.opsForValue().set(redisKey, "acked", ACK_TTL);

        log.debug("Acknowledged notification: {}", notificationId);
        return ResponseEntity.ok(Map.of(
            "status", "acknowledged",
            "notificationId", notificationId
        ));
    }

    /**
     * Acknowledge multiple notifications at once.
     * POST /api/notifications/ack-all
     *
     * @param body JSON body: { "notificationIds": ["id1", "id2", ...] }
     * @return 200 OK with count of acknowledged notifications
     */
    @PostMapping("/ack-all")
    public ResponseEntity<Map<String, Object>> acknowledgeAll(
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        java.util.List<String> notificationIds = (java.util.List<String>) body.get("notificationIds");

        if (notificationIds == null || notificationIds.isEmpty()) {
            return ResponseEntity.ok(Map.of("status", "ok", "acknowledged", 0));
        }

        int count = 0;
        for (String id : notificationIds) {
            String redisKey = ACK_PREFIX + id;
            redisTemplate.opsForValue().set(redisKey, "acked", ACK_TTL);
            count++;
        }

        log.debug("Acknowledged {} notifications in bulk", count);
        return ResponseEntity.ok(Map.of(
            "status", "acknowledged",
            "acknowledged", count
        ));
    }

    /**
     * Check if a specific notification has been acknowledged.
     * GET /api/notifications/{notificationId}/ack
     *
     * @param notificationId The notification ID to check
     * @return 200 OK with acknowledged: true/false
     */
    @GetMapping("/{notificationId}/ack")
    public ResponseEntity<Map<String, Object>> checkAcknowledgement(
            @PathVariable String notificationId) {
        String redisKey = ACK_PREFIX + notificationId;
        Boolean exists = redisTemplate.hasKey(redisKey);
        boolean acknowledged = Boolean.TRUE.equals(exists);

        return ResponseEntity.ok(Map.of(
            "notificationId", notificationId,
            "acknowledged", acknowledged
        ));
    }
}
