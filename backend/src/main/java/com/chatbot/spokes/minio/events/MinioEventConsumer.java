package com.chatbot.spokes.minio.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Kafka event consumer for MinIO spoke events
 * Other spokes can publish events that MinIO spoke needs to consume
 */
@Service
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class MinioEventConsumer {
    
    /**
     * Consume events from other spokes
     * This is a placeholder for future inter-spoke communication
     */
    @KafkaListener(
        topics = "spokes-events",
        groupId = "minio-spoke-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleSpokesEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        try {
            log.info("Received spokes event with key: {}", key);
            
            // Parse event and determine type
            // This will be implemented when other spokes start publishing events
            // that MinIO spoke needs to consume
            
        } catch (Exception e) {
            log.error("Failed to process spokes event", e);
        }
    }
}
