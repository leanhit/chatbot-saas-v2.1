# Message Hub API Documentation

## Overview
The Message Hub provides message routing, processing, and decision-making services for the chatbot SaaS platform.

## Base URL
```
http://localhost:8080/api/v1/messages
```

## Authentication
All endpoints require JWT token in Authorization header:
```
Authorization: Bearer <jwt-token>
```

## Endpoints

### Message Routing

#### POST /route
Route a message to appropriate destination.

**Request Body:**
```json
{
  "message": {
    "id": "msg_123",
    "content": "Hello, I need help with my order",
    "type": "TEXT",
    "sender": {
      "id": "user_456",
      "type": "CUSTOMER"
    },
    "recipient": {
      "id": "bot_789",
      "type": "BOT"
    },
    "channel": "FACEBOOK",
    "timestamp": "2024-01-01T10:00:00Z",
    "metadata": {
      "pageId": "page_123",
      "conversationId": "conv_456"
    }
  },
  "routingContext": {
    "tenantId": "tenant_789",
    "priority": "NORMAL",
    "timeout": 30000
  }
}
```

**Response:**
```json
{
  "messageId": "msg_123",
  "routingDecision": {
    "destination": {
      "type": "BOTPRESS",
      "botId": "bot_789",
      "endpoint": "https://bot.example.com/webhook"
    },
    "confidence": 0.95,
    "reason": "Intent matched: customer_support",
    "processingTime": 150
  },
  "status": "ROUTED",
  "timestamp": "2024-01-01T10:00:00Z"
}
```

#### GET /routing/rules
Get message routing rules.

**Query Parameters:**
- `tenantId`: Filter by tenant (optional)
- `status`: Filter by status (optional)

