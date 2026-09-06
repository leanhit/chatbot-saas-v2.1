package com.chatbot.spokes.minio.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka event producer for MinIO spoke events
 * Publishes events for other spokes to consume
 */
@Service
@Slf4j
public class MinioEventProducer {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String MINIO_EVENTS_TOPIC = "minio-events";

    public MinioEventProducer(
            @Autowired(required = false) KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Publish MinIO file uploaded event
     */
    public void publishFileUploaded(MinioFileUploadedEvent event) {
        if (kafkaTemplate == null) {
            log.debug("Kafka disabled, skipping publishFileUploaded");
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(MINIO_EVENTS_TOPIC, event.getFileId().toString(), json);
            log.info("Published MinioFileUploadedEvent for file: {}", event.getFileId());
        } catch (Exception e) {
            log.error("Failed to publish MinioFileUploadedEvent", e);
        }
    }
}
