package com.chatbot.spokes.facebook.webhook.consumer;

import com.chatbot.config.KafkaConfig;
import com.chatbot.spokes.facebook.webhook.dto.FacebookKafkaEvent;
import com.chatbot.spokes.facebook.webhook.processor.FacebookEventProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer that processes Facebook webhook events asynchronously by delegating to FacebookEventProcessor.
 */
@Service("facebookWebhookEventConsumer")
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
@RequiredArgsConstructor
public class FacebookEventConsumer {

    private final FacebookEventProcessor eventProcessor;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaConfig.FACEBOOK_EVENT_TOPIC, groupId = "facebook-consumer-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(String messageJson) {
        FacebookKafkaEvent event = null;
        try {
            event = objectMapper.readValue(messageJson, FacebookKafkaEvent.class);
        } catch (Exception e) {
            log.error("❌ [Kafka Consumer] Failed to deserialize JSON: {}", e.getMessage(), e);
            return;
        }

        try {
            eventProcessor.processEvent(event);
        } catch (Exception e) {
            // Already handled and logged in processEvent
        }
    }

    /**
     * Legacy method delegate for compatibility
     */
    public void processEvent(FacebookKafkaEvent event) {
        eventProcessor.processEvent(event);
    }
}

