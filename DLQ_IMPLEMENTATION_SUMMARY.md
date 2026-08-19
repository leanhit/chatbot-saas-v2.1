# Dead Letter Queue (DLQ) Implementation Summary

## Overview
Implemented comprehensive Dead Letter Queue (DLQ) functionality for RabbitMQ to handle failed messages from Facebook webhooks and other message consumers. Messages that fail after retry attempts are automatically routed to DLQ for inspection and replay instead of being lost.

## Changes Made

### 1. RabbitMQ Configuration Enhancement (RabbitMQConfig.java)

**Added Dead Letter Exchange (DLX)**
- Created dedicated DLX: `chatbot.dlx` (Direct Exchange)
- All main queues now route failed messages to DLX

**Enhanced Queue Configuration**
- All 8 queues now have DLQ arguments:
  - `x-dead-letter-exchange`: chatbot.dlx
  - `x-dead-letter-routing-key`: {queueName}.dlq

**Complete DLQ Setup**
- Created DLQ for all 8 queues:
  - chatbot.queue.default.dlq
  - chatbot.queue.high-priority.dlq
  - chatbot.queue.low-priority.dlq
  - chatbot.queue.email.dlq
  - chatbot.queue.sms.dlq
  - chatbot.queue.notification.dlq
  - chatbot.queue.report.dlq
  - chatbot.queue.cleanup.dlq

**Retry Configuration**
- Added retry logic with exponential backoff:
  - max-attempts: 3 (configurable)
  - initial-interval: 1000ms (1 second)
  - multiplier: 2.0 (exponential)
  - max-interval: 10000ms (10 seconds)

### 2. Application Properties Update (application.properties)

Added RabbitMQ retry configuration:
```properties
rabbitmq.dlx.name=chatbot.dlx
rabbitmq.retry.max-attempts=3
rabbitmq.retry.initial-interval=1000
rabbitmq.retry.multiplier=2.0
rabbitmq.retry.max-interval=10000
```

### 3. DLQ Management Service (DLQManagementService.java)

Created comprehensive service for DLQ management:

**Features:**
- `getDLQStatistics()` - Get message counts for all DLQs
- `inspectDLQ()` - Inspect messages without consuming them
- `replayMessage()` - Replay specific message to original queue
- `replayAllMessages()` - Replay all messages from DLQ
- `deleteMessage()` - Delete specific message from DLQ
- `clearDLQ()` - Clear all messages from DLQ

**Message Handling:**
- Removes `x-death` headers during replay to prevent re-routing to DLQ
- Parses message bodies for inspection
- Tracks retry information from death headers
- 7-day TTL for DLQ messages

### 4. DLQ Management Controller (DLQManagementController.java)

Created REST API endpoints for DLQ management (Admin only):

**Endpoints:**
- `GET /api/admin/dlq/statistics` - Get all DLQ statistics
- `GET /api/admin/dlq/inspect/{queueName}` - Inspect DLQ messages
- `POST /api/admin/dlq/replay/{dlqName}/{originalQueue}/{messageId}` - Replay specific message
- `POST /api/admin/dlq/replay-all/{dlqName}/{originalQueue}` - Replay all messages
- `DELETE /api/admin/dlq/delete/{dlqName}/{messageId}` - Delete specific message
- `DELETE /api/admin/dlq/clear/{dlqName}` - Clear entire DLQ

**Security:**
- All endpoints require `ROLE_ADMIN` authorization
- Queue name validation (must end with .dlq)

## Message Flow

### Normal Processing
```
Producer → Main Queue → Consumer → Process
```

### Failed Message with Retry
```
Producer → Main Queue → Consumer (Fail)
         → Retry 1 (1s delay)
         → Retry 2 (2s delay)
         → Retry 3 (4s delay)
         → DLQ (after 3 failures)
```

### DLQ Replay
```
DLQ → Admin Review → Replay → Main Queue → Consumer → Process
```

## Retry Behavior

**Exponential Backoff:**
- Attempt 1: Immediate
- Attempt 2: 1 second delay
- Attempt 3: 2 seconds delay
- Attempt 4: 4 seconds delay (if configured)
- After max attempts: Route to DLQ

**Death Headers:**
Messages in DLQ contain `x-death` headers with:
- `count`: Number of retry attempts
- `reason`: Failure reason
- `queue`: Original queue name
- `time`: Timestamp of each death

