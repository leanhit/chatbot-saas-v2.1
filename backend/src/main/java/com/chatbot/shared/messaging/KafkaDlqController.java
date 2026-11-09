package com.chatbot.shared.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Kafka Dead Letter Topic management: list, inspect, replay *.DLT topics.
 */
@RestController
@RequestMapping("/api/admin/dlq/kafka")
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class KafkaDlqController {

    private final Optional<KafkaDlqService> kafkaDlqService;

    public KafkaDlqController(Optional<KafkaDlqService> kafkaDlqService) {
        this.kafkaDlqService = kafkaDlqService;
    }

    @GetMapping("/topics")
    public ResponseEntity<Map<String, Object>> listDlqTopics() {
        if (kafkaDlqService.isEmpty()) {
            return ResponseEntity.status(503).body(Map.of("error", "Kafka DLQ service unavailable"));
        }
        return ResponseEntity.ok(kafkaDlqService.get().listDlqTopics());
    }

    @GetMapping("/topics/{topic}/messages")
    public ResponseEntity<List<Map<String, Object>>> readDlq(
            @PathVariable String topic,
            @RequestParam(defaultValue = "50") int max) {
        if (kafkaDlqService.isEmpty()) {
            return ResponseEntity.status(503).build();
        }
        return ResponseEntity.ok(kafkaDlqService.get().readDlq(topic, Math.min(max, 500)));
    }

    @PostMapping("/topics/{topic}/replay")
    public ResponseEntity<Map<String, Object>> replay(
            @PathVariable String topic,
            @RequestParam(defaultValue = "50") int max) {
        if (kafkaDlqService.isEmpty()) {
            return ResponseEntity.status(503).build();
        }
        return ResponseEntity.ok(kafkaDlqService.get().replay(topic, Math.min(max, 500)));
    }
}
