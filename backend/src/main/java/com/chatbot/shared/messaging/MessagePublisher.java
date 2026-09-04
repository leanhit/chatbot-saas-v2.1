package com.chatbot.shared.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@ConditionalOnClass(RabbitTemplate.class)
@Slf4j
public class MessagePublisher {

    private final Optional<RabbitTemplate> rabbitTemplate;

    public MessagePublisher(Optional<RabbitTemplate> rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Value("${rabbitmq.exchange.name:chatbot.exchange}")
    private String exchangeName;

    public void publish(String routingKey, Object message) {
        publish(routingKey, message, null);
    }

    public void publish(String routingKey, Object message, Map<String, Object> headers) {
        if (rabbitTemplate.isEmpty()) {
            log.warn("RabbitTemplate not available (RabbitMQ disabled). Skipping message to routing key: {}", routingKey);
            return;
        }
        try {
            RabbitTemplate template = rabbitTemplate.get();
            org.springframework.amqp.core.Message amqpMessage = template.getMessageConverter().toMessage(message, null);
            
            if (headers != null) {
                headers.forEach((key, value) -> amqpMessage.getMessageProperties().setHeader(key, value));
            }
            
            // Add default headers
            amqpMessage.getMessageProperties().setMessageId(UUID.randomUUID().toString());
            amqpMessage.getMessageProperties().setTimestamp(new java.util.Date());
            amqpMessage.getMessageProperties().setContentType("application/json");
            
            template.send(exchangeName, routingKey, amqpMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish message to routing key: " + routingKey, e);
        }
    }

    public void publishHighPriority(String routingKey, Object message) {
        publishWithPriority(routingKey, message, 10);
    }

    public void publishLowPriority(String routingKey, Object message) {
        publishWithPriority(routingKey, message, 1);
    }

    public void publishWithPriority(String routingKey, Object message, int priority) {
        if (rabbitTemplate.isEmpty()) {
            log.warn("RabbitTemplate not available (RabbitMQ disabled). Skipping priority message to routing key: {}", routingKey);
            return;
        }
        try {
            RabbitTemplate template = rabbitTemplate.get();
            org.springframework.amqp.core.Message amqpMessage = template.getMessageConverter().toMessage(message, null);
            
            amqpMessage.getMessageProperties().setMessageId(UUID.randomUUID().toString());
            amqpMessage.getMessageProperties().setTimestamp(new java.util.Date());
            amqpMessage.getMessageProperties().setContentType("application/json");
            amqpMessage.getMessageProperties().setPriority(priority);
            
            template.send(exchangeName, routingKey, amqpMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish message with priority to routing key: " + routingKey, e);
        }
    }

    public void publishWithDelay(String routingKey, Object message, long delayMillis) {
        if (rabbitTemplate.isEmpty()) {
            log.warn("RabbitTemplate not available (RabbitMQ disabled). Skipping delayed message to routing key: {}", routingKey);
            return;
        }
        try {
            RabbitTemplate template = rabbitTemplate.get();
            org.springframework.amqp.core.Message amqpMessage = template.getMessageConverter().toMessage(message, null);
            
            amqpMessage.getMessageProperties().setMessageId(UUID.randomUUID().toString());
            amqpMessage.getMessageProperties().setTimestamp(new java.util.Date());
            amqpMessage.getMessageProperties().setContentType("application/json");
            
            template.send(exchangeName, routingKey, amqpMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish delayed message to routing key: " + routingKey, e);
        }
    }

    public void publishWithTTL(String routingKey, Object message, long ttlMillis) {
        if (rabbitTemplate.isEmpty()) {
            log.warn("RabbitTemplate not available (RabbitMQ disabled). Skipping TTL message to routing key: {}", routingKey);
            return;
        }
        try {
            RabbitTemplate template = rabbitTemplate.get();
            org.springframework.amqp.core.Message amqpMessage = template.getMessageConverter().toMessage(message, null);
            
            amqpMessage.getMessageProperties().setMessageId(UUID.randomUUID().toString());
            amqpMessage.getMessageProperties().setTimestamp(new java.util.Date());
            amqpMessage.getMessageProperties().setContentType("application/json");
            amqpMessage.getMessageProperties().setExpiration(String.valueOf(ttlMillis));
            
            template.send(exchangeName, routingKey, amqpMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish message with TTL to routing key: " + routingKey, e);
        }
    }

    public void publishEmail(Object emailMessage) {
        publish("chatbot.email.send", emailMessage);
    }

    public void publishSms(Object smsMessage) {
        publish("chatbot.sms.send", smsMessage);
    }

    public void publishNotification(Object notificationMessage) {
        publish("chatbot.notification.send", notificationMessage);
    }

    public void publishReport(Object reportMessage) {
        publish("chatbot.report.generate", reportMessage);
    }

    public void publishCleanup(Object cleanupMessage) {
        publish("chatbot.cleanup.execute", cleanupMessage);
    }

    public void publishEvent(String eventType, Object eventData) {
        Event event = new Event();
        event.setEventType(eventType);
        event.setEventData(eventData);
        event.setTimestamp(java.time.LocalDateTime.now());
        event.setEventId(UUID.randomUUID().toString());
        
        publish("chatbot.event." + eventType, event);
    }

    public void publishEvent(String eventType, Object eventData, String correlationId) {
        Event event = new Event();
        event.setEventType(eventType);
        event.setEventData(eventData);
        event.setTimestamp(java.time.LocalDateTime.now());
        event.setEventId(UUID.randomUUID().toString());
        event.setCorrelationId(correlationId);
        
        publish("chatbot.event." + eventType, event);
    }

    public void publishSagaStep(String sagaId, String stepName, Object stepData) {
        Map<String, Object> sagaMessage = Map.of(
            "sagaId", sagaId,
            "stepName", stepName,
            "stepData", stepData,
            "timestamp", java.time.LocalDateTime.now()
        );
        
        publish("chatbot.saga.step", sagaMessage);
    }

    public void publishSagaCompletion(String sagaId, boolean success, Object result) {
        Map<String, Object> sagaMessage = Map.of(
            "sagaId", sagaId,
            "success", success,
            "result", result,
            "timestamp", java.time.LocalDateTime.now()
        );
        
        publish("chatbot.saga.completion", sagaMessage);
    }

    public void publishUserActivity(String userId, String activity, Object data) {
        Map<String, Object> activityMessage = Map.of(
            "userId", userId,
            "activity", activity,
            "data", data,
            "timestamp", java.time.LocalDateTime.now()
        );
        
        publish("chatbot.user.activity", activityMessage);
    }

    public void publishTenantActivity(String tenantId, String activity, Object data) {
        Map<String, Object> activityMessage = Map.of(
            "tenantId", tenantId,
            "activity", activity,
            "data", data,
            "timestamp", java.time.LocalDateTime.now()
        );
        
        publish("chatbot.tenant.activity", activityMessage);
    }

    public void publishAppActivity(String appId, String activity, Object data) {
        Map<String, Object> activityMessage = Map.of(
            "appId", appId,
            "activity", activity,
            "data", data,
            "timestamp", java.time.LocalDateTime.now()
        );
        
        publish("chatbot.app.activity", activityMessage);
    }

    public void publishMetrics(Object metricsData) {
        publish("chatbot.metrics.collect", metricsData);
    }

    public void publishAlert(Object alertData) {
        publishHighPriority("chatbot.alert.notify", alertData);
    }

    public void publishHealthCheck(String serviceName, Object healthData) {
        Map<String, Object> healthMessage = Map.of(
            "serviceName", serviceName,
            "healthData", healthData,
            "timestamp", java.time.LocalDateTime.now()
        );
        
        publish("chatbot.health.check", healthMessage);
    }

    public void publishWithCorrelationId(String routingKey, Object message, String correlationId) {
        Map<String, Object> headers = Map.of("correlationId", correlationId);
        publish(routingKey, message, headers);
    }

    public void publishWithUserId(String routingKey, Object message, String userId) {
        Map<String, Object> headers = Map.of("userId", userId);
        publish(routingKey, message, headers);
    }

    public void publishWithTenantId(String routingKey, Object message, String tenantId) {
        Map<String, Object> headers = Map.of("tenantId", tenantId);
        publish(routingKey, message, headers);
    }

    public void publishWithAllHeaders(String routingKey, Object message, String correlationId, String userId, String tenantId) {
        Map<String, Object> headers = Map.of(
            "correlationId", correlationId,
            "userId", userId,
            "tenantId", tenantId
        );
        publish(routingKey, message, headers);
    }
}
