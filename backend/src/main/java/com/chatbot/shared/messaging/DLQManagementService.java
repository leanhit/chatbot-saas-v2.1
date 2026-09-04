package com.chatbot.shared.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing Dead Letter Queues (DLQ)
 * Provides monitoring, inspection, and replay capabilities for failed messages
 */
@Service
@ConditionalOnClass(RabbitTemplate.class)
@Slf4j
public class DLQManagementService {

    private final Optional<RabbitTemplate> rabbitTemplate;
    private final Optional<AmqpAdmin> amqpAdmin;
    private final ObjectMapper objectMapper;

    public DLQManagementService(Optional<RabbitTemplate> rabbitTemplate, Optional<AmqpAdmin> amqpAdmin, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.amqpAdmin = amqpAdmin;
        this.objectMapper = objectMapper;
    }

    /**
     * Get DLQ statistics for all queues
     */
    public Map<String, Object> getDLQStatistics() {
        Map<String, Object> stats = new HashMap<>();
        if (amqpAdmin.isEmpty()) {
            return Map.of("error", "AmqpAdmin is unavailable (RabbitMQ disabled)");
        }
        
        String[] queueNames = {
            "chatbot.queue.default.dlq",
            "chatbot.queue.high-priority.dlq", 
            "chatbot.queue.low-priority.dlq",
            "chatbot.queue.email.dlq",
            "chatbot.queue.sms.dlq",
            "chatbot.queue.notification.dlq",
            "chatbot.queue.report.dlq",
            "chatbot.queue.cleanup.dlq"
        };

        for (String queueName : queueNames) {
            try {
                QueueInformation queueInfo = amqpAdmin.get().getQueueInfo(queueName);
                if (queueInfo != null) {
                    Map<String, Object> queueStats = new HashMap<>();
                    queueStats.put("messageCount", queueInfo.getMessageCount());
                    queueStats.put("consumerCount", queueInfo.getConsumerCount());
                    stats.put(queueName, queueStats);
                }
            } catch (Exception e) {
                log.warn("Failed to get stats for queue {}: {}", queueName, e.getMessage());
                stats.put(queueName, Map.of("error", e.getMessage()));
            }
        }

        return stats;
    }

