package com.chatbot.shared.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class MessageConsumer {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventHandler eventHandler;

    @RabbitListener(queues = "${rabbitmq.queue.default:chatbot.queue.default}")
    public void handleDefaultMessage(@Payload Object message, 
                                   @Header Map<String, Object> headers) {
        try {
            processMessage(message, headers, "DEFAULT");
        } catch (Exception e) {
            handleMessageProcessingError(message, headers, e, "DEFAULT");
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.high-priority:chatbot.queue.high-priority}")
    public void handleHighPriorityMessage(@Payload Object message, 
                                       @Header Map<String, Object> headers) {
        try {
            processMessage(message, headers, "HIGH_PRIORITY");
        } catch (Exception e) {
            handleMessageProcessingError(message, headers, e, "HIGH_PRIORITY");
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.low-priority:chatbot.queue.low-priority}")
    public void handleLowPriorityMessage(@Payload Object message, 
                                      @Header Map<String, Object> headers) {
        try {
            processMessage(message, headers, "LOW_PRIORITY");
        } catch (Exception e) {
            handleMessageProcessingError(message, headers, e, "LOW_PRIORITY");
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.email:chatbot.queue.email}")
    public void handleEmailMessage(@Payload Object emailMessage, 
                                  @Header Map<String, Object> headers) {
        try {
            processEmailMessage(emailMessage, headers);
        } catch (Exception e) {
            handleMessageProcessingError(emailMessage, headers, e, "EMAIL");
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.sms:chatbot.queue.sms}")
    public void handleSmsMessage(@Payload Object smsMessage, 
                                @Header Map<String, Object> headers) {
        try {
            processSmsMessage(smsMessage, headers);
        } catch (Exception e) {
            handleMessageProcessingError(smsMessage, headers, e, "SMS");
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.notification:chatbot.queue.notification}")
    public void handleNotificationMessage(@Payload Object notificationMessage, 
                                        @Header Map<String, Object> headers) {
        try {
            processNotificationMessage(notificationMessage, headers);
        } catch (Exception e) {
            handleMessageProcessingError(notificationMessage, headers, e, "NOTIFICATION");
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.report:chatbot.queue.report}")
    public void handleReportMessage(@Payload Object reportMessage, 
                                  @Header Map<String, Object> headers) {
        try {
            processReportMessage(reportMessage, headers);
        } catch (Exception e) {
            handleMessageProcessingError(reportMessage, headers, e, "REPORT");
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.cleanup:chatbot.queue.cleanup}")
    public void handleCleanupMessage(@Payload Object cleanupMessage, 
                                   @Header Map<String, Object> headers) {
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
        
        System.out.println("Processing message from " + queueType + " queue");
        System.out.println("Message ID: " + messageId);
        System.out.println("Correlation ID: " + correlationId);
        System.out.println("User ID: " + userId);
        System.out.println("Tenant ID: " + tenantId);
        
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
        System.out.println("Handling saga step: " + stepName + " for saga: " + sagaId);
    }

    private void handleSagaCompletionMessage(Map<String, Object> message, Map<String, Object> headers) {
        String sagaId = (String) message.get("sagaId");
        Boolean success = (Boolean) message.get("success");
        Object result = message.get("result");
        
        // Handle saga completion
        System.out.println("Saga " + sagaId + " completed with success: " + success);
    }

    private void handleUserActivityMessage(Map<String, Object> message, Map<String, Object> headers) {
        String userId = (String) message.get("userId");
        String activity = (String) message.get("activity");
        Object data = message.get("data");
        
        // Handle user activity
        System.out.println("User activity: " + activity + " for user: " + userId);
    }

    private void handleTenantActivityMessage(Map<String, Object> message, Map<String, Object> headers) {
        String tenantId = (String) message.get("tenantId");
        String activity = (String) message.get("activity");
        Object data = message.get("data");
        
        // Handle tenant activity
        System.out.println("Tenant activity: " + activity + " for tenant: " + tenantId);
    }

    private void handleAppActivityMessage(Map<String, Object> message, Map<String, Object> headers) {
        String appId = (String) message.get("appId");
        String activity = (String) message.get("activity");
        Object data = message.get("data");
        
        // Handle app activity
        System.out.println("App activity: " + activity + " for app: " + appId);
    }

    private void handleMetricsMessage(Map<String, Object> message, Map<String, Object> headers) {
        // Handle metrics collection
        System.out.println("Processing metrics message");
    }

    private void handleAlertMessage(Map<String, Object> message, Map<String, Object> headers) {
        // Handle alert notification
        System.out.println("Processing alert message");
    }

    private void handleHealthCheckMessage(Map<String, Object> message, Map<String, Object> headers) {
        String serviceName = (String) message.get("serviceName");
        Object healthData = message.get("healthData");
        
        // Handle health check
        System.out.println("Health check for service: " + serviceName);
    }

    private void processEmailMessage(Object emailMessage, Map<String, Object> headers) {
        // Process email message
        System.out.println("Processing email message");
    }

    private void processSmsMessage(Object smsMessage, Map<String, Object> headers) {
        // Process SMS message
        System.out.println("Processing SMS message");
    }

    private void processNotificationMessage(Object notificationMessage, Map<String, Object> headers) {
        // Process notification message
        System.out.println("Processing notification message");
    }

    private void processReportMessage(Object reportMessage, Map<String, Object> headers) {
        // Process report generation
        System.out.println("Processing report message");
    }

    private void processCleanupMessage(Object cleanupMessage, Map<String, Object> headers) {
        // Process cleanup task
        System.out.println("Processing cleanup message");
    }

    private void handleGenericMessage(Object message, Map<String, Object> headers) {
        // Handle generic message
        System.out.println("Processing generic message: " + message.getClass().getSimpleName());
    }

    private void handleMessageProcessingError(Object message, Map<String, Object> headers, Exception e, String queueType) {
        String messageId = (String) headers.get("messageId");
        String correlationId = (String) headers.get("correlationId");
        
        System.err.println("Error processing message from " + queueType + " queue");
        System.err.println("Message ID: " + messageId);
        System.err.println("Correlation ID: " + correlationId);
        System.err.println("Error: " + e.getMessage());
        
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
