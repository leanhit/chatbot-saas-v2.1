# HTTP Status Code Mapping

This document describes how error codes map to HTTP status codes in the Chatbot SaaS backend API.

## HTTP Status Code Categories

### 2xx Success
- **200 OK**: Request succeeded
- **201 Created**: Resource created successfully
- **204 No Content**: Request succeeded with no content

### 4xx Client Errors
- **400 Bad Request**: Invalid request format or data
- **401 Unauthorized**: Authentication required or failed
- **403 Forbidden**: Authentication succeeded but access denied
- **404 Not Found**: Resource not found
- **409 Conflict**: Resource conflict (already exists)
- **413 Payload Too Large**: Request payload exceeds size limit
- **429 Too Many Requests**: Rate limit exceeded

### 5xx Server Errors
- **500 Internal Server Error**: Unexpected server error
- **502 Bad Gateway**: External service integration failed
- **503 Service Unavailable**: Service temporarily unavailable
- **504 Gateway Timeout**: Request timeout

---

## Error Code to HTTP Status Mapping

### HTTP 400 Bad Request

Error codes that map to **400 Bad Request**:

| Error Code | Description | Usage |
|------------|-------------|-------|
| VALIDATION_ERROR | Generic validation error | Request validation fails |
| BAD_REQUEST | Invalid request | Request format or data is invalid |
| INVALID_REQUEST_BODY | Invalid request body | Request body format is invalid |
| INVALID_TENANT_KEY | Invalid tenant key | Tenant key format is invalid |
| INVALID_PAYMENT_AMOUNT | Invalid payment amount | Payment amount is invalid |
| INVALID_FILE_TYPE | Invalid file type | Uploaded file type not allowed |
| FILE_EMPTY | File is empty | Uploaded file has no content |
| FILE_NULL | File is null | File parameter is null |
| INVALID_AGENT_NAME | Invalid agent name | Agent name is empty or invalid |
| INVALID_AGENT_EMAIL | Invalid agent email | Agent email is empty or invalid |
| INVALID_AGENT_ROLE | Invalid agent role | Agent role is null or invalid |
| INVALID_AGENT_MAX_CONCURRENT | Invalid max concurrent | Max concurrent is invalid |
| INVALID_AGENT_CURRENT_LOAD | Invalid current load | Current load is invalid |
| AGENT_TENANT_ID_REQUIRED | Tenant ID required | Agent tenant ID is null |
| INVALID_WEBHOOK_URL | Invalid webhook URL | Webhook URL format is invalid |
| PACKAGE_NOT_ACTIVE | Package not active | Package is inactive |
| PASSWORD_CONFIRMATION_MISMATCH | Password mismatch | Password confirmation doesn't match |
| RSA_PRIVATE_KEY_REQUIRED | RSA private key required | RSA private key missing for RS256 |
| RSA_PUBLIC_KEY_REQUIRED | RSA public key required | RSA public key missing for RS256 |
| INSUFFICIENT_BALANCE | Insufficient balance | User balance insufficient |
| PAYMENT_EXPIRED | Payment expired | Payment has expired |
| TENANT_CONTEXT_MISSING | Tenant context missing | Tenant ID missing from context |

---

### HTTP 401 Unauthorized

Error codes that map to **401 Unauthorized**:

| Error Code | Description | Usage |
|------------|-------------|-------|
| UNAUTHORIZED | Not authenticated | Authentication required but not provided |
| BAD_CREDENTIALS | Invalid credentials | Login credentials are incorrect |
| AUTHENTICATION_FAILED | Authentication failed | Authentication process fails |
| INVALID_TOKEN | Invalid token | JWT token is invalid or expired |
| USER_NOT_AUTHENTICATED | User not authenticated | User is not authenticated |

---

### HTTP 403 Forbidden

Error codes that map to **403 Forbidden**:

| Error Code | Description | Usage |
|------------|-------------|-------|
| FORBIDDEN | Access denied | User lacks permission |
| ACCESS_DENIED | Access denied | Specific access control scenario |
| INSUFFICIENT_PERMISSION | Insufficient permissions | User lacks required permissions |
| TENANT_INACTIVE | Tenant inactive | Tenant account is inactive |
| LICENSE_EXPIRED | License expired | License has expired |
| LICENSE_INACTIVE | License inactive | License is inactive |

---

### HTTP 404 Not Found

Error codes that map to **404 Not Found**:

| Error Code | Description | Usage |
|------------|-------------|-------|
| NOT_FOUND | Resource not found | Generic resource not found |
| USER_NOT_FOUND | User not found | Requested user does not exist |
| TENANT_NOT_FOUND | Tenant not found | Requested tenant does not exist |
| LICENSE_NOT_FOUND | License not found | Requested license does not exist |
| PAYMENT_NOT_FOUND | Payment not found | Requested payment does not exist |
| RESOURCE_NOT_FOUND | Resource not found | Generic resource not found |
| ESCALATION_TIER_NOT_FOUND | Escalation tier not found | Requested escalation tier does not exist |
| SLA_CONFIG_NOT_FOUND | SLA config not found | Requested SLA configuration does not exist |
| ROUTING_RULE_NOT_FOUND | Routing rule not found | Requested routing rule does not exist |
| ENVIRONMENT_CONFIG_NOT_FOUND | Environment config not found | Requested environment config does not exist |
| CONVERSATION_NOT_FOUND | Conversation not found | Requested conversation does not exist |
| ENDPOINT_NOT_FOUND | Endpoint not found | API endpoint does not exist |

---

### HTTP 409 Conflict

Error codes that map to **409 Conflict**:

| Error Code | Description | Usage |
|------------|-------------|-------|
| CONFLICT | Resource conflict | Request conflicts with existing state |
| RESOURCE_ALREADY_EXISTS | Resource already exists | Resource already exists |
| EMAIL_ALREADY_EXISTS | Email already exists | Email already registered |
| WEBHOOK_URL_EXISTS | Webhook URL exists | Webhook URL already registered |
| PACKAGE_ID_EXISTS | Package ID exists | Package ID already registered |
| DISCOUNT_CODE_EXISTS | Discount code exists | Discount code already registered |

---

### HTTP 413 Payload Too Large

Error codes that map to **413 Payload Too Large**:

| Error Code | Description | Usage |
|------------|-------------|-------|
| PAYLOAD_TOO_LARGE | Payload too large | Request body exceeds size limit |
| FILE_TOO_LARGE | File too large | Uploaded file exceeds size limit |

---

### HTTP 429 Too Many Requests

Error codes that map to **429 Too Many Requests**:

| Error Code | Description | Usage |
|------------|-------------|-------|
| RATE_LIMIT_EXCEEDED | Rate limit exceeded | API rate limit exceeded |

---

### HTTP 500 Internal Server Error

Error codes that map to **500 Internal Server Error**:

| Error Code | Description | Usage |
|------------|-------------|-------|
| INTERNAL_ERROR | Internal error | Unexpected server error |
| NOTIFICATION_SEND_FAILED | Notification failed | Failed to send notification |
| RUNTIME_ERROR | Runtime error | Generic runtime error |
| DATA_INTEGRITY_VIOLATION | Data integrity violation | Database constraint violation |
| OPTIMISTIC_LOCK | Optimistic lock | Concurrent modification conflict |
| BULK_DELETE_ERROR | Bulk delete error | Bulk operation failed |

---

### HTTP 502 Bad Gateway

Error codes that map to **502 Bad Gateway**:

| Error Code | Description | Usage |
|------------|-------------|-------|
| INTEGRATION_ERROR | Integration error | External service integration failed |
| BANK_API_ERROR | Bank API error | Bank API integration fails |

---

### HTTP 503 Service Unavailable

Error codes that map to **503 Service Unavailable**:

| Error Code | Description | Usage |
|------------|-------------|-------|
| SERVICE_UNAVAILABLE | Service unavailable | Service temporarily unavailable |

---

### HTTP 504 Gateway Timeout

Error codes that map to **504 Gateway Timeout**:

| Error Code | Description | Usage |
|------------|-------------|-------|
| TIMEOUT | Request timeout | Request takes too long to process |

---

## GlobalExceptionHandler Mapping Logic

