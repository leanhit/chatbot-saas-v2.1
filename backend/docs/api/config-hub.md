# Config Hub API Documentation

## Overview
The Config Hub manages runtime configuration and environment settings for the chatbot SaaS platform.

## Base URL
```
http://localhost:8080/api/v1/config
```

## Authentication
All endpoints require JWT token in Authorization header:
```
Authorization: Bearer <jwt-token>
```

## Endpoints

### Runtime Configuration

#### GET /runtime
Get runtime configurations for current tenant.

**Query Parameters:**
- `scope`: Configuration scope (GLOBAL, TENANT, USER)
- `category`: Filter by category (optional)

**Response:**
```json
{
  "configurations": [
    {
      "id": "config_123",
      "key": "chatbot.max_conversation_duration",
      "value": "3600",
      "type": "INTEGER",
      "scope": "TENANT",
      "tenantId": "tenant_456",
      "category": "CHATBOT",
      "description": "Maximum conversation duration in seconds",
      "isEncrypted": false,
      "validation": {
        "min": 60,
        "max": 86400
      },
      "createdAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

#### GET /runtime/{configKey}
Get specific runtime configuration.

**Response:**
```json
{
  "id": "config_123",
  "key": "chatbot.max_conversation_duration",
  "value": "3600",
  "type": "INTEGER",
  "scope": "TENANT",
  "tenantId": "tenant_456",
  "category": "CHATBOT",
  "description": "Maximum conversation duration in seconds",
  "isEncrypted": false,
  "validation": {
    "min": 60,
    "max": 86400
  },
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

#### PUT /runtime/{configKey}
Update runtime configuration.

**Request Body:**
```json
{
  "value": "7200",
  "description": "Updated maximum conversation duration"
}
```

#### POST /runtime
Create new runtime configuration.

**Request Body:**
```json
{
  "key": "chatbot.welcome_message",
  "value": "Hello! How can I help you today?",
  "type": "STRING",
  "scope": "TENANT",
  "category": "CHATBOT",
  "description": "Welcome message for new conversations",
  "validation": {
    "maxLength": 500
  }
}
```

#### DELETE /runtime/{configKey}
Delete runtime configuration.

### Configuration Schema

#### GET /schema
Get configuration schema definitions.

**Response:**
```json
{
  "schemas": [
    {
      "category": "CHATBOT",
      "definitions": [
        {
          "key": "max_conversation_duration",
          "type": "INTEGER",
          "defaultValue": "3600",
          "description": "Maximum conversation duration in seconds",
          "validation": {
            "min": 60,
            "max": 86400
          },
          "scopes": ["GLOBAL", "TENANT"]
        },
        {
          "key": "welcome_message",
          "type": "STRING",
          "defaultValue": "Hello! How can I help you?",
          "description": "Welcome message for new conversations",
          "validation": {
            "maxLength": 500
          },
          "scopes": ["TENANT", "USER"]
        }
      ]
    }
  ]
}
```

#### GET /schema/{category}
Get schema definitions for specific category.

### Configuration Bulk Operations

#### POST /runtime/bulk
Bulk update runtime configurations.

**Request Body:**
```json
{
  "configurations": [
    {
      "key": "chatbot.max_conversation_duration",
      "value": "7200"
    },
    {
      "key": "chatbot.welcome_message",
      "value": "Welcome to our service!"
    }
  ]
}
```

#### POST /runtime/export
Export configurations.

**Request Body:**
```json
{
  "scope": "TENANT",
  "format": "JSON",
  "includeEncrypted": false
}
```

**Response:**
```json
{
  "exportId": "export_123",
  "downloadUrl": "https://example.com/exports/config-export.json",
  "expiresAt": "2024-01-01T12:00:00Z"
}
```

#### POST /runtime/import
Import configurations.

**Request:** multipart/form-data
- `file`: Configuration file
- `scope`: Import scope
- `overwrite`: Overwrite existing configurations

### Environment Configuration

#### GET /environment
Get environment configurations.

**Response:**
```json
{
  "environment": "production",
  "region": "us-east-1",
  "version": "2.1.0",
  "features": {
    "analytics": true,
    "multiLanguage": true,
    "fileUpload": true
  },
  "limits": {
    "maxFileSize": 10485760,
    "maxConcurrentUsers": 1000,
    "apiRateLimit": 1000
  },
  "services": {
    "database": {
      "host": "db.example.com",
      "port": 5432,
      "ssl": true
    },
    "redis": {
      "host": "redis.example.com",
      "port": 6379,
      "cluster": true
    }
  }
}
```

#### GET /environment/features
Get feature flags.

**Response:**
```json
{
  "features": [
    {
      "key": "analytics",
      "enabled": true,
      "description": "Enable analytics tracking",
      "rolloutPercentage": 100
    },
    {
      "key": "betaUI",
      "enabled": false,
      "description": "Enable beta user interface",
      "rolloutPercentage": 10
    }
  ]
}
```

#### POST /environment/features/{featureKey}/toggle
Toggle feature flag (admin only).

**Request Body:**
```json
{
  "enabled": true,
  "rolloutPercentage": 50
}
```

### Configuration History

#### GET /runtime/{configKey}/history
Get configuration change history.

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `fromDate`: Start date filter (optional)
- `toDate`: End date filter (optional)

**Response:**
```json
{
  "content": [
    {
      "id": "history_123",
      "configKey": "chatbot.max_conversation_duration",
      "oldValue": "3600",
      "newValue": "7200",
      "changedBy": "user_456",
      "reason": "Increase timeout for better user experience",
      "timestamp": "2024-01-01T10:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 5,
  "totalPages": 1
}
```

### Configuration Validation

#### POST /runtime/validate
Validate configuration values.

**Request Body:**
```json
{
  "configurations": [
    {
      "key": "chatbot.max_conversation_duration",
      "value": "7200"
    },
    {
      "key": "chatbot.welcome_message",
      "value": "This message is way too long and exceeds the maximum length limit of 500 characters"
    }
  ]
}
```

**Response:**
```json
{
  "valid": false,
  "errors": [
    {
      "key": "chatbot.welcome_message",
      "error": "Value exceeds maximum length of 500 characters",
      "currentLength": 120,
      "maxLength": 500
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
- `NOT_FOUND` (404): Configuration not found
- `VALIDATION_ERROR` (400): Invalid configuration value
- `CONFIGURATION_LOCKED` (423): Configuration is locked
- `INVALID_SCOPE` (400): Invalid configuration scope
- `ENCRYPTION_ERROR` (500): Encryption/decryption failed
- `IMPORT_ERROR` (400): Configuration import failed

## gRPC Services

### RuntimeConfigService

- `GetConfig`: Get configuration value
- `SetConfig`: Set configuration value
- `DeleteConfig`: Delete configuration
- `ValidateConfig`: Validate configuration
- `GetConfigHistory`: Get configuration history

### EnvironmentConfigService

- `GetEnvironment`: Get environment configuration
- `GetFeatureFlag`: Get feature flag status
- `SetFeatureFlag`: Set feature flag status
- `GetServiceConfig`: Get service configuration

## Rate Limiting

API endpoints are rate-limited:
- Configuration reads: 500 requests per minute
- Configuration writes: 100 requests per minute
- Bulk operations: 20 requests per minute
- Export/Import: 10 requests per minute
- History queries: 50 requests per minute

## Security

- Encrypted configurations stored with AES-256
- Audit trail for all configuration changes
- Role-based access control for sensitive configs
- Configuration validation before applying changes
- Backup and restore capabilities
- Environment isolation for configurations