    /**
     * Inspect messages in a specific DLQ without consuming them
     */
    public Map<String, Object> inspectDLQ(String queueName, int maxMessages) {
        Map<String, Object> result = new HashMap<>();
        if (rabbitTemplate.isEmpty()) {
            result.put("error", "RabbitTemplate is unavailable (RabbitMQ disabled)");
            return result;
        }
        
        try {
            RabbitTemplate template = rabbitTemplate.get();
            // Receive messages without auto-ack for inspection
            Message[] messages = new Message[maxMessages];
            int count = 0;
            
            for (int i = 0; i < maxMessages; i++) {
                Message message = template.receive(queueName);
                if (message == null) break;
                
                messages[count] = message;
                count++;
            }
            
            // Re-queue messages for later processing
            for (int i = 0; i < count; i++) {
                template.send(queueName, messages[i]);
            }
            
            result.put("queueName", queueName);
            result.put("messageCount", count);
            result.put("messages", parseMessages(messages, count));
            
        } catch (Exception e) {
            log.error("Error inspecting DLQ {}: {}", queueName, e.getMessage(), e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * Replay a specific message from DLQ to the original queue
     */
    public boolean replayMessage(String dlqName, String originalQueue, String messageId) {
        if (rabbitTemplate.isEmpty()) {
            log.warn("RabbitTemplate unavailable. Cannot replay message {}", messageId);
            return false;
        }
        try {
            log.info("Replaying message {} from {} to {}", messageId, dlqName, originalQueue);
            
            // Find and remove the specific message from DLQ
            Message message = findAndRemoveMessage(dlqName, messageId);
            if (message == null) {
                log.warn("Message {} not found in {}", messageId, dlqName);
                return false;
            }
            
            // Remove DLQ headers to prevent re-routing to DLQ
            MessageBuilder builder = MessageBuilder.fromMessage(message);
            builder.removeHeader("x-death");
            
            // Send to original queue
            rabbitTemplate.get().send(originalQueue, builder.build());
            
            log.info("Successfully replayed message {} to {}", messageId, originalQueue);
            return true;
            
        } catch (Exception e) {
            log.error("Error replaying message {}: {}", messageId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Replay all messages from a DLQ to the original queue
     */
    public Map<String, Object> replayAllMessages(String dlqName, String originalQueue) {
        Map<String, Object> result = new HashMap<>();
        if (rabbitTemplate.isEmpty()) {
            result.put("error", "RabbitTemplate is unavailable (RabbitMQ disabled)");
            result.put("status", "failed");
            return result;
        }
        int successCount = 0;
        int failCount = 0;
        
        try {
            log.info("Replaying all messages from {} to {}", dlqName, originalQueue);
            RabbitTemplate template = rabbitTemplate.get();
            
            Message message;
            while ((message = template.receive(dlqName)) != null) {
                try {
                    // Remove DLQ headers
                    MessageBuilder builder = MessageBuilder.fromMessage(message);
                    builder.removeHeader("x-death");
                    
                    // Send to original queue
                    template.send(originalQueue, builder.build());
                    successCount++;
                    
                } catch (Exception e) {
                    log.error("Failed to replay message: {}", e.getMessage());
                    failCount++;
                    // Re-queue failed message back to DLQ
                    template.send(dlqName, message);
                }
            }
            
            result.put("dlqName", dlqName);
            result.put("originalQueue", originalQueue);
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("status", "completed");
            
            log.info("Replay completed: {} successful, {} failed", successCount, failCount);
            
        } catch (Exception e) {
            log.error("Error replaying messages from {}: {}", dlqName, e.getMessage(), e);
            result.put("error", e.getMessage());
            result.put("status", "failed");
        }
        
        return result;
    }

    /**
     * Delete a specific message from DLQ
     */
    public boolean deleteMessage(String dlqName, String messageId) {
        try {
            Message message = findAndRemoveMessage(dlqName, messageId);
            if (message != null) {
                log.info("Deleted message {} from {}", messageId, dlqName);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error deleting message {}: {}", messageId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Clear all messages from a DLQ
     */
    public boolean clearDLQ(String dlqName) {
        if (rabbitTemplate.isEmpty()) {
            return false;
        }
        try {
            log.info("Clearing all messages from {}", dlqName);
            RabbitTemplate template = rabbitTemplate.get();

            int count = 0;
            while (template.receive(dlqName) != null) {
                count++;
            }

            log.info("Cleared {} messages from {}", count, dlqName);
            return true;

        } catch (Exception e) {
            log.error("Error clearing DLQ {}: {}", dlqName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Helper method to find and remove a specific message by ID
     */
    private Message findAndRemoveMessage(String queueName, String messageId) {
        if (rabbitTemplate.isEmpty()) {
            return null;
        }
        RabbitTemplate template = rabbitTemplate.get();

        Message message = template.receive(queueName);
        if (message != null) {
            String msgId = message.getMessageProperties().getMessageId();
            if (msgId != null && msgId.equals(messageId)) {
                return message;
            } else {
                // Not the message we're looking for, put it back
                template.send(queueName, message);
                return null;
            }
        }
        return null;
    }

    /**
     * Parse messages for inspection
     */
    private Map<String, Object>[] parseMessages(Message[] messages, int count) {
        @SuppressWarnings("unchecked")
        Map<String, Object>[] parsed = new Map[count];
        
        for (int i = 0; i < count; i++) {
            Map<String, Object> msgInfo = new HashMap<>();
            Message message = messages[i];
            
            msgInfo.put("messageId", message.getMessageProperties().getMessageId());
            msgInfo.put("timestamp", message.getMessageProperties().getTimestamp());
            msgInfo.put("contentType", message.getMessageProperties().getContentType());
            
            // Parse death headers for retry information
            Map<String, Object> headers = message.getMessageProperties().getHeaders();
            if (headers != null && headers.containsKey("x-death")) {
                msgInfo.put("deathHeaders", headers.get("x-death"));
            }
            
            try {
                String body = new String(message.getBody());
                msgInfo.put("body", body);
                
                // Try to parse as JSON
                if (body.startsWith("{") || body.startsWith("[")) {
                    Object json = objectMapper.readValue(body, Object.class);
                    msgInfo.put("parsedBody", json);
                }
            } catch (Exception e) {
                msgInfo.put("body", new String(message.getBody()));
                msgInfo.put("parseError", e.getMessage());
            }
            
            parsed[i] = msgInfo;
        }
        
        return parsed;
    }
}
