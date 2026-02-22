# Identity Hub API Documentation

## Overview
The Identity Hub provides authentication and user management services for the chatbot SaaS platform.

## Base URL
```
http://localhost:8080/api/v1/identity
```

## Authentication
All endpoints (except login/register) require JWT token in Authorization header:
```
Authorization: Bearer <jwt-token>
```

## Endpoints

### Authentication

#### POST /auth/login
Login user and return JWT token.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "jwt-token-here",
  "email": "user@example.com",
  "role": "USER",
  "expiresIn": 3600
}
```

#### POST /auth/register
Register new user account.

**Request Body:**
```json
{
  "email": "newuser@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### POST /auth/refresh
Refresh JWT token.

**Headers:**
```
Authorization: Bearer <current-jwt-token>
```

**Response:**
```json
{
  "token": "new-jwt-token",
  "expiresIn": 3600
}
```

### User Management

#### GET /users/profile
Get current user profile.

**Response:**
```json
{
  "id": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "USER",
  "isActive": true,
  "createdAt": "2024-01-01T00:00:00Z"
}
```

#### PUT /users/profile
Update user profile.

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Smith",
  "phone": "+1234567890"
}
```

#### POST /auth/change-password
Change user password.

**Request Body:**
```json
{
  "currentPassword": "oldpassword",
  "newPassword": "newpassword123"
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
- `NOT_FOUND` (404): Resource not found
- `VALIDATION_ERROR` (400): Invalid request data
- `INTERNAL_ERROR` (500): Server error

## gRPC Services

The Identity Hub also provides internal gRPC services for inter-hub communication:

### IdentityService

- `ValidateToken`: Validate JWT token and return user info
- `GetUserProfile`: Get user profile by ID
- `ValidateUser`: Check if user exists and is active
- `GetUserRole`: Get user role
- `IsUserActive`: Check if user is active

## Rate Limiting

API endpoints are rate-limited:
- Authentication endpoints: 5 requests per minute
- User profile endpoints: 100 requests per minute

## Security

- Passwords are hashed using bcrypt
- JWT tokens expire after 1 hour
- All sensitive data is encrypted at rest
- HTTPS is required in production
