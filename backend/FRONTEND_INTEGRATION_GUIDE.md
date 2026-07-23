# Frontend Integration Guide for Error Handling

This guide helps frontend developers integrate with the Chatbot SaaS backend API error handling system.

## Overview

The backend uses a standardized error response format with error codes that the frontend should handle consistently. All errors follow a predictable structure, making it easy to implement robust error handling on the frontend.

## Error Response Format

All error responses from the backend follow this structure:

```typescript
interface ErrorResponse {
  status: "error";
  code: string;           // Error code from ErrorCode enum
  message: string;        // Human-readable error message
  description?: string;   // Optional detailed description
  errors?: string[];      // List of validation errors
  details?: Record<string, any>; // Additional context
  path: string;           // Request path
  timestamp: string;      // ISO 8601 timestamp
  correlationId?: string;  // For tracing
  requestId?: string;     // For tracing
  userId?: number;        // User ID if authenticated
  tenantId?: number;      // Tenant ID if available
}
```

## HTTP Status Codes

The backend uses appropriate HTTP status codes:

| Status | Meaning | Frontend Action |
|--------|---------|-----------------|
| 200 | Success | Process response normally |
| 400 | Bad Request | Show validation errors to user |
| 401 | Unauthorized | Redirect to login |
| 403 | Forbidden | Show "access denied" message |
| 404 | Not Found | Show "resource not found" message |
| 409 | Conflict | Show "already exists" message |
| 413 | Payload Too Large | Show file size error |
| 429 | Rate Limit Exceeded | Show rate limit message |
| 500 | Server Error | Show generic error message |
| 502 | Bad Gateway | Show service unavailable message |
| 503 | Service Unavailable | Show maintenance message |
| 504 | Gateway Timeout | Show timeout message |

## Integration Examples

### React with Axios

```typescript
import axios, { AxiosError } from 'axios';

// Create axios instance with interceptors
const api = axios.create({
  baseURL: process.env.API_BASE_URL,
  timeout: 30000,
});

// Request interceptor - add auth token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor - handle errors
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ErrorResponse>) => {
    if (!error.response) {
      // Network error
      handleNetworkError(error);
      return Promise.reject(error);
    }

    const { status, data } = error.response;
    
    switch (status) {
      case 401:
        handleUnauthorized(data);
        break;
      case 403:
        handleForbidden(data);
        break;
      case 404:
        handleNotFound(data);
        break;
      case 409:
        handleConflict(data);
        break;
      case 429:
        handleRateLimit(data);
        break;
      case 500:
      case 502:
      case 503:
      case 504:
        handleServerError(data);
        break;
      case 400:
        handleBadRequest(data);
        break;
      default:
        handleGenericError(data);
    }
    
    return Promise.reject(error);
  }
);

// Error handlers
function handleUnauthorized(error: ErrorResponse) {
  // Clear token and redirect to login
  localStorage.removeItem('authToken');
  window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname);
}

function handleForbidden(error: ErrorResponse) {
  // Show access denied message
  showErrorToast(error.message || 'You do not have permission to access this resource');
}

function handleNotFound(error: ErrorResponse) {
  // Show not found message
  showErrorToast(error.message || 'Resource not found');
}

function handleConflict(error: ErrorResponse) {
  // Show conflict message
  showErrorToast(error.message || 'Resource already exists');
}

function handleRateLimit(error: ErrorResponse) {
  // Show rate limit message with retry info
  const retryAfter = error.details?.retryAfter || 60;
  showErrorToast(`Rate limit exceeded. Please try again in ${retryAfter} seconds.`);
}

function handleServerError(error: ErrorResponse) {
  // Show generic server error
  showErrorToast('An error occurred. Please try again later.');
  // Log correlation ID for debugging
  console.error('Server error:', error.correlationId);
}

function handleBadRequest(error: ErrorResponse) {
  // Handle validation errors
  if (error.errors && error.errors.length > 0) {
    // Show validation errors
    error.errors.forEach((err) => {
      showErrorToast(err);
    });
  } else {
    showErrorToast/error.message || 'Invalid request');
  }
}

function handleNetworkError(error: AxiosError) {
  showErrorToast('Network error. Please check your connection.');
}

function handleGenericError(error: ErrorResponse) {
  showErrorToast(error.message || 'An error occurred');
}

// Toast notification helper
function showErrorToast(message: string) {
  // Implement your toast notification logic
  console.error('Error:', message);
}
```

### Vue with Fetch API

