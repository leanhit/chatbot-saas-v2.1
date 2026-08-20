package com.chatbot.spokes.facebook.webhook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.chatbot.config.KafkaConfig;

/**
 * Kafka producer that forwards Facebook webhook payloads to the {@code facebook-events}
 * topic. The payload is serialized as JSON string. This enables asynchronous
 * processing of webhook events by a downstream consumer.
 */
@Service
@Slf4j
public class FacebookKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public FacebookKafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        this.objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * Sends a payload to Kafka with a partition key.
     *
     * @param key     the partition key (e.g. senderId) to guarantee ordering
     * @param payload the object to be sent; it will be converted to a JSON string
     */
    public void send(String key, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(new ProducerRecord<>(KafkaConfig.FACEBOOK_EVENT_TOPIC, key, json));
            log.info("[Kafka] Produced message to topic {} with key {}: {}", KafkaConfig.FACEBOOK_EVENT_TOPIC, key, json);
        } catch (Exception e) {
            log.error("[Kafka] Failed to produce message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send event to Kafka topic: " + KafkaConfig.FACEBOOK_EVENT_TOPIC, e);
        }
    }
}
