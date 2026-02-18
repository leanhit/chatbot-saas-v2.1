# Penny Hub API Documentation

## Overview

The Penny Hub is the intelligent middleware layer that provides smart routing, context management, and analytics for the chatbot system. It acts as the central brain that orchestrates communication between different chatbot providers and manages conversation flow.

## Architecture

### Core Components

1. **PennyMiddlewareEngine** - Central processing engine
2. **ContextManager** - Conversation context and state management
3. **ProviderSelector** - Intelligent provider routing
4. **AnalyticsCollector** - Metrics and performance tracking
5. **ErrorHandler** - Centralized error handling
6. **CustomLogicEngine** - Business rule processing

### Provider Adapters

- **BotpressProviderAdapter** - Botpress integration
- **RasaProviderAdapter** - Rasa integration
- **ChatbotProviderWrapper** - Legacy provider wrapper

## API Endpoints

### Bot Management

#### Create Bot
```http
POST /api/penny/bots
Content-Type: application/json

{
  "botName": "Customer Service Bot",
  "botType": "BOTPRESS",
  "tenantId": 1,
  "ownerId": "user123",
  "botpressBotId": "bp-bot-123",
  "description": "Customer service assistant"
}
```

#### Get All Bots
```http
GET /api/penny/bots
Authorization: Bearer <token>
```

#### Get Bot by ID
```http
GET /api/penny/bots/{botId}
Authorization: Bearer <token>
```

#### Update Bot
```http
PUT /api/penny/bots/{botId}
Content-Type: application/json

{
  "botName": "Updated Bot Name",
  "description": "Updated description"
}
```

#### Enable/Disable Bot
```http
POST /api/penny/bots/{botId}/enable
POST /api/penny/bots/{botId}/disable
```

#### Delete Bot
```http
DELETE /api/penny/bots/{botId}
```

### Bot Selection

#### Get Available Bots
```http
GET /api/penny/bots/selection/available
```

#### Select Bot
```http
POST /api/penny/bots/selection/select
Content-Type: application/json

{
  "botId": "uuid-here",
  "provider": "BOTPRESS"
}
```

#### Get Selected Bot
```http
GET /api/penny/bots/selection/current
```

### Custom Rules

#### Create Rule
```http
POST /api/penny/rules/custom
Content-Type: application/json

{
  "name": "Greeting Rule",
  "description": "Handle greetings",
  "conditions": [
    {
      "type": "intent",
      "operator": "equals",
      "value": "greeting"
    }
  ],
  "actions": [
    {
      "type": "response",
      "value": "Hello! How can I help you today?"
    }
  ],
  "priority": 1,
  "enabled": true
}
```

#### Get All Rules
```http
GET /api/penny/rules/custom
```

#### Update Rule
```http
PUT /api/penny/rules/custom/{ruleId}
Content-Type: application/json

{
  "name": "Updated Rule",
  "enabled": false
}
```

#### Delete Rule
```http
DELETE /api/penny/rules/custom/{ruleId}
```

#### Test Rule
```http
POST /api/penny/rules/custom/{ruleId}/test
Content-Type: application/json

{
  "message": "Hello",
  "context": {}
}
```

### Response Templates

#### Create Template
```http
POST /api/penny/rules/templates
Content-Type: application/json

{
  "name": "Greeting Template",
  "content": "Hello {{name}}! Welcome to our service.",
  "variables": ["name"],
  "category": "greeting",
  "language": "en"
}
```

#### Get All Templates
```http
GET /api/penny/rules/templates
```

#### Update Template
```http
PUT /api/penny/rules/templates/{templateId}
Content-Type: application/json

{
  "content": "Updated template content"
}
```

#### Delete Template
```http
DELETE /api/penny/rules/templates/{templateId}
```

### Management APIs

#### Get Metrics
```http
GET /api/penny/management/metrics
Authorization: Bearer <admin-token>
```

#### Get Metrics for Time Range
```http
GET /api/penny/management/metrics/range?startTime=2024-01-01T00:00:00Z&endTime=2024-01-02T00:00:00Z
```

#### Get Provider Metrics
```http
GET /api/penny/management/metrics/provider/BOTPRESS
```

#### Get Bot Metrics
```http
GET /api/penny/management/metrics/bot/{botId}
```

#### Get Tenant Metrics
```http
GET /api/penny/management/metrics/tenant/{tenantId}
```

#### Export Metrics
```http
GET /api/penny/management/metrics/export?format=json
```

#### Reset Metrics
```http
POST /api/penny/management/metrics/reset
```

#### Get System Health
```http
GET /api/penny/management/health
```

#### Get Detailed Health
```http
GET /api/penny/management/health/detailed
```

#### Get Component Health
```http
GET /api/penny/management/health/component/{component}
```

#### Get Current Configuration
```http
GET /api/penny/management/configuration
```

#### Update Configuration
```http
PUT /api/penny/management/configuration/{component}
Content-Type: application/json

{
  "enabled": true,
  "maxConcurrentRequests": 100
}
```

