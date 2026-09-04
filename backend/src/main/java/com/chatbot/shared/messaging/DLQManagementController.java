package com.chatbot.shared.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import org.springframework.http.HttpStatus;
import java.util.Optional;

/**
 * REST Controller for Dead Letter Queue management
 * Provides endpoints for monitoring, inspecting, and replaying failed messages
 */
@RestController
@RequestMapping("/api/admin/dlq")
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class DLQManagementController {

    private final Optional<DLQManagementService> dlqManagementService;

    public DLQManagementController(Optional<DLQManagementService> dlqManagementService) {
        this.dlqManagementService = dlqManagementService;
    }

    /**
     * Get statistics for all Dead Letter Queues
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getDLQStatistics() {
        log.info("GET /api/admin/dlq/statistics - Getting DLQ statistics");
        if (dlqManagementService.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "DLQ Service unavailable"));
        }
        Map<String, Object> stats = dlqManagementService.get().getDLQStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Inspect messages in a specific DLQ
     */
    @GetMapping("/inspect/{queueName}")
    public ResponseEntity<Map<String, Object>> inspectDLQ(
            @PathVariable String queueName,
            @RequestParam(defaultValue = "10") int maxMessages) {
        log.info("GET /api/admin/dlq/inspect/{} - Inspecting DLQ with max messages: {}", queueName, maxMessages);
        
        // Validate queue name ends with .dlq
        if (!queueName.endsWith(".dlq")) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Queue name must end with .dlq",
                "queueName", queueName
            ));
        }
        
        if (dlqManagementService.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "DLQ Service unavailable"));
        }
        Map<String, Object> result = dlqManagementService.get().inspectDLQ(queueName, maxMessages);
        return ResponseEntity.ok(result);
    }

    /**
     * Replay a specific message from DLQ to the original queue
     */
    @PostMapping("/replay/{dlqName}/{originalQueue}/{messageId}")
    public ResponseEntity<Map<String, Object>> replayMessage(
            @PathVariable String dlqName,
            @PathVariable String originalQueue,
            @PathVariable String messageId) {
        log.info("POST /api/admin/dlq/replay/{}/{} - Replaying message: {}", dlqName, originalQueue, messageId);
        
        if (dlqManagementService.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "DLQ Service unavailable"));
        }
        boolean success = dlqManagementService.get().replayMessage(dlqName, originalQueue, messageId);
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Message replayed successfully",
                "messageId", messageId,
                "from", dlqName,
                "to", originalQueue
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "failed",
                "message", "Failed to replay message",
                "messageId", messageId
            ));
        }
    }

    /**
     * Replay all messages from a DLQ to the original queue
     */
    @PostMapping("/replay-all/{dlqName}/{originalQueue}")
    public ResponseEntity<Map<String, Object>> replayAllMessages(
            @PathVariable String dlqName,
            @PathVariable String originalQueue) {
        log.info("POST /api/admin/dlq/replay-all/{}/{} - Replaying all messages", dlqName, originalQueue);
        
        if (dlqManagementService.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "DLQ Service unavailable"));
        }
        Map<String, Object> result = dlqManagementService.get().replayAllMessages(dlqName, originalQueue);
        return ResponseEntity.ok(result);
    }

    /**
     * Delete a specific message from DLQ
     */
    @DeleteMapping("/delete/{dlqName}/{messageId}")
    public ResponseEntity<Map<String, Object>> deleteMessage(
            @PathVariable String dlqName,
            @PathVariable String messageId) {
        log.info("DELETE /api/admin/dlq/delete/{}/{} - Deleting message", dlqName, messageId);
        
        if (dlqManagementService.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "DLQ Service unavailable"));
        }
        boolean success = dlqManagementService.get().deleteMessage(dlqName, messageId);
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Message deleted successfully",
                "messageId", messageId
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "failed",
                "message", "Failed to delete message",
                "messageId", messageId
            ));
        }
    }

    /**
     * Clear all messages from a DLQ
     */
    @DeleteMapping("/clear/{dlqName}")
    public ResponseEntity<Map<String, Object>> clearDLQ(@PathVariable String dlqName) {
        log.info("DELETE /api/admin/dlq/clear/{} - Clearing DLQ", dlqName);
        
        // Validate queue name ends with .dlq
        if (!dlqName.endsWith(".dlq")) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Queue name must end with .dlq",
                "queueName", dlqName
            ));
        }
        
        if (dlqManagementService.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "DLQ Service unavailable"));
        }
        boolean success = dlqManagementService.get().clearDLQ(dlqName);
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "DLQ cleared successfully",
                "queueName", dlqName
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "failed",
                "message", "Failed to clear DLQ",
                "queueName", dlqName
            ));
        }
    }
}