## Kafka DLQ Support

**Existing Kafka Configuration:**
- Kafka already has DLQ support via `DeadLetterPublishingRecoverer`
- Facebook webhook events use Kafka topic: `facebook-events`
- Retry: 3 attempts with 2-second fixed backoff
- Failed messages published to DLQ topic automatically

## Monitoring & Management

### Check DLQ Statistics
```bash
curl -X GET http://localhost:8080/api/admin/dlq/statistics \
  -H "Authorization: Bearer {admin_token}"
```

### Inspect DLQ Messages
```bash
curl -X GET "http://localhost:8080/api/admin/dlq/inspect/chatbot.queue.default.dlq?maxMessages=10" \
  -H "Authorization: Bearer {admin_token}"
```

### Replay All Messages
```bash
curl -X POST http://localhost:8080/api/admin/dlq/replay-all/chatbot.queue.default.dlq/chatbot.queue.default \
  -H "Authorization: Bearer {admin_token}"
```

### Clear DLQ
```bash
curl -X DELETE http://localhost:8080/api/admin/dlq/clear/chatbot.queue.default.dlq \
  -H "Authorization: Bearer {admin_token}"
```

### RabbitMQ Management UI
Access RabbitMQ Management Console:
```
URL: http://localhost:15672
Username: admin
Password: admin123
```

Navigate to:
- Queues tab → View DLQs (ending with .dlq)
- Get message counts
- View message details
- Manually move messages if needed

## Benefits

1. **No Message Loss**: Failed messages preserved in DLQ instead of being dropped
2. **Admin Visibility**: Failed messages visible for inspection and debugging
3. **Replay Capability**: Messages can be replayed after fixing issues
4. **Retry Logic**: Automatic retry with exponential backoff reduces temporary failures
5. **Monitoring**: DLQ statistics help identify systemic issues
6. **Audit Trail**: Death headers provide failure history

## Configuration

### Adjust Retry Settings
Edit `application.properties`:
```properties
rabbitmq.retry.max-attempts=5          # Increase retry attempts
rabbitmq.retry.initial-interval=2000   # Start with 2s delay
rabbitmq.retry.multiplier=3.0          # Faster exponential growth
rabbitmq.retry.max-interval=30000      # Max 30s delay
```

### Adjust DLQ TTL
Edit `RabbitMQConfig.java`:
```java
@Bean
public Queue defaultDLQ() {
    return QueueBuilder.durable(defaultQueue + ".dlq")
            .withArgument("x-message-ttl", 1209600000) // 14 days
            .build();
}
```

## Troubleshooting

### High DLQ Message Count
- Check application logs for error patterns
- Inspect DLQ messages to identify common failures
- Fix root cause before replaying messages
- Consider increasing retry attempts for transient failures

### Messages Not Reaching DLQ
- Verify DLX and DLQ bindings in RabbitMQ Management UI
- Check queue arguments include `x-dead-letter-exchange`
- Ensure retry configuration is active
- Review RabbitMQ logs for routing errors

### Replay Failures
- Check original queue exists and is accessible
- Verify message format is still valid
- Remove `x-death` headers before replay (handled automatically)
- Check consumer is running and healthy

### Performance Impact
- DLQ adds minimal overhead to normal message flow
- Retry delays only affect failed messages
- DLQ inspection is non-destructive
- Replay operations are manual/admin-controlled

## Files Modified

1. `backend/src/main/java/com/chatbot/shared/messaging/RabbitMQConfig.java` - DLX/DLQ configuration
2. `backend/src/main/resources/application.properties` - Retry settings
3. `backend/src/main/java/com/chatbot/shared/messaging/DLQManagementService.java` - DLQ management (new)
4. `backend/src/main/java/com/chatbot/shared/messaging/DLQManagementController.java` - REST API (new)

## Next Steps

1. Test DLQ functionality with intentional failures
2. Set up monitoring alerts for high DLQ message counts
3. Create admin dashboard for DLQ management
4. Document common failure patterns and resolution procedures
5. Consider automated replay for specific error types
6. Set up Prometheus metrics for DLQ monitoring

## Security Considerations

- DLQ management endpoints require admin role
- Sensitive message data in DLQ should be encrypted
- Regular DLQ cleanup to prevent data accumulation
- Audit logging for DLQ operations
- Network access control to RabbitMQ Management UI
