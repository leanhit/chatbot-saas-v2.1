# Frontend Error Handling Guide

This guide explains how to handle errors in the Chatbot SaaS frontend application, aligned with the backend error handling system.

## Overview

The frontend uses a centralized error handling system that:
- Extracts error codes from backend responses
- Maps error codes to user-friendly localized messages
- Provides utility functions for common error scenarios
- Supports both English and Vietnamese languages

## File Structure

```
frontend/src/
├── utils/
│   ├── errorHandler.js      # Main error handling utilities
│   └── errorCodes.js        # Error code constants
├── locales/
│   ├── en.json             # English error messages
│   └── vi.json             # Vietnamese error messages
```

## Error Handler Utilities

### errorHandler.js

The main error handling utility provides the following functions:

#### `getErrorMessage(errorCode, fallbackMessage)`

Get localized error message for an error code.

```javascript
import { getErrorMessage } from '@/utils/errorHandler'

const message = getErrorMessage('USER_NOT_FOUND', 'User not found')
```

#### `extractErrorCode(error)`

Extract error code from axios error response.

```javascript
import { extractErrorCode } from '@/utils/errorHandler'

const errorCode = extractErrorCode(error)
```

#### `getErrorFromResponse(error, fallbackMessage)`

Get user-friendly error message from axios error.

```javascript
import { getErrorFromResponse } from '@/utils/errorHandler'

const message = getErrorFromResponse(error, 'An error occurred')
```

#### `handleApiError(error, options)`

Handle API error with logging and message extraction.

```javascript
import { handleApiError } from '@/utils/errorHandler'

const message = handleApiError(error, {
  fallback: 'Default error message',
  logError: true
})
```

#### Error Type Checkers

```javascript
import { 
  isAuthError,
  isValidationError,
  isFileValidationError,
  isAgentValidationError,
  isWebhookValidationError,
  isPackageValidationError,
  isDiscountValidationError,
  isTenantError,
  isPaymentError
} from '@/utils/errorHandler'

if (isAuthError(error)) {
  // Redirect to login
}

if (isFileValidationError(error)) {
  // Show file upload error
}
```

#### `getValidationErrors(error)`

Extract validation errors from response.

```javascript
import { getValidationErrors } from '@/utils/errorHandler'

const errors = getValidationErrors(error)
errors.forEach(err => console.log(err))
```

#### `getFileErrorDetails(error)`

Get detailed file error information.

```javascript
import { getFileErrorDetails } from '@/utils/errorHandler'

const details = getFileErrorDetails(error)
if (details) {
  console.log('Error type:', details.type)
  console.log('Max size:', details.maxSize)
  console.log('Message:', details.message)
}
```

#### `logErrorWithCorrelation(error, context)`

Log error with correlation ID for debugging.

```javascript
import { logErrorWithCorrelation } from '@/utils/errorHandler'

logErrorWithCorrelation(error, 'Upload avatar')
```

### errorCodes.js

Error code constants for type-safe error handling.

```javascript
import { 
  USER_NOT_FOUND,
  TENANT_NOT_FOUND,
  INVALID_FILE_TYPE,
  FILE_TOO_LARGE
} from '@/utils/errorCodes'

// Use constants instead of hardcoded strings
if (errorCode === USER_NOT_FOUND) {
  // Handle user not found
}
```

#### Error Code Groups

```javascript
import { 
  AUTH_ERROR_CODES,
  VALIDATION_ERROR_CODES,
  FILE_ERROR_CODES,
  PAYMENT_ERROR_CODES
} from '@/utils/errorCodes'

if (AUTH_ERROR_CODES.includes(errorCode)) {
  // Handle auth errors
}
```

## Usage Examples

### Vue Component Example

```vue
<template>
  <div>
    <input type="file" @change="handleFileUpload" />
    <div v-if="error" class="error">{{ error }}</div>
  </div>
</template>

<script>
import { handleApiError, isFileValidationError, getFileErrorDetails } from '@/utils/errorHandler'

export default {
  data() {
    return {
      error: null
    }
  },
  methods: {
    async handleFileUpload(event) {
      const file = event.target.files[0]
      const formData = new FormData()
      formData.append('file', file)
      
      try {
        await this.$api.uploadFile(formData)
        this.error = null
      } catch (error) {
        if (isFileValidationError(error)) {
          const details = getFileErrorDetails(error)
          if (details.type === 'too_large') {
            this.error = `File too large. Maximum size: ${this.formatBytes(details.maxSize)}`
          } else if (details.type === 'invalid_type') {
            this.error = `Invalid file type. Allowed: ${details.allowedTypes}`
          } else {
            this.error = details.message
          }
        } else {
          this.error = handleApiError(error)
        }
      }
    },
    formatBytes(bytes) {
      if (bytes === 0) return '0 Bytes'
      const k = 1024
      const sizes = ['Bytes', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
    }
  }
}
</script>
```

### Form Validation Example

```vue
<template>
  <form @submit.prevent="handleSubmit">
    <input v-model="form.name" />
    <div v-if="errors.name" class="error">{{ errors.name }}</div>
    
    <input v-model="form.email" />
    <div v-if="errors.email" class="error">{{ errors.email }}</div>
    
    <button type="submit">Submit</button>
  </form>
</template>

<script>
import { getValidationErrors, handleApiError } from '@/utils/errorHandler'

export default {
  data() {
    return {
      form: {
        name: '',
        email: ''
      },
      errors: {}
    }
  },
  methods: {
    async handleSubmit() {
      this.errors = {}
      
      try {
        await this.$api.createResource(this.form)
      } catch (error) {
        const validationErrors = getValidationErrors(error)
        
        if (validationErrors.length > 0) {
          // Parse validation errors
          validationErrors.forEach(err => {
            const match = err.match(/Field '(\w+)' (.+)/)
            if (match) {
              this.errors[match[1]] = match[2]
            } else {
              this.errors.general = err
            }
          })
        } else {
          this.errors.general = handleApiError(error)
        }
      }
    }
  }
}
</script>
```

