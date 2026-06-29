package com.chatbot.shared.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.chatbot.core.notification.websocket.NotificationWebSocketHandler;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class MessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(MessageConsumer.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventHandler eventHandler;

    @Autowired
    private NotificationWebSocketHandler notificationWebSocketHandler;

    @RabbitListener(queues = "${rabbitmq.queue.default:chatbot.queue.default}")
    public void handleDefaultMessage(@Payload Object message, 
                                   @Headers Map<String, Object> headers) {
        try {
            processMessage(message, headers, "DEFAULT");
        } catch (Exception e) {
            handleMessageProcessingError(message, headers, e, "DEFAULT");
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.high-priority:chatbot.queue.high-priority}")
    public void handleHighPriorityMessage(@Payload Object message, 
                                       @Headers Map<String, Object> headers) {
        try {
            processMessage(message, headers, "HIGH_PRIORITY");
        } catch (Exception e) {
            handleMessageProcessingError(message, headers, e, "HIGH_PRIORITY");
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.low-priority:chatbot.queue.low-priority}")
    public void handleLowPriorityMessage(@Payload Object message, 
                                      @Headers Map<String, Object> headers) {
        try {
            processMessage(message, headers, "LOW_PRIORITY");
        } catch (Exception e) {
            handleMessageProcessingError(message, headers, e, "LOW_PRIORITY");
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.email:chatbot.queue.email}")
    public void handleEmailMessage(@Payload Object emailMessage, 
                                  @Headers Map<String, Object> headers) {
        try {
            processEmailMessage(emailMessage, headers);
        } catch (Exception e) {
            handleMessageProcessingError(emailMessage, headers, e, "EMAIL");
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.sms:chatbot.queue.sms}")
    public void handleSmsMessage(@Payload Object smsMessage, 
                                @Headers Map<String, Object> headers) {
        try {
            processSmsMessage(smsMessage, headers);
        } catch (Exception e) {
            handleMessageProcessingError(smsMessage, headers, e, "SMS");
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.notification:chatbot.queue.notification}")
    public void handleNotificationMessage(@Payload Object notificationMessage, 
                                        @Headers Map<String, Object> headers) {
        try {
            processNotificationMessage(notificationMessage, headers);
        } catch (Exception e) {
            handleMessageProcessingError(notificationMessage, headers, e, "NOTIFICATION");
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.report:chatbot.queue.report}")
    public void handleReportMessage(@Payload Object reportMessage, 
                                   @Headers Map<String, Object> headers) {
        try {
            processReportMessage(reportMessage, headers);
        } catch (Exception e) {
            handleMessageProcessingError(reportMessage, headers, e, "REPORT");
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.cleanup:chatbot.queue.cleanup}")
    public void handleCleanupMessage(@Payload Object cleanupMessage, 
                                   @Headers Map<String, Object> headers) {
        try {
            processCleanupMessage(cleanupMessage, headers);
        } catch (Exception e) {
            handleMessageProcessingError(cleanupMessage, headers, e, "CLEANUP");
        }
    }

    private void processMessage(Object message, Map<String, Object> headers, String queueType) {
        String correlationId = (String) headers.get("correlationId");
        String userId = (String) headers.get("userId");
        String tenantId = (String) headers.get("tenantId");
        String messageId = (String) headers.get("messageId");
        
        log.info("Processing message from {} queue", queueType);
        log.debug("Message ID: {}, Correlation ID: {}, User ID: {}, Tenant ID: {}", 
                  messageId, correlationId, userId, tenantId);
        
        // Process the message based on its type
        if (message instanceof Event) {
            handleEvent((Event) message, headers);
        } else if (message instanceof Map) {
            handleMapMessage((Map<String, Object>) message, headers);
        } else {
            handleGenericMessage(message, headers);
        }
    }

    private void handleEvent(Event event, Map<String, Object> headers) {
        try {
            eventHandler.handleEvent(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to handle event: " + event.getEventType(), e);
        }
    }

    private void handleMapMessage(Map<String, Object> message, Map<String, Object> headers) {
        String messageType = (String) message.get("messageType");
        
        switch (messageType) {
            case "saga_step":
                handleSagaStepMessage(message, headers);
                break;
            case "saga_completion":
                handleSagaCompletionMessage(message, headers);
                break;
            case "user_activity":
                handleUserActivityMessage(message, headers);
                break;
            case "tenant_activity":
                handleTenantActivityMessage(message, headers);
                break;
            case "app_activity":
                handleAppActivityMessage(message, headers);
                break;
            case "metrics":
                handleMetricsMessage(message, headers);
                break;
            case "alert":
                handleAlertMessage(message, headers);
                break;
            case "health_check":
                handleHealthCheckMessage(message, headers);
                break;
            default:
                handleGenericMessage(message, headers);
        }
    }

    private void handleSagaStepMessage(Map<String, Object> message, Map<String, Object> headers) {
        String sagaId = (String) message.get("sagaId");
        String stepName = (String) message.get("stepName");
        Object stepData = message.get("stepData");
        
        // Handle saga step completion
        log.info("Handling saga step: {} for saga: {}", stepName, sagaId);
    }

    private void handleSagaCompletionMessage(Map<String, Object> message, Map<String, Object> headers) {
        String sagaId = (String) message.get("sagaId");
        Boolean success = (Boolean) message.get("success");
        Object result = message.get("result");
        
        // Handle saga completion
        log.info("Saga {} completed with success: {}", sagaId, success);
    }

    private void handleUserActivityMessage(Map<String, Object> message, Map<String, Object> headers) {
        String userId = (String) message.get("userId");
        String activity = (String) message.get("activity");
        Object data = message.get("data");
        
        // Handle user activity
        log.debug("User activity: {} for user: {}", activity, userId);
    }

    private void handleTenantActivityMessage(Map<String, Object> message, Map<String, Object> headers) {
        String tenantId = (String) message.get("tenantId");
        String activity = (String) message.get("activity");
        Object data = message.get("data");
        
        // Handle tenant activity
        log.debug("Tenant activity: {} for tenant: {}", activity, tenantId);
    }

    private void handleAppActivityMessage(Map<String, Object> message, Map<String, Object> headers) {
        String appId = (String) message.get("appId");
        String activity = (String) message.get("activity");
        Object data = message.get("data");
        
        // Handle app activity
        log.debug("App activity: {} for app: {}", activity, appId);
    }

    private void handleMetricsMessage(Map<String, Object> message, Map<String, Object> headers) {
        // Handle metrics collection
        log.debug("Processing metrics message");
    }

    private void handleAlertMessage(Map<String, Object> message, Map<String, Object> headers) {
        // Handle alert notification
        log.warn("Processing alert message");
    }

    private void handleHealthCheckMessage(Map<String, Object> message, Map<String, Object> headers) {
        String serviceName = (String) message.get("serviceName");
        Object healthData = message.get("healthData");
        
        // Handle health check
        log.debug("Health check for service: {}", serviceName);
    }

    private void processEmailMessage(Object emailMessage, Map<String, Object> headers) {
        // Process email message
        log.info("Processing email message");
    }

    private void processSmsMessage(Object smsMessage, Map<String, Object> headers) {
        // Process SMS message
        log.info("Processing SMS message");
    }

    private void processNotificationMessage(Object notificationMessage, Map<String, Object> headers) {
        log.info("📢 Processing notification message from queue: {}", notificationMessage);
        
        if (notificationMessage instanceof Map) {
            Map<String, Object> notificationMap = (Map<String, Object>) notificationMessage;
            String type = (String) notificationMap.get("type");
            
            if (type != null) {
                // Wrap to match client payload structure: { type: "TYPE", data: { ... } }
                Map<String, Object> wsPayload = Map.of(
                    "type", type,
                    "data", notificationMap
                );
                
                // Get tenantId if available
                Long tenantId = null;
                Object tenantIdObj = notificationMap.get("tenantId");
                if (tenantIdObj instanceof Number) {
                    tenantId = ((Number) tenantIdObj).longValue();
                } else if (tenantIdObj instanceof String) {
                    try {
                        tenantId = Long.parseLong((String) tenantIdObj);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid tenantId format: {}", tenantIdObj);
                    }
                }
                
                switch (type) {
                    case "TENANT_INVITATION":
                        String recipientEmail = (String) notificationMap.get("recipientEmail");
                        if (recipientEmail != null) {
                            notificationWebSocketHandler.sendToUser(recipientEmail, wsPayload);
                            log.info("📧 Sent TENANT_INVITATION to user email: {}", recipientEmail);
                        }
                        break;
                        
                    case "TENANT_JOIN_REQUEST":
                    case "TENANT_INVITATION_ACCEPTED":
                        if (tenantId != null) {
                            notificationWebSocketHandler.broadcastToTenant(tenantId, wsPayload);
                            log.info("🏢 Broadcasted {} to tenant ID: {}", type, tenantId);
                        }
                        break;
                        
                    case "TENANT_JOIN_REQUEST_APPROVED":
                        String memberEmail = (String) notificationMap.get("memberEmail");
                        if (memberEmail != null) {
                            notificationWebSocketHandler.sendToUser(memberEmail, wsPayload);
                            log.info("📧 Sent TENANT_JOIN_REQUEST_APPROVED to user email: {}", memberEmail);
                        }
                        break;
                        
                    default:
                        // For other notification types, if a tenant ID is present, broadcast to tenant
                        if (tenantId != null) {
                            notificationWebSocketHandler.broadcastToTenant(tenantId, wsPayload);
                        } else {
                            log.warn("Unknown notification type and no tenantId available for: {}", type);
                        }
                        break;
                }
            }
        } else {
            log.warn("Notification message is not an instance of Map: {}", notificationMessage != null ? notificationMessage.getClass().getName() : "null");
        }
    }

    private void processReportMessage(Object reportMessage, Map<String, Object> headers) {
        // Process report generation
        log.info("Processing report message");
    }

    private void processCleanupMessage(Object cleanupMessage, Map<String, Object> headers) {
        // Process cleanup task
        log.info("Processing cleanup message");
    }

    private void handleGenericMessage(Object message, Map<String, Object> headers) {
        // Handle generic message
        log.debug("Processing generic message: {}", message.getClass().getSimpleName());
    }

    private void handleMessageProcessingError(Object message, Map<String, Object> headers, Exception e, String queueType) {
        String messageId = (String) headers.get("messageId");
        String correlationId = (String) headers.get("correlationId");
        
        log.error("Error processing message from {} queue. Message ID: {}, Correlation ID: {}, Error: {}", 
                  queueType, messageId, correlationId, e.getMessage(), e);
        
        // Log the error and potentially send to dead letter queue
        // This is where you would implement your error handling strategy
    }

    public CompletableFuture<Void> processMessageAsync(Object message, Map<String, Object> headers, String queueType) {
        return CompletableFuture.runAsync(() -> {
            try {
                processMessage(message, headers, queueType);
            } catch (Exception e) {
                handleMessageProcessingError(message, headers, e, queueType);
            }
        });
    }
}