```typescript
// API service class
class ApiService {
  private baseURL: string;
  
  constructor() {
    this.baseURL = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080';
  }
  
  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${this.baseURL}${endpoint}`;
    const token = localStorage.getItem('authToken');
    
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
      ...options.headers,
    };
    
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    
    try {
      const response = await fetch(url, {
        ...options,
        headers,
      });
      
      if (!response.ok) {
        const error: ErrorResponse = await response.json();
        this.handleError(error, response.status);
        throw error;
      }
      
      return response.json();
    } catch (error) {
      if (error instanceof TypeError) {
        // Network error
        this.showNotification('Network error. Please check your connection.', 'error');
      }
      throw error;
    }
  }
  
  private handleError(error: ErrorResponse, status: number): void {
    switch (status) {
      case 401:
        this.handleUnauthorized(error);
        break;
      case 403:
        this.showNotification(error.message || 'Access denied', 'error');
        break;
      case 404:
        this.showNotification(error.message || 'Resource not found', 'error');
        break;
      case 409:
        this.showNotification(error.message || 'Resource already exists', 'error');
        break;
      case 429:
        const retryAfter = error.details?.retryAfter || 60;
        this.showNotification(
          `Rate limit exceeded. Try again in ${retryAfter}s`,
          'warning'
        );
        break;
      case 400:
        if (error.errors?.length) {
          error.errors.forEach(err => this.showNotification(err, 'error'));
        } else {
          this.showNotification(error.message || 'Invalid request', 'error');
        }
        break;
      default:
        if (status >= 500) {
          this.showNotification('Server error. Please try again later.', 'error');
          console.error('Server error:', error.correlationId);
        } else {
          this.showNotification(error.message || 'An error occurred', 'error');
        }
    }
  }
  
  private handleUnauthorized(error: ErrorResponse): void {
    localStorage.removeItem('authToken');
    window.location.href = '/login';
  }
  
  private showNotification(message: string, type: 'error' | 'warning' | 'info'): void {
    // Implement your notification system
    console.log(`[${type.toUpperCase()}] ${message}`);
  }
  
  async get<T>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'GET' });
  }
  
  async post<T>(endpoint: string, data: any): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }
  
  async put<T>(endpoint: string, data: any): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  }
  
  async delete<T>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'DELETE' });
  }
}

export const apiService = new ApiService();
```

### Error Code Mapping

Create a mapping of error codes to user-friendly messages:

```typescript
const ERROR_MESSAGES: Record<string, string> = {
  // Common errors
  NOT_FOUND: 'Resource not found',
  VALIDATION_ERROR: 'Please check your input',
  UNAUTHORIZED: 'Please log in to continue',
  FORBIDDEN: 'You do not have permission',
  BAD_REQUEST: 'Invalid request',
  
  // Authentication errors
  BAD_CREDENTIALS: 'Invalid email or password',
  AUTHENTICATION_FAILED: 'Authentication failed',
  INVALID_TOKEN: 'Session expired. Please log in again',
  
  // User errors
  EMAIL_ALREADY_EXISTS: 'This email is already registered',
  USER_NOT_FOUND: 'User not found',
  
  // Tenant errors
  TENANT_NOT_FOUND: 'Tenant not found',
  TENANT_INACTIVE: 'Tenant account is suspended',
  INSUFFICIENT_PERMISSION: 'You do not have permission',
  
  // Payment errors
  PAYMENT_ERROR: 'Payment processing failed',
  PAYMENT_NOT_FOUND: 'Payment not found',
  INVALID_PAYMENT_AMOUNT: 'Invalid payment amount',
  INSUFFICIENT_BALANCE: 'Insufficient balance',
  
  // File errors
  INVALID_FILE_TYPE: 'Invalid file type',
  FILE_TOO_LARGE: 'File is too large',
  FILE_EMPTY: 'File is empty',
  
  // Agent errors
  INVALID_AGENT_NAME: 'Invalid agent name',
  INVALID_AGENT_EMAIL: 'Invalid agent email',
  
  // Webhook errors
  WEBHOOK_URL_EXISTS: 'Webhook URL already exists',
  WEBHOOK_TEST_FAILED: 'Webhook test failed',
  
  // Package errors
  PACKAGE_NOT_ACTIVE: 'Package is not available',
  
  // Discount errors
  DISCOUNT_CODE_EXISTS: 'Discount code already exists',
  
  // Password errors
  PASSWORD_CONFIRMATION_MISMATCH: 'Passwords do not match',
};

function getErrorMessage(errorCode: string, defaultMessage?: string): string {
  return ERROR_MESSAGES[errorCode] || defaultMessage || 'An error occurred';
}
```

## File Upload Error Handling

Special handling for file upload errors:

```typescript
async function uploadFile(file: File): Promise<void> {
  const formData = new FormData();
  formData.append('file', file);
  
  try {
    const response = await fetch('/api/upload', {
      method: 'POST',
      body: formData,
    });
    
    if (!response.ok) {
      const error: ErrorResponse = await response.json();
      
      switch (error.code) {
        case 'FILE_NULL':
          showError('Please select a file');
          break;
        case 'FILE_EMPTY':
          showError('File is empty');
          break;
        case 'INVALID_FILE_TYPE':
          showError('Invalid file type. Allowed types: ' + error.details?.allowedTypes);
          break;
        case 'FILE_TOO_LARGE':
          const maxSize = error.details?.maxSize || 10485760;
          showError(`File too large. Maximum size: ${formatBytes(maxSize)}`);
          break;
        default:
          showError(error.message || 'Upload failed');
      }
      
      throw error;
    }
    
    return response.json();
  } catch (error) {
    console.error('Upload error:', error);
    throw error;
  }
}