#### Export Configuration
```http
GET /api/penny/management/configuration/export?format=json
```

#### Import Configuration
```http
POST /api/penny/management/configuration/import?format=json
Content-Type: application/json

{
  "analytics": {
    "enabled": true,
    "metrics": {
      "retentionDays": 30
    }
  }
}
```

#### Get System Overview
```http
GET /api/penny/management/overview
```

## Data Models

### PennyBot
```json
{
  "id": "uuid",
  "botName": "Customer Service Bot",
  "botType": "BOTPRESS",
  "tenantId": 1,
  "ownerId": "user123",
  "botpressBotId": "bp-bot-123",
  "description": "Customer service assistant",
  "isActive": true,
  "isEnabled": true,
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

### Custom Rule
```json
{
  "id": 1,
  "name": "Greeting Rule",
  "description": "Handle greetings",
  "conditions": [
    {
      "type": "intent",
      "operator": "equals",
      "value": "greeting"
    }
  ],
  "actions": [
    {
      "type": "response",
      "value": "Hello! How can I help you today?"
    }
  ],
  "priority": 1,
  "enabled": true,
  "tenantId": 1,
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

### Response Template
```json
{
  "id": 1,
  "name": "Greeting Template",
  "content": "Hello {{name}}! Welcome to our service.",
  "variables": ["name"],
  "category": "greeting",
  "language": "en",
  "tenantId": 1,
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

## Configuration

### Penny Properties
```yaml
penny:
  enabled: true
  max-concurrent-requests: 100
  timeout: 30s
  
  analytics:
    enabled: true
    metrics:
      enabled: true
      retention-days: 30
    events:
      enabled: true
      buffer-size: 1000
      
  context:
    cleanup:
      enabled: true
      interval: 1h
      max-age: 24h
    storage-type: redis
    ttl: 3600
    
  error:
    retry-attempts: 3
    retry-delay: 1s
    max-errors: 100
    circuit-breaker:
      enabled: true
      threshold: 10
```

## Error Handling

### Error Response Format
```json
{
  "success": false,
  "error": {
    "code": "BOT_NOT_FOUND",
    "message": "Bot not found",
    "details": "Bot with ID 'uuid' does not exist"
  },
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### Common Error Codes
- `BOT_NOT_FOUND` - Bot does not exist
- `RULE_NOT_FOUND` - Custom rule does not exist
- `TEMPLATE_NOT_FOUND` - Response template does not exist
- `VALIDATION_ERROR` - Input validation failed
- `PERMISSION_DENIED` - Insufficient permissions
- `INTERNAL_ERROR` - Server error

## Performance Metrics

### Key Metrics
- **Total Processed Messages** - Total messages processed
- **Error Rate** - Percentage of failed requests
- **Average Processing Time** - Average time per request
- **Provider Usage** - Usage count per provider
- **Intent Distribution** - Distribution of detected intents

### Health Indicators
- **Engine Status** - Overall engine health
- **Provider Health** - Individual provider status
- **Context Storage** - Context storage health
- **Memory Usage** - System memory consumption
- **CPU Usage** - System CPU consumption

## Security

### Authentication
- JWT tokens required for all endpoints
- Admin role required for management endpoints
- Tenant isolation enforced

### Authorization
- Role-based access control
- Tenant-level data isolation
- Resource-based permissions

## Monitoring

### Logging
- Structured logging with correlation IDs
- Performance metrics logging
- Error tracking and alerting

### Health Checks
- Component-level health monitoring
- Automatic failover mechanisms
- Circuit breaker patterns

## Integration

### Provider Integration
- Botpress API integration
- Rasa API integration
- Custom provider adapters

### External Systems
- Redis for context storage
- Database for persistence
- Analytics systems integration

## Best Practices

### Performance
- Use connection pooling
- Implement caching strategies
- Monitor resource usage
- Optimize database queries

### Security
- Validate all inputs
- Use HTTPS for all requests
- Implement rate limiting
- Regular security audits

### Reliability
- Implement retry mechanisms
- Use circuit breakers
- Monitor system health
- Plan for failover

## Troubleshooting

### Common Issues
1. **Bot Not Responding** - Check provider health and configuration
2. **High Error Rate** - Review error logs and provider status
3. **Slow Performance** - Check resource usage and optimize queries
4. **Context Loss** - Verify Redis connectivity and configuration

### Debug Mode
Enable debug logging for detailed troubleshooting:
```yaml
logging:
  level:
    com.chatbot.modules.penny: DEBUG
```

## Version History

### v1.0.0
- Initial release
- Core middleware engine
- Provider adapters
- Basic analytics
- Configuration management

### v1.1.0
- Enhanced metrics
- Health monitoring
- Configuration export/import
- Performance optimizations

### v1.2.0
- Advanced routing logic
- Custom rule engine
- Response templates
- Multi-tenant improvements