**Response:**
```json
{
  "rules": [
    {
      "id": "rule_123",
      "tenantId": "tenant_789",
      "name": "Customer Support Routing",
      "priority": 1,
      "conditions": [
        {
          "field": "message.content",
          "operator": "CONTAINS",
          "value": "order",
          "caseSensitive": false
        },
        {
          "field": "message.channel",
          "operator": "EQUALS",
          "value": "FACEBOOK"
        }
      ],
      "actions": [
        {
          "type": "ROUTE_TO",
          "destination": {
            "type": "BOTPRESS",
            "botId": "support_bot"
          }
        }
      ],
      "status": "ACTIVE",
      "createdAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

#### POST /routing/rules
Create new routing rule.

**Request Body:**
```json
{
  "name": "Sales Lead Routing",
  "priority": 2,
  "conditions": [
    {
      "field": "message.content",
      "operator": "CONTAINS",
      "value": "pricing"
    }
  ],
  "actions": [
    {
      "type": "ROUTE_TO",
      "destination": {
        "type": "HUMAN_AGENT",
        "queueId": "sales_queue"
      }
    }
  ]
}
```

#### PUT /routing/rules/{ruleId}
Update routing rule.

#### DELETE /routing/rules/{ruleId}
Delete routing rule.

### Message Processing

#### POST /process
Process a message through the decision engine.

**Request Body:**
```json
{
  "message": {
    "id": "msg_456",
    "content": "What are your pricing plans?",
    "type": "TEXT",
    "sender": {
      "id": "user_456",
      "type": "CUSTOMER"
    },
    "channel": "WEBSITE",
    "timestamp": "2024-01-01T10:00:00Z"
  },
  "processingOptions": {
    "runIntentDetection": true,
    "runSentimentAnalysis": true,
    "runEntityExtraction": true,
    "language": "en"
  }
}
```

**Response:**
```json
{
  "messageId": "msg_456",
  "processingResult": {
    "intent": {
      "name": "pricing_inquiry",
      "confidence": 0.92
    },
    "sentiment": {
      "score": 0.1,
      "label": "NEUTRAL"
    },
    "entities": [
      {
        "type": "TOPIC",
        "value": "pricing",
        "confidence": 0.95
      }
    ],
    "language": "en",
    "processingTime": 250
  },
  "nextActions": [
    {
      "type": "SEND_RESPONSE",
      "template": "pricing_response"
    },
    {
      "type": "CREATE_LEAD",
      "priority": "HIGH"
    }
  ]
}
```

### Message Store

#### GET /conversations/{conversationId}/messages
Get messages in a conversation.

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 50)
- `fromDate`: Start date filter (optional)
- `toDate`: End date filter (optional)

**Response:**
```json
{
  "content": [
    {
      "id": "msg_123",
      "content": "Hello, I need help with my order",
      "type": "TEXT",
      "sender": {
        "id": "user_456",
        "type": "CUSTOMER",
        "name": "John Doe"
      },
      "recipient": {
        "id": "bot_789",
        "type": "BOT"
      },
      "channel": "FACEBOOK",
      "status": "DELIVERED",
      "timestamp": "2024-01-01T10:00:00Z",
      "metadata": {
        "pageId": "page_123",
        "messageId": "fb_msg_456"
      }
    }
  ],
  "page": 0,
  "size": 50,
  "totalElements": 25,
  "totalPages": 1
}
```

#### GET /conversations
Get user's conversations.

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `status`: Filter by status (ACTIVE, CLOSED, ARCHIVED)
- `channel`: Filter by channel (optional)

**Response:**
```json
{
  "content": [
    {
      "id": "conv_123",
      "participant": {
        "id": "user_456",
        "name": "John Doe",
        "email": "john@example.com"
      },
      "channel": "FACEBOOK",
      "status": "ACTIVE",
      "lastMessage": {
        "content": "Thank you for your help!",
        "timestamp": "2024-01-01T10:30:00Z"
      },
      "messageCount": 15,
      "createdAt": "2024-01-01T09:00:00Z",
      "updatedAt": "2024-01-01T10:30:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 5,
  "totalPages": 1
}
```

#### POST /conversations/{conversationId}/messages
Send a message to conversation.

**Request Body:**
```json
{
  "content": "I can help you with your order. What's your order number?",
  "type": "TEXT",
  "metadata": {
    "template": "order_inquiry"
  }
}
```

### Decision Engine

#### GET /decisions/{messageId}
Get decision details for a message.

**Response:**
```json
{
  "messageId": "msg_123",
  "decisions": [
    {
      "type": "ROUTING",
      "result": {
        "destination": "BOTPRESS",
        "confidence": 0.95,
        "reason": "Intent matched: customer_support"
      },
      "timestamp": "2024-01-01T10:00:01Z"
    },
    {
      "type": "PROCESSING",
      "result": {
        "intent": "customer_support",
        "sentiment": "NEUTRAL",
        "entities": ["order"]
      },
      "timestamp": "2024-01-01T10:00:02Z"
    }
  ],
  "finalDecision": {
    "action": "ROUTE_TO_BOT",
    "destination": "bot_789",
    "confidence": 0.95
  }
}
```

#### POST /decisions/evaluate
Evaluate routing decision manually.

**Request Body:**
```json
{
  "message": {
    "content": "I want to cancel my subscription",
    "channel": "WEBSITE"
  },
  "context": {
    "tenantId": "tenant_789",
    "userId": "user_456"
  }
}
```

**Response:**
```json
{
  "evaluation": {
    "routingDecision": {
      "destination": "HUMAN_AGENT",
      "queueId": "retention_queue",
      "confidence": 0.88
    },
    "reasoning": [
      "High-priority intent detected: subscription_cancellation",
      "Customer retention risk: HIGH",
      "Recommended escalation to human agent"
    ],
    "alternatives": [
      {
        "destination": "BOTPRESS",
        "botId": "retention_bot",
        "confidence": 0.75
      }
    ]
  }
}
```

### Analytics

#### GET /analytics/routing
Get routing analytics.

**Query Parameters:**
- `fromDate`: Start date (required)
- `toDate`: End date (required)
- `groupBy`: Group by period (HOUR, DAY, WEEK)
- `tenantId`: Filter by tenant (optional)

**Response:**
```json
{
  "period": "DAY",
  "data": [
    {
      "date": "2024-01-01",
      "totalMessages": 1500,
      "routingDecisions": {
        "BOTPRESS": 1200,
        "HUMAN_AGENT": 250,
        "EXTERNAL_API": 50
      },
      "averageProcessingTime": 180,
      "successRate": 0.98
    }
  ],
  "summary": {
    "totalMessages": 10500,
    "mostUsedDestination": "BOTPRESS",
    "averageConfidence": 0.87,
    "totalProcessingTime": 3150000
  }
}
```

#### GET /analytics/intents
Get intent analytics.

**Response:**
```json
{
  "period": "WEEK",
  "intents": [
    {
      "name": "customer_support",
      "count": 350,
      "percentage": 35.0,
      "trend": "UP"
    },
    {
      "name": "pricing_inquiry",
      "count": 200,
      "percentage": 20.0,
      "trend": "STABLE"
    }
  ]
}
```

## Error Responses

All endpoints return consistent error format:

```json
{
  "error": "ERROR_CODE",
  "message": "Human readable error message",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

Common error codes:
- `UNAUTHORIZED` (401): Invalid or missing token
- `FORBIDDEN` (403): Insufficient permissions
- `NOT_FOUND` (404): Message or conversation not found
- `VALIDATION_ERROR` (400): Invalid message format
- `ROUTING_FAILED` (500): Message routing failed
- `PROCESSING_ERROR` (500): Message processing failed
- `DECISION_TIMEOUT` (408): Decision engine timeout
- `RATE_LIMIT_EXCEEDED` (429): Too many requests

## gRPC Services

### MessageRouterService

- `RouteMessage`: Route message to destination
- `GetRoutingRules`: Get routing rules
- `CreateRoutingRule`: Create routing rule
- `UpdateRoutingRule`: Update routing rule
- `DeleteRoutingRule`: Delete routing rule

### DecisionEngineService

- `ProcessMessage`: Process message through decision engine
- `EvaluateDecision`: Evaluate routing decision
- `GetDecisionHistory`: Get decision history

### MessageStoreService

- `StoreMessage`: Store message
- `GetConversation`: Get conversation messages
- `GetUserConversations`: Get user conversations
- `SendMessage`: Send message to conversation

## Rate Limiting

API endpoints are rate-limited:
- Message routing: 1000 requests per minute
- Message processing: 500 requests per minute
- Conversation queries: 200 requests per minute
- Analytics queries: 100 requests per minute
- Rule management: 50 requests per minute

## Security

- Message content encrypted at rest
- PII detection and masking
- Audit trail for all routing decisions
- Content filtering and moderation
- GDPR compliance for message retention
- Secure message transmission
