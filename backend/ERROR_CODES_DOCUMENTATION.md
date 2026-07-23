# Error Codes Documentation

This document provides comprehensive documentation for all error codes used in the Chatbot SaaS backend API.

## Table of Contents

- [Common Errors](#common-errors)
- [Authentication Errors](#authentication-errors)
- [User Errors](#user-errors)
- [Tenant Errors](#tenant-errors)
- [License Errors](#license-errors)
- [Payment Errors](#payment-errors)
- [Config Errors](#config-errors)
- [Conversation Errors](#conversation-errors)
- [Resource Errors](#resource-errors)
- [File Validation Errors](#file-validation-errors)
- [Agent Validation Errors](#agent-validation-errors)
- [Webhook Validation Errors](#webhook-validation-errors)
- [Package Validation Errors](#package-validation-errors)
- [Discount Validation Errors](#discount-validation-errors)
- [Escalation Errors](#escalation-errors)
- [SLA Errors](#sla-errors)
- [Routing Rule Errors](#routing-rule-errors)
- [Environment Config Errors](#environment-config-errors)
- [Notification Errors](#notification-errors)
- [Password Validation Errors](#password-validation-errors)
- [RSA Key Validation Errors](#rsa-key-validation-errors)
- [Insufficient Balance Errors](#insufficient-balance-errors)

---

## Common Errors

### NOT_FOUND
- **Code**: `NOT_FOUND`
- **HTTP Status**: 404
- **Description**: Generic resource not found error
- **Usage**: When a requested resource cannot be found
- **Example Response**:
```json
{
  "status": "error",
  "code": "NOT_FOUND",
  "message": "Resource not found: 123"
}
```

### VALIDATION_ERROR
- **Code**: `VALIDATION_ERROR`
- **HTTP Status**: 400
- **Description**: Generic validation error
- **Usage**: When request validation fails
- **Example Response**:
```json
{
  "status": "error",
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "errors": ["Field 'name' is required"]
}
```

### UNAUTHORIZED
- **Code**: `UNAUTHORIZED`
- **HTTP Status**: 401
- **Description**: User is not authenticated
- **Usage**: When authentication is required but not provided
- **Example Response**:
```json
{
  "status": "error",
  "code": "UNAUTHORIZED",
  "message": "Authentication required"
}
```

### FORBIDDEN
- **Code**: `FORBIDDEN`
- **HTTP Status**: 403
- **Description**: User is authenticated but lacks permission
- **Usage**: When user doesn't have permission to access resource
- **Example Response**:
```json
{
  "status": "error",
  "code": "FORBIDDEN",
  "message": "Access denied"
}
```

### ACCESS_DENIED
- **Code**: `ACCESS_DENIED`
- **HTTP Status**: 403
- **Description**: Access denied to resource
- **Usage**: Similar to FORBIDDEN, for specific access control scenarios
- **Example Response**:
```json
{
  "status": "error",
  "code": "ACCESS_DENIED",
  "message": "You do not have permission to access this resource"
}
```

### BAD_REQUEST
- **Code**: `BAD_REQUEST`
- **HTTP Status**: 400
- **Description**: Invalid request
- **Usage**: When request format or data is invalid
- **Example Response**:
```json
{
  "status": "error",
  "code": "BAD_REQUEST",
  "message": "Invalid request format"
}
```

### CONFLICT
- **Code**: `CONFLICT`
- **HTTP Status**: 409
- **Description**: Resource conflict
- **Usage**: When request conflicts with existing state
- **Example Response**:
```json
{
  "status": "error",
  "code": "CONFLICT",
  "message": "Resource already exists"
}
```

### RESOURCE_ALREADY_EXISTS
- **Code**: `RESOURCE_ALREADY_EXISTS`
- **HTTP Status**: 409
- **Description**: Resource already exists
- **Usage**: When trying to create a resource that already exists
- **Example Response**:
```json
{
  "status": "error",
  "code": "RESOURCE_ALREADY_EXISTS",
  "message": "Resource with ID 'abc123' already exists"
}
```

### INTERNAL_ERROR
- **Code**: `INTERNAL_ERROR`
- **HTTP Status**: 500
- **Description**: Internal server error
- **Usage**: When an unexpected server error occurs
- **Example Response**:
```json
{
  "status": "error",
  "code": "INTERNAL_ERROR",
  "message": "An internal error occurred"
}
```

### SERVICE_UNAVAILABLE
- **Code**: `SERVICE_UNAVAILABLE`
- **HTTP Status**: 503
- **Description**: Service temporarily unavailable
- **Usage**: When service is down for maintenance
- **Example Response**:
```json
{
  "status": "error",
  "code": "SERVICE_UNAVAILABLE",
  "message": "Service is temporarily unavailable"
}
```

### TIMEOUT
- **Code**: `TIMEOUT`
- **HTTP Status**: 504
- **Description**: Request timeout
- **Usage**: When request takes too long to process
- **Example Response**:
```json
{
  "status": "error",
  "code": "TIMEOUT",
  "message": "Request timeout"
}
```

### RATE_LIMIT_EXCEEDED
- **Code**: `RATE_LIMIT_EXCEEDED`
- **HTTP Status**: 429
- **Description**: Rate limit exceeded
- **Usage**: When API rate limit is exceeded
- **Example Response**:
```json
{
  "status": "error",
  "code": "RATE_LIMIT_EXCEEDED",
  "message": "Rate limit exceeded. Please try again later."
}
```

### INTEGRATION_ERROR
- **Code**: `INTEGRATION_ERROR`
- **HTTP Status**: 502
- **Description**: Integration error with external service
- **Usage**: When external service integration fails
- **Example Response**:
```json
{
  "status": "error",
  "code": "INTEGRATION_ERROR",
  "message": "Failed to communicate with external service"
}
```

### PAYLOAD_TOO_LARGE
- **Code**: `PAYLOAD_TOO_LARGE`
- **HTTP Status**: 413
- **Description**: Request payload too large
- **Usage**: When request body exceeds size limit
- **Example Response**:
```json
{
  "status": "error",
  "code": "PAYLOAD_TOO_LARGE",
  "message": "Request payload too large. Maximum size is 10MB."
}
```

---

## Authentication Errors

### BAD_CREDENTIALS
- **Code**: `BAD_CREDENTIALS`
- **HTTP Status**: 401
- **Description**: Invalid username or password
- **Usage**: When login credentials are incorrect
- **Example Response**:
```json
{
  "status": "error",
  "code": "BAD_CREDENTIALS",
  "message": "Invalid username or password"
}
```

### AUTHENTICATION_FAILED
- **Code**: `AUTHENTICATION_FAILED`
- **HTTP Status**: 401
- **Description**: Authentication failed
- **Usage**: When authentication process fails
- **Example Response**:
```json
{
  "status": "error",
  "code": "AUTHENTICATION_FAILED",
  "message": "Authentication failed"
}
```

### INVALID_TOKEN
- **Code**: `INVALID_TOKEN`
- **HTTP Status**: 401
- **Description**: Invalid authentication token
- **Usage**: When JWT token is invalid or expired
- **Example Response**:
```json
{
  "status": "error",
  "code": "INVALID_TOKEN",
  "message": "Invalid or expired token"
}
```

---

## User Errors

### EMAIL_ALREADY_EXISTS
- **Code**: `EMAIL_ALREADY_EXISTS`
- **HTTP Status**: 409
- **Description**: Email already registered
- **Usage**: When trying to register with existing email
- **Example Response**:
```json
{
  "status": "error",
  "code": "EMAIL_ALREADY_EXISTS",
  "message": "Email 'user@example.com' is already registered"
}
```

### USER_NOT_FOUND
- **Code**: `USER_NOT_FOUND`
- **HTTP Status**: 404
- **Description**: User not found
- **Usage**: When requested user does not exist
- **Example Response**:
```json
{
  "status": "error",
  "code": "USER_NOT_FOUND",
  "message": "User not found: 123"
}
```

---

## Tenant Errors

### TENANT_NOT_FOUND
- **Code**: `TENANT_NOT_FOUND`
- **HTTP Status**: 404
- **Description**: Tenant not found
- **Usage**: When requested tenant does not exist
- **Example Response**:
```json
{
  "status": "error",
  "code": "TENANT_NOT_FOUND",
  "message": "Tenant not found: 'tenant-key'"
}
```

### INVALID_TENANT_KEY
- **Code**: `INVALID_TENANT_KEY`
- **HTTP Status**: 400
- **Description**: Invalid tenant key format
- **Usage**: When tenant key format is invalid
- **Example Response**:
```json
{
  "status": "error",
  "code": "INVALID_TENANT_KEY",
  "message": "Invalid tenant key format"
}
```

### INSUFFICIENT_PERMISSION
- **Code**: `INSUFFICIENT_PERMISSION`
- **HTTP Status**: 403
- **Description**: Insufficient permissions
- **Usage**: When user lacks required permissions
- **Example Response**:
```json
{
  "status": "error",
  "code": "INSUFFICIENT_PERMISSION",
  "message": "You do not have permission to perform this action"
}
```

### TENANT_INACTIVE
- **Code**: `TENANT_INACTIVE`
- **HTTP Status**: 403
- **Description**: Tenant account is inactive
- **Usage**: When trying to access an inactive tenant
- **Example Response**:
```json
{
  "status": "error",
  "code": "TENANT_INACTIVE",
  "message": "Tenant account已被暂停"
}
```

### TENANT_CONTEXT_MISSING
- **Code**: `TENANT_CONTEXT_MISSING`
- **HTTP Status**: 400
- **Description**: Tenant context not found
- **Usage**: When tenant ID is missing from request context
- **Example Response**:
```json
{
  "status": "error",
  "code": "TENANT_CONTEXT_MISSING",
  "message": "Tenant ID not found in context"
}
```

---

## License Errors

### LICENSE_NOT_FOUND
- **Code**: `LICENSE_NOT_FOUND`
- **HTTP Status**: 404
- **Description**: License not found
- **Usage**: When requested license does not exist
- **Example Response**:
```json
{
  "status": "error",
  "code": "LICENSE_NOT_FOUND",
  "message": "License not found"
}
```

### LICENSE_EXPIRED
- **Code**: `LICENSE_EXPIRED`
- **HTTP Status**: 403
- **Description**: License has expired
- **Usage**: When trying to use an expired license
- **Example Response**:
```json
{
  "status": "error",
  "code": "LICENSE_EXPIRED",
  "message": "License expired on 2024-01-01"
}
```

### LICENSE_INACTIVE
- **Code**: `LICENSE_INACTIVE`
- **HTTP Status**: 403
- **Description**: License is inactive
- **Usage**: When trying to use an inactive license
- **Example Response**:
```json
{
  "status": "error",
  "code": "LICENSE_INACTIVE",
  "message": "License is inactive"
}
```

---

## Payment Errors

### PAYMENT_ERROR
- **Code**: `PAYMENT_ERROR`
- **HTTP Status**: 400
- **Description**: Generic payment error
- **Usage**: When payment processing fails
- **Example Response**:
```json
{
  "status": "error",
  "code": "PAYMENT_ERROR",
  "message": "Payment processing failed"
}
```

### PAYMENT_NOT_FOUND
- **Code**: `PAYMENT_NOT_FOUND`
- **HTTP Status**: 404
- **Description**: Payment not found
- **Usage**: When requested payment does not exist
- **Example Response**:
```json
{
  "status": "error",
  "code": "PAYMENT_NOT_FOUND",
  "message": "Payment not found: REF123456"
}
```

### PAYMENT_EXPIRED
- **Code**: `PAYMENT_EXPIRED`
- **HTTP Status**: 400
- **Description**: Payment has expired
- **Usage**: When trying to use an expired payment
- **Example Response**:
```json
{
  "status": "error",
  "code": "PAYMENT_EXPIRED",
  "message": "Payment expired"
}
```

### INVALID_PAYMENT_AMOUNT
- **Code**: `INVALID_PAYMENT_AMOUNT`
- **HTTP Status**: 400
- **Description**: Invalid payment amount
- **Usage**: When payment amount is invalid
- **Example Response**:
```json
{
  "status": "error",
  "code": "INVALID_PAYMENT_AMOUNT",
  "message": "Payment amount must be greater than 0"
}
```

### BANK_API_ERROR
- **Code**: `BANK_API_ERROR`
- **HTTP Status**: 502
- **Description**: Bank API error
- **Usage**: When bank API integration fails
- **Example Response**:
```json
{
  "status": "error",
  "code": "BANK_API_ERROR",
  "message": "Bank API error: Connection timeout"
}
```

---

## File Validation Errors

### INVALID_FILE_TYPE
- **Code**: `INVALID_FILE_TYPE`
- **HTTP Status**: 400
- **Description**: Invalid file type
- **Usage**: When uploaded file type is not allowed
- **Example Response**:
```json
{
  "status": "error",
  "code": "INVALID_FILE_TYPE",
  "message": "Invalid file type. Allowed types: image/*"
}
```

### FILE_TOO_LARGE
- **Code**: `FILE_TOO_LARGE`
- **HTTP Status**: 413
- **Description**: File size exceeds limit
- **Usage**: When uploaded file is too large
- **Example Response**:
```json
{
  "status": "error",
  "code": "FILE_TOO_LARGE",
  "message": "File size cannot exceed 10485760 bytes"
}
```

### FILE_EMPTY
- **Code**: `FILE_EMPTY`
- **HTTP Status**: 400
- **Description**: File is empty
- **Usage**: When uploaded file has no content
- **Example Response**:
```json
{
  "status": "error",
  "code": "FILE_EMPTY",
  "message": "File cannot be empty"
}
```

### FILE_NULL
- **Code**: `FILE_NULL`
- **HTTP Status**: 400
- **Description**: File is null
- **Usage**: When file parameter is null
- **Example Response**:
```json
{
  "status": "error",
  "code": "FILE_NULL",
  "message": "File cannot be null"
}
```

---

## Agent Validation Errors

### INVALID_AGENT_NAME
- **Code**: `INVALID_AGENT_NAME`
- **HTTP Status**: 400
- **Description**: Invalid agent name
- **Usage**: When agent name is empty or invalid
- **Example Response**:
```json
{
  "status": "error",
  "code": "INVALID_AGENT_NAME",
  "message": "Agent name cannot be empty"
}
```

### INVALID_AGENT_EMAIL
- **Code**: `INVALID_AGENT_EMAIL`
- **HTTP Status**: 400
- **Description**: Invalid agent email
- **Usage**: When agent email is empty or invalid format
- **Example Response**:
```json
{
  "status": "error",
  "code": "INVALID_AGENT_EMAIL",
  "message": "Invalid email format: agent@example"
}
```

### INVALID_AGENT_ROLE
- **Code**: `INVALID_AGENT_ROLE`
- **HTTP Status**: 400
- **Description**: Invalid agent role
- **Usage**: When agent role is null or invalid
- **Example Response**:
```json
{
  "status": "error",
  "code": "INVALID_AGENT_ROLE",
  "message": "Agent role cannot be null"
}
```

### INVALID_AGENT_MAX_CONCURRENT
- **Code**: `INVALID_AGENT_MAX_CONCURRENT`
- **HTTP Status**: 400
- **Description**: Invalid max concurrent conversations
- **Usage**: When max concurrent is invalid
- **Example Response**:
```json
{
  "status": "error",
  "code": "INVALID_AGENT_MAX_CONCURRENT",
  "message": "Max concurrent conversations must be greater than 0, got: 0"
}
```

### INVALID_AGENT_CURRENT_LOAD
- **Code**: `INVALID_AGENT_CURRENT_LOAD`
- **HTTP Status**: 400
- **Description**: Invalid current load
- **Usage**: When current load is invalid
- **Example Response**:
```json
{
  "status": "error",
  "code": "INVALID_AGENT_CURRENT_LOAD",
  "message": "Current load cannot be negative or exceed max concurrent. Current: -1, Max: 10"
}
```

### AGENT_TENANT_ID_REQUIRED
- **Code**: `AGENT_TENANT_ID_REQUIRED`
- **HTTP Status**: 400
- **Description**: Tenant ID required
- **Usage**: When agent tenant ID is null
- **Example Response**:
```json
{
  "status": "error",
  "code": "AGENT_TENANT_ID_REQUIRED",
  "message": "Tenant ID cannot be null"
}
```

---

## Webhook Validation Errors

### WEBHOOK_URL_EXISTS
- **Code**: `WEBHOOK_URL_EXISTS`
- **HTTP Status**: 409
- **Description**: Webhook URL already exists
- **Usage**: When webhook URL is already registered
- **Example Response**:
```json
{
  "status": "error",
  "code": "WEBHOOK_URL_EXISTS",
  "message": "Webhook URL already exists: https://example.com/webhook"
}
```

### INVALID_WEBHOOK_URL
- **Code**: `INVALID_WEBHOOK_URL`
- **HTTP Status**: 400
- **Description**:.Invalid webhook URL
- **Usage**: When webhook URL format is invalid
- **Example Response**:
```json
{
  "status": "error",
  "code": "INVALID_WEBHOOK_URL",
  "message": "Invalid webhook URL: not-a-url"
}
```

### WEBHOOK_SIGNATURE_ERROR
- **Code**: `WEBHOOK_SIGNATURE_ERROR`
- **HTTP Status**: 400
- **Description**: Webhook signature error
- **Usage**: When webhook signature verification fails
- **Example Response**:
```json
{
  "status": "error",
  "code": "WEBHOOK_SIGNATURE_ERROR",
  "message": "Webhook signature error: Signature mismatch"
}
```

### WEBHOOK_TEST_FAILED
- **Code**: `WEBHOOK_TEST_FAILED`
- **HTTP Status**: 400
- **Description**: Webhook test failed
- **Usage**: When webhook test endpoint fails
- **Example Response**:
```json
{
  "status": "error",
  "code": "WEBHOOK_TEST_FAILED",
  "message": "Webhook test failed for MyWebhook: Returned status: 500"
}
```

---

## Package Validation Errors

### PACKAGE_ID_EXISTS
- **Code**: `PACKAGE_ID_EXISTS`
- **HTTP Status**: 409
- **Description**: Package ID already exists
- **Usage**: When package ID is already registered
- **Example Response**:
```json
{
  "status": "error",
  "code": "PACKAGE_ID_EXISTS",
  "message": "Package ID already exists: BASIC_PLAN"
}
```

### PACKAGE_NOT_ACTIVE
- **Code**: `PACKAGE_NOT_ACTIVE`
- **HTTP Status**: 400
- **Description**: Package is not active
- **Usage**: When trying to use an inactive package
- **Example Response**:
```json
{
  "status": "error",
  "code": "PACKAGE_NOT_ACTIVE",
  "message": "Package is not active: BASIC_PLAN"
}
```

---

## Discount Validation Errors

### DISCOUNT_CODE_EXISTS
- **Code**: `DISCOUNT_CODE_EXISTS`
- **HTTP Status**: 409
- **Description**: Discount code already exists
- **Usage**: When discount code is already registered
- **Example Response**:
```json
{
  "status": "error",
  "code": "DISCOUNT_CODE_EXISTS",
  "message": "Discount code already exists: SUMMER2024"
}
```

---

## Escalation Errors

### ESCALATION_TIER_NOT_FOUND
- **Code**: `ESCALATION_TIER_NOT_FOUND`
- **HTTP Status**: 404
- **Description**: Escalation tier not found
- **Usage**: When requested escalation tier does not exist
- **Example Response**:
```json
{
  "status": "error",
  "code": "ESCALATION_TIER_NOT_FOUND",
  "message": "Escalation tier not found: 123"
}
```

---

## SLA Errors

### SLA_CONFIG_NOT_FOUND
- **Code**: `SLA_CONFIG_NOT_FOUND`
- **HTTP Status**: 404
- **Description**: SLA configuration not found
- **Usage**: When requested SLA configuration does not exist
- **Example Response**:
```json
{
  "status": "error",
  "code": "SLA_CONFIG_NOT_FOUND",
  "message": "SLA configuration not found: 456"
}
```

---

## Routing Rule Errors

### ROUTING_RULE_NOT_FOUND
- **Code**: `ROUTING_RULE_NOT_FOUND`
- **HTTP Status**: 404
- **Description**: Routing rule not found
- **Usage**: When requested routing rule does not exist
- **Example Response**:
```json
{
  "status": "error",
  "code": "ROUTING_RULE_NOT_FOUND",
  "message": "Routing rule not found: 789"
}
```

---

## Environment Config Errors

### ENVIRONMENT_CONFIG_NOT_FOUND
- **Code**: `ENVIRONMENT_CONFIG_NOT_FOUND`
- **HTTP Status**: 404
- **Description**: Environment config not found
- **Usage**: When requested environment config does not exist
- **Example Response**:
```json
{
  "status": "error",
  "code": "ENVIRONMENT_CONFIG_NOT_FOUND",
  "message": "Environment config not found"
}
```

---

## Notification Errors

### NOTIFICATION_SEND_FAILED
- **Code**: `NOTIFICATION_SEND_FAILED`
- **HTTP Status**: 500
- **Description**: Failed to send notification
- **Usage**: When notification sending fails
- **Example Response**:
```json
{
  "status": "error",
  "code": "NOTIFICATION_SEND_FAILED",
  "message": "Failed to send Slack notification"
}
```

---

## Password Validation Errors

### PASSWORD_CONFIRMATION_MISMATCH
- **Code**: `PASSWORD_CONFIRMATION_MISMATCH`
- **HTTP Status**: 400
- **Description**: Password confirmation does not match
- **Usage**: When password confirmation doesn't match
- **Example Response**:
```json
{
  "status": "error",
  "code": "PASSWORD_CONFIRMATION_MISMATCH",
  "message": "New password confirmation does not match"
}
```

---

## RSA Key Validation Errors

### RSA_PRIVATE_KEY_REQUIRED
- **Code**: `RSA_PRIVATE_KEY_REQUIRED`
- **HTTP Status**: 400
- **Description**: RSA private key required
- **Usage**: When RSA private key is missing for RS256 algorithm
- **Example Response**:
```json
{
  "status": "error",
  "code": "RSA_PRIVATE_KEY_REQUIRED",
  "message": "RSA private key is required for RS256 algorithm"
}
```

### RSA_PUBLIC_KEY_REQUIRED
- **Code**: `RSA_PUBLIC_KEY_REQUIRED`
- **HTTP Status**: 400
- **Description**: RSA public key required
- **Usage**: When RSA public key is missing for RS256 algorithm
- **Example Response**:
```json
{
  "status": "error",
  "code": "RSA_PUBLIC_KEY_REQUIRED",
  "message": "RSA public key is required for RS256 algorithm"
}
```

---

## Insufficient Balance Errors

### INSUFFICIENT_BALANCE
- **Code**: `INSUFFICIENT_BALANCE`
- **HTTP Status**: 400
- **Description**: Insufficient balance
- **Usage**: When user balance is insufficient for transaction
- **Example Response**:
```json
{
  "status": "error",
  "code": "INSUFFICIENT_BALANCE",
  "message": "Insufficient balance. Required: 1000000, Available: 500000"
}
```

---

## Error Response Format

All error responses follow this standard format:

```json
{
  "status": "error",
  "code": "ERROR_CODE",
  "message": "Human-readable error message",
  "description": "Optional detailed description",
  "errors": ["List of validation errors"],
  "details": {
    "key": "Additional context information"
  },
  "path": "/api/endpoint",
  "timestamp": "2024-01-01T12:00:00",
  "correlationId": "abc-123-def-456",
  "requestId": "req-789",
  "userId": 123,
  "tenantId": 456
}
```

## Best Practices

1. **Always use specific error codes** - Use the most specific error code for the situation
2. **Provide clear messages** - Error messages should be clear and actionable
3. **Include context** - Add relevant details to help debugging
4. **Log errors server-side** - Always log errors with full context
5. **Don't expose sensitive data** - Never include passwords, tokens, or sensitive data in error messages
6. **Use consistent format** - Follow the standard error response format
