# App Hub API Documentation

## Overview
The App Hub manages application registry, subscriptions, and access control for the chatbot SaaS platform.

## Base URL
```
http://localhost:8080/api/v1/apps
```

## Authentication
All endpoints require JWT token in Authorization header:
```
Authorization: Bearer <jwt-token>
```

## Endpoints

### App Registry

#### GET /registry
Get list of available applications.

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `category`: Filter by category (optional)
- `status`: Filter by status (optional)

**Response:**
```json
{
  "content": [
    {
      "id": "app_123",
      "name": "Facebook Messenger",
      "description": "Connect your Facebook Messenger",
      "category": "SOCIAL",
      "type": "INTEGRATION",
      "status": "ACTIVE",
      "version": "2.1.0",
      "developer": "ChatBot Team",
      "icon": "https://example.com/facebook-icon.png",
      "screenshots": [
        "https://example.com/screen1.png"
      ],
      "features": ["WEBHOOK_SUPPORT", "AUTO_REPLY"],
      "pricing": {
        "type": "FREE",
        "trialDays": 0
      },
      "createdAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-01-01T00:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 15,
  "totalPages": 1
}
```

#### GET /registry/{appId}
Get application details by ID.

**Response:**
```json
{
  "id": "app_123",
  "name": "Facebook Messenger",
  "description": "Connect your Facebook Messenger for customer support",
  "category": "SOCIAL",
  "type": "INTEGRATION",
  "status": "ACTIVE",
  "version": "2.1.0",
  "developer": "ChatBot Team",
  "icon": "https://example.com/facebook-icon.png",
  "screenshots": [
    "https://example.com/screen1.png",
    "https://example.com/screen2.png"
  ],
  "features": [
    "WEBHOOK_SUPPORT",
    "AUTO_REPLY",
    "MESSAGE_TEMPLATES",
    "ANALYTICS"
  ],
  "requirements": {
    "minTenantPlan": "STARTER",
    "requiredPermissions": ["MESSAGING_READ", "MESSAGING_WRITE"]
  },
  "pricing": {
    "type": "FREEMIUM",
    "trialDays": 14,
    "plans": [
      {
        "name": "FREE",
        "price": 0,
        "limits": {
          "messagesPerMonth": 100,
          "conversations": 10
        }
      },
      {
        "name": "PRO",
        "price": 29.99,
        "limits": {
          "messagesPerMonth": 10000,
          "conversations": 1000
        }
      }
    ]
  },
  "configuration": {
    "fields": [
      {
        "name": "PAGE_ACCESS_TOKEN",
        "type": "SECRET",
        "required": true,
        "description": "Facebook Page Access Token"
      },
      {
        "name": "WEBHOOK_SECRET",
        "type": "SECRET",
        "required": true,
        "description": "Webhook verification secret"
      }
    ]
  },
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

### App Subscriptions

#### GET /subscriptions
Get current tenant's app subscriptions.

**Query Parameters:**
- `status`: Filter by status (optional)
- `category`: Filter by category (optional)

**Response:**
```json
{
  "subscriptions": [
    {
      "id": "sub_123",
      "appId": "app_123",
      "tenantId": "tenant_456",
      "status": "ACTIVE",
      "plan": "PRO",
      "configuration": {
        "PAGE_ACCESS_TOKEN": "masked_token",
        "WEBHOOK_SECRET": "masked_secret"
      },
      "usage": {
        "messagesThisMonth": 1500,
        "conversationsThisMonth": 150,
        "lastUsedAt": "2024-01-01T10:00:00Z"
      },
      "billing": {
        "nextBillingDate": "2024-02-01T00:00:00Z",
        "amount": 29.99,
        "currency": "USD"
      },
      "subscribedAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

#### POST /subscriptions
Subscribe to an application.

**Request Body:**
```json
{
  "appId": "app_123",
  "plan": "PRO",
  "configuration": {
    "PAGE_ACCESS_TOKEN": "EAAD...",
    "WEBHOOK_SECRET": "my_secret_123"
  }
}
```

**Response:**
```json
{
  "id": "sub_456",
  "appId": "app_123",
  "tenantId": "tenant_456",
  "status": "ACTIVE",
  "plan": "PRO",
  "configuration": {
    "PAGE_ACCESS_TOKEN": "masked_token",
    "WEBHOOK_SECRET": "masked_secret"
  },
  "subscribedAt": "2024-01-01T00:00:00Z"
}
```

#### PUT /subscriptions/{subscriptionId}
Update app subscription configuration.

**Request Body:**
```json
{
  "plan": "FREE",
  "configuration": {
    "PAGE_ACCESS_TOKEN": "EAAD_new_token"
  }
}
```

#### DELETE /subscriptions/{subscriptionId}
Unsubscribe from an application.

#### POST /subscriptions/{subscriptionId}/cancel
Cancel subscription (graceful cancellation).

### App Guard

#### GET /guards
Get app guard rules for current tenant.

**Response:**
```json
{
  "guards": [
    {
      "id": "guard_123",
      "appId": "app_123",
      "tenantId": "tenant_456",
      "type": "RATE_LIMIT",
      "status": "ACTIVE",
      "rules": [
        {
          "name": "MESSAGE_RATE_LIMIT",
          "condition": "messages_per_minute > 100",
          "action": "BLOCK",
          "priority": 1
        }
      ],
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

#### POST /guards
Create app guard rule.

**Request Body:**
```json
{
  "appId": "app_123",
  "type": "CONTENT_FILTER",
  "rules": [
    {
      "name": "PROFANITY_FILTER",
      "condition": "message contains profanity",
      "action": "FLAG",
      "priority": 1
    }
  ]
}
```

#### PUT /guards/{guardId}
Update guard rule.

#### DELETE /guards/{guardId}
Delete guard rule.

### App Configuration

#### GET /subscriptions/{subscriptionId}/config
Get app configuration schema.

**Response:**
```json
{
  "appId": "app_123",
  "schema": {
    "fields": [
      {
        "name": "PAGE_ACCESS_TOKEN",
        "type": "SECRET",
        "required": true,
        "label": "Page Access Token",
        "description": "Facebook Page Access Token",
        "validation": {
          "pattern": "^EAAD.*",
          "minLength": 50
        }
      }
    ]
  },
  "current": {
    "PAGE_ACCESS_TOKEN": "masked_token"
  }
}
```

#### PUT /subscriptions/{subscriptionId}/config
Update app configuration.

**Request Body:**
```json
{
  "PAGE_ACCESS_TOKEN": "EAAD_new_token",
  "ENABLE_AUTO_REPLY": true
}
```

### App Analytics

#### GET /subscriptions/{subscriptionId}/analytics
Get app usage analytics.

**Query Parameters:**
- `fromDate`: Start date (required)
- `toDate`: End date (required)
- `groupBy`: Group by period (DAY, WEEK, MONTH)

**Response:**
```json
{
  "period": "DAY",
  "data": [
    {
      "date": "2024-01-01",
      "usage": {
        "messages": 150,
        "conversations": 15,
        "errors": 2
      },
      "cost": 0.50
    }
  ],
  "summary": {
    "totalMessages": 4500,
    "totalConversations": 450,
    "totalErrors": 5,
    "totalCost": 15.00
  }
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
- `NOT_FOUND` (404): App or subscription not found
- `VALIDATION_ERROR` (400): Invalid configuration
- `ALREADY_SUBSCRIBED` (409): Already subscribed to app
- `SUBSCRIPTION_LIMIT_EXCEEDED` (429): Subscription limit reached
- `INCOMPATIBLE_PLAN` (422): App requires higher tenant plan
- `CONFIGURATION_ERROR` (422): Invalid app configuration

## gRPC Services

### AppRegistryService

- `GetAppById`: Get app by ID
- `ListApps`: List available apps
- `ValidateAppCompatibility`: Check if app is compatible with tenant
- `GetAppConfigurationSchema`: Get app configuration schema

### AppSubscriptionService

- `SubscribeToApp`: Subscribe tenant to app
- `UnsubscribeFromApp`: Unsubscribe from app
- `UpdateSubscription`: Update subscription
- `ValidateSubscription`: Check if subscription is active

### AppGuardService

- `CreateGuard`: Create guard rule
- `EvaluateGuard`: Evaluate guard rules
- `UpdateGuard`: Update guard rule
- `DeleteGuard`: Delete guard rule

## Rate Limiting

API endpoints are rate-limited:
- App registry: 200 requests per minute
- Subscription management: 100 requests per minute
- Guard management: 50 requests per minute
- Configuration updates: 30 requests per minute
- Analytics: 50 requests per minute

## Security

- All app configurations are encrypted at rest
- Secret values are masked in responses
- App isolation enforced per tenant
- Audit logs for all subscription changes
- Guard rules evaluated in real-time
