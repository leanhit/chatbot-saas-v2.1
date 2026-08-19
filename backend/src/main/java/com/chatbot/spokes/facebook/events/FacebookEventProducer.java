package com.chatbot.spokes.facebook.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka event producer for Facebook spoke events
 * Publishes events for other spokes to consume
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FacebookEventProducer {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String FACEBOOK_EVENTS_TOPIC = "facebook-events";
    
    /**
     * Publish Facebook connection created event
     */
    public void publishConnectionCreated(FacebookConnectionCreatedEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(FACEBOOK_EVENTS_TOPIC, event.getConnectionId().toString(), json);
            log.info("Published FacebookConnectionCreatedEvent for connection: {}", event.getConnectionId());
        } catch (Exception e) {
            log.error("Failed to publish FacebookConnectionCreatedEvent", e);
        }
    }
    
    /**
     * Publish Facebook connection updated event
     */
    public void publishConnectionUpdated(FacebookConnectionUpdatedEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(FACEBOOK_EVENTS_TOPIC, event.getConnectionId().toString(), json);
            log.info("Published FacebookConnectionUpdatedEvent for connection: {}", event.getConnectionId());
        } catch (Exception e) {
            log.error("Failed to publish FacebookConnectionUpdatedEvent", e);
        }
    }
    
    /**
     * Publish Facebook message received event
     */
    public void publishMessageReceived(FacebookMessageReceivedEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(FACEBOOK_EVENTS_TOPIC, event.getMessageId(), json);
            log.info("Published FacebookMessageReceivedEvent for message: {}", event.getMessageId());
        } catch (Exception e) {
            log.error("Failed to publish FacebookMessageReceivedEvent", e);
        }
    }
}