The `GlobalExceptionHandler` automatically maps exceptions to HTTP status codes based on the `ErrorCode`:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        HttpStatus status = mapErrorCodeToHttpStatus(errorCode);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .status("error")
            .code(errorCode.getCode())
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
            
        return ResponseEntity.status(status).body(errorResponse);
    }
    
    private HttpStatus mapErrorCodeToHttpStatus(ErrorCode errorCode) {
        // Mapping logic based on error code category
        return switch (errorCode) {
            case NOT_FOUND, USER_NOT_FOUND, TENANT_NOT_FOUND, LICENSE_NOT_FOUND,
                 PAYMENT_NOT_FOUND, RESOURCE_NOT_FOUND, ESCALATION_TIER_NOT_FOUND,
                 SLA_CONFIG_NOT_FOUND, ROUTING_RULE_NOT_FOUND, ENVIRONMENT_CONFIG_NOT_FOUND,
                 CONVERSATION_NOT_FOUND, ENDPOINT_NOT_FOUND -> HttpStatus.NOT_FOUND;
                 
            case UNAUTHORIZED, BAD_CREDENTIALS, AUTHENTICATION_FAILED, 
                 INVALID_TOKEN, USER_NOT_AUTHENTICATED -> HttpStatus.UNAUTHORIZED;
                 
            case FORBIDDEN, ACCESS_DENIED, INSUFFICIENT_PERMISSION,
                 TENANT_INACTIVE, LICENSE_EXPIRED, LICENSE_INACTIVE -> HttpStatus.FORBIDDEN;
                 
            case VALIDATION_ERROR, BAD_REQUEST, INVALID_REQUEST_BODY,
                 INVALID_TENANT_KEY, INVALID_PAYMENT_AMOUNT, INVALID_FILE_TYPE,
                 FILE_EMPTY, FILE_NULL, INVALID_AGENT_NAME, INVALID_AGENT_EMAIL,
                 INVALID_AGENT_ROLE, INVALID_AGENT_MAX_CONCURRENT, INVALID_AGENT_CURRENT_LOAD,
                 AGENT_TENANT_ID_REQUIRED, INVALID_WEBHOOK_URL, PACKAGE_NOT_ACTIVE,
                 PASSWORD_CONFIRMATION_MISMATCH, RSA_PRIVATE_KEY_REQUIRED, RSA_PUBLIC_KEY_REQUIRED,
                 INSUFFICIENT_BALANCE, PAYMENT_EXPIRED, TENANT_CONTEXT_MISSING -> HttpStatus.BAD_REQUEST;
                 
            case CONFLICT, RESOURCE_ALREADY_EXISTS, EMAIL_ALREADY_EXISTS,
                 WEBHOOK_URL_EXISTS, PACKAGE_ID_EXISTS, DISCOUNT_CODE_EXISTS -> HttpStatus.CONFLICT;
                 
            case PAYLOAD_TOO_LARGE, FILE_TOO_LARGE -> HttpStatus.PAYLOAD_TOO_LARGE;
                 
            case RATE_LIMIT_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;
                 
            case INTERNAL_ERROR, NOTIFICATION_SEND_FAILED, RUNTIME_ERROR,
                 DATA_INTEGRITY_VIOLATION, OPTIMISTIC_LOCK, BULK_DELETE_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
                 
            case INTEGRATION_ERROR, BANK_API_ERROR -> HttpStatus.BAD_GATEWAY;
                 
            case SERVICE_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
                 
            case TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;
                 
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
```

## Custom Exception HTTP Status Override

Custom exceptions can override the default HTTP status by implementing a custom mapping:

```java
public class CustomException extends BaseException {
    
    @Override
    public HttpStatus getHttpStatus() {
        // Override default status for this specific exception
        return HttpStatus.CONFLICT;
    }
}
```

## Best Practices

1. **Use appropriate HTTP status codes** - Match HTTP status to error semantics
2. **Be consistent** - Similar errors should use similar HTTP status codes
3. **Document custom mappings** - If you override default mappings, document why
4. **Test status codes** - Verify that error responses return correct HTTP status
5. **Frontend handling** - Frontend should handle different HTTP status codes appropriately

## Quick Reference

| HTTP Status | Error Code Category | Typical Usage |
|-------------|-------------------|---------------|
| 400 | Validation errors | Invalid input, validation failures |
| 401 | Authentication errors | Not authenticated, invalid credentials |
| 403 | Authorization errors | No permission, inactive resources |
| 404 | Not found errors | Resource does not exist |
| 409 | Conflict errors | Resource already exists |
| 413 | Size errors | Payload/file too large |
| 429 | Rate limiting | Too many requests |
| 500 | Server errors | Unexpected server failures |
| 502 | Integration errors | External service failures |
| 503 | Availability errors | Service down for maintenance |
| 504 | Timeout errors | Request processing timeout |