function formatBytes(bytes: number): string {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
}
```

## Form Validation Error Handling

Display validation errors in forms:

```typescript
interface FormErrors {
  [fieldName: string]: string;
}

function handleFormError(error: ErrorResponse): FormErrors {
  const errors: FormErrors = {};
  
  if (error.errors && Array.isArray(error.errors)) {
    // Parse validation errors
    error.errors.forEach((err) => {
      // Assuming format: "Field 'name' is required"
      const match = err.match(/Field '(\w+)' (.+)/);
      if (match) {
        errors[match[1]] = match[2];
      } else {
        errors.general = err;
      }
    });
  } else if (error.details) {
    // Use details if available
    Object.entries(error.details).forEach(([key, value]) => {
      errors[key] = String(value);
    });
  } else {
    errors.general = error.message || 'Validation failed';
  }
  
  return errors;
}

// Usage in React form
function MyForm() {
  const [errors, setErrors] = useState<FormErrors>({});
  
  const handleSubmit = async (data: FormData) => {
    try {
      await api.post('/api/resource', data);
      setErrors({});
    } catch (error: any) {
      if (error.response?.data) {
        setErrors(handleFormError(error.response.data));
      }
    }
  };
  
  return (
    <form onSubmit={handleSubmit}>
      <input name="name" />
      {errors.name && <div className="error">{errors.name}</div>}
      
      <input name="email" />
      {errors.email && <div className="error">{errors.email}</div>}
      
      {errors.general && <div className="error">{errors.general}</div>}
      
      <button type="submit">Submit</button>
    </form>
  );
}
```

## Retry Logic

Implement retry logic for transient errors:

```typescript
async function withRetry<T>(
  fn: () => Promise<T>,
  maxRetries = 3,
  delay = 1000
): Promise<T> {
  let lastError: any;
  
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await fn();
    } catch (error: any) {
      lastError = error;
      
      // Don't retry on client errors
      if (error.response?.status >= 400 && error.response?.status < 500) {
        throw error;
      }
      
      // Retry on server errors
      if (i < maxRetries - 1) {
        await new Promise(resolve => setTimeout(resolve, delay * (i + 1)));
      }
    }
  }
  
  throw lastError;
}

// Usage
const data = await withRetry(() => api.get('/api/data'));
```

## Error Logging

Log errors for debugging:

```typescript
function logError(error: ErrorResponse, context?: any): void {
  const logData = {
    code: error.code,
    message: error.message,
    path: error.path,
    timestamp: error.timestamp,
    correlationId: error.correlationId,
    requestId: error.requestId,
    context,
  };
  
  // Send to error tracking service (e.g., Sentry, LogRocket)
  if (typeof window !== 'undefined' && (window as any).Sentry) {
    (window as any).Sentry.captureException(error, {
      extra: logData,
    });
  }
  
  // Log to console in development
  if (process.env.NODE_ENV === 'development') {
    console.error('Error:', logData);
  }
}
```

## Best Practices

1. **Always handle errors gracefully** - Never let errors crash the UI
2. **Show user-friendly messages** - Translate error codes to readable messages
3. **Provide actionable feedback** - Tell users what they can do to fix the error
4. **Log correlation IDs** - Include correlation IDs when reporting issues
5. **Implement retry logic** - Retry transient errors automatically
6. **Handle authentication errors** - Redirect to login on 401
7. **Validate forms client-side** - Prevent unnecessary server requests
8. **Show loading states** - Disable buttons during API calls
9. **Handle network errors** - Show appropriate messages for offline scenarios
10. **Test error scenarios** - Test various error conditions

## Testing Error Handling

```typescript
// Test error handling with mock responses
describe('API Error Handling', () => {
  it('handles 401 unauthorized', async () => {
    mockApi.get('/api/resource').mockRejectedValue({
      response: {
        status: 401,
        data: {
          status: 'error',
          code: 'UNAUTHORIZED',
          message: 'Authentication required',
        },
      },
    });
    
    await expect(api.get('/api/resource')).rejects.toThrow();
    expect(window.location.href).toContain('/login');
  });
  
  it('handles validation errors', async () => {
    mockApi.post('/api/resource').mockRejectedValue({
      response: {
        status: 400,
        data: {
          status: 'error',
          code: 'VALIDATION_ERROR',
          message: 'Validation failed',
          errors: ['Field "name" is required', 'Field "email" is invalid'],
        },
      },
    });
    
    await expect(api.post('/api/resource', {})).rejects.toThrow();
  });
});
```

## Support

For questions about error handling or to report issues with error codes, contact the backend team with the correlation ID from the error response.