### Authentication Error Handling

```javascript
// In your API interceptor or router guard
import { isAuthError } from '@/utils/errorHandler'

api.interceptors.response.use(
  response => response,
  error => {
    if (isAuthError(error)) {
      // Clear auth token
      localStorage.removeItem('authToken')
      
      // Redirect to login
      router.push('/login?redirect=' + encodeURIComponent(window.location.pathname))
    }
    
    return Promise.reject(error)
  }
)
```

### Payment Error Handling

```vue
<template>
  <div>
    <button @click="makePayment">Pay $100</button>
    <div v-if="error" class="error">{{ error }}</div>
  </div>
</template>

<script>
import { handleApiError, isPaymentError, PAYMENT_ERROR_CODES } from '@/utils/errorHandler'
import { INSUFFICIENT_BALANCE } from '@/utils/errorCodes'

export default {
  data() {
    return {
      error: null
    }
  },
  methods: {
    async makePayment() {
      try {
        await this.$api.processPayment({ amount: 100 })
        this.error = null
      } catch (error) {
        if (isPaymentError(error)) {
          const errorCode = error.response?.data?.code
          
          if (errorCode === INSUFFICIENT_BALANCE) {
            const details = error.response?.data?.details || {}
            this.error = `Insufficient balance. Required: $${details.required}, Available: $${details.available}`
          } else {
            this.error = handleApiError(error)
          }
        } else {
          this.error = handleApiError(error)
        }
      }
    }
  }
}
</script>
```

## Localization

Error messages are localized in `locales/en.json` and `locales/vi.json`.

### Adding New Error Messages

1. Add the error code to both locale files:

**en.json:**
```json
{
  "errors": {
    "NEW_ERROR_CODE": "Error message in English"
  }
}
```

**vi.json:**
```json
{
  "errors": {
    "NEW_ERROR_CODE": "Thông báo lỗi bằng tiếng Việt"
  }
}
```

2. Use the error code in your code:

```javascript
import { getErrorMessage } from '@/utils/errorHandler'

const message = getErrorMessage('NEW_ERROR_CODE')
```

## Best Practices

1. **Always use error code constants** - Import from `errorCodes.js` instead of hardcoded strings
2. **Handle specific error types** - Use type checker functions for specific error scenarios
3. **Provide user-friendly messages** - Use localized messages instead of raw server messages
4. **Log errors with context** - Use `logErrorWithCorrelation` for debugging
5. **Handle authentication errors** - Redirect to login on auth errors
6. **Show file upload details** - Use `getFileErrorDetails` for file upload errors
7. **Parse validation errors** - Use `getValidationErrors` for form validation
8. **Don't expose sensitive data** - Never show passwords or tokens in error messages
9. **Test error scenarios** - Test various error conditions in your components
10. **Use fallback messages** - Provide fallback messages for unknown error codes

## Error Code Reference

### Common Errors
- `NOT_FOUND` - Resource not found
- `VALIDATION_ERROR` - Validation failed
- `UNAUTHORIZED` - Not authenticated
- `FORBIDDEN` - Access denied
- `BAD_REQUEST` - Invalid request
- `INTERNAL_ERROR` - Internal server error

### Authentication Errors
- `BAD_CREDENTIALS` - Invalid credentials
- `AUTHENTICATION_FAILED` - Authentication failed
- `INVALID_TOKEN` - Invalid token
- `TOKEN_INVALID_OR_EXPIRED` - Token expired
- `USER_NOT_AUTHENTICATED` - User not authenticated

### File Validation Errors
- `INVALID_FILE_TYPE` - Invalid file type
- `FILE_TOO_LARGE` - File size exceeds limit
- `FILE_EMPTY` - File is empty
- `FILE_NULL` - File is null

### Payment Errors
- `PAYMENT_ERROR` - Payment processing failed
- `PAYMENT_NOT_FOUND` - Payment not found
- `INSUFFICIENT_BALANCE` - Insufficient balance
- `BANK_API_ERROR` - Bank API error

### Tenant Errors
- `TENANT_NOT_FOUND` - Tenant not found
- `TENANT_INACTIVE` - Tenant account suspended
- `TENANT_CONTEXT_MISSING` - Tenant context missing
- `NOT_TENANT_MEMBER` - Not a tenant member

For a complete list of error codes, see `errorCodes.js` or the backend documentation.

## Testing Error Handling

```javascript
// Test error handling with mock responses
describe('Error Handling', () => {
  it('handles file validation errors', () => {
    const error = {
      response: {
        data: {
          code: 'FILE_TOO_LARGE',
          message: 'File size exceeds limit',
          details: { maxSize: 10485760 }
        }
      }
    }
    
    expect(isFileValidationError(error)).toBe(true)
    const details = getFileErrorDetails(error)
    expect(details.type).toBe('too_large')
    expect(details.maxSize).toBe(10485760)
  })
  
  it('handles authentication errors', () => {
    const error = {
      response: {
        data: {
          code: 'UNAUTHORIZED',
          message: 'Authentication required'
        }
      }
    }
    
    expect(isAuthError(error)).toBe(true)
  })
})
```

## Support

For questions about error handling or to report issues, contact the backend team with the correlation ID from the error response.
