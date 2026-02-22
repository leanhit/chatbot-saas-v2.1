# User Hub API Documentation

## Overview
The User Hub manages user profiles, preferences, activities, and analytics for the chatbot SaaS platform.

## Base URL
```
http://localhost:8080/api/v1/users
```

## Authentication
All endpoints require JWT token in Authorization header:
```
Authorization: Bearer <jwt-token>
```

## Endpoints

### User Profile Management

#### GET /profile
Get current user's complete profile including preferences and activities.

**Response:**
```json
{
  "id": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "avatar": "https://example.com/avatar.jpg",
  "timezone": "UTC",
  "language": "en",
  "status": "ACTIVE",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z",
  "preferences": {
    "notifications": true,
    "theme": "light",
    "currency": "USD"
  },
  "addresses": [
    {
      "id": 1,
      "type": "HOME",
      "street": "123 Main St",
      "city": "New York",
      "state": "NY",
      "zipCode": "10001",
      "country": "USA"
    }
  ]
}
```

#### PUT /profile
Update user profile information.

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Smith",
  "phone": "+1234567890",
  "timezone": "America/New_York",
  "language": "en"
}
```

#### POST /profile/avatar
Upload user avatar.

**Request:** multipart/form-data
- `avatar`: Image file (max 5MB)

**Response:**
```json
{
  "avatarUrl": "https://example.com/new-avatar.jpg"
}
```

### User Preferences

#### GET /preferences
Get user preferences.

**Response:**
```json
{
  "notifications": {
    "email": true,
    "sms": false,
    "push": true
  },
  "ui": {
    "theme": "dark",
    "language": "en",
    "timezone": "America/New_York"
  },
  "privacy": {
    "profileVisibility": "PUBLIC",
    "activityTracking": true
  }
}
```

#### PUT /preferences
Update user preferences.

**Request Body:**
```json
{
  "notifications": {
    "email": false,
    "sms": true,
    "push": true
  },
  "ui": {
    "theme": "light",
    "language": "es"
  }
}
```

### User Activities

#### GET /activities
Get user activity history with pagination.

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `type`: Activity type filter (optional)
- `fromDate`: Start date filter (optional)
- `toDate`: End date filter (optional)

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "type": "LOGIN",
      "description": "User logged in from New York",
      "timestamp": "2024-01-01T10:00:00Z",
      "ipAddress": "192.168.1.1",
      "userAgent": "Mozilla/5.0...",
      "metadata": {
        "location": "New York, USA"
      }
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

#### POST /activities
Log a user activity.

**Request Body:**
```json
{
  "type": "MESSAGE_SENT",
  "description": "User sent a message to bot",
  "metadata": {
    "botId": "bot_123",
    "messageType": "TEXT"
  }
}
```

### User Analytics

#### GET /analytics/summary
Get user analytics summary.

**Response:**
```json
{
  "totalMessages": 1500,
  "totalSessions": 45,
  "averageSessionDuration": 300,
  "lastActiveAt": "2024-01-01T10:00:00Z",
  "mostActiveHour": 14,
  "preferredChannels": ["FACEBOOK", "WEBSITE"],
  "engagementScore": 85
}
```

#### GET /analytics/messages
Get message analytics with date range.

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
      "sent": 25,
      "received": 30,
      "engagementRate": 0.83
    }
  ]
}
```

### Address Management

#### GET /addresses
Get user's addresses.

**Response:**
```json
{
  "addresses": [
    {
      "id": 1,
      "type": "HOME",
      "street": "123 Main St",
      "city": "New York",
      "state": "NY",
      "zipCode": "10001",
      "country": "USA",
      "isDefault": true
    }
  ]
}
```

#### POST /addresses
Add new address.

**Request Body:**
```json
{
  "type": "WORK",
  "street": "456 Business Ave",
  "city": "New York",
  "state": "NY",
  "zipCode": "10002",
  "country": "USA",
  "isDefault": false
}
```

#### PUT /addresses/{addressId}
Update existing address.

#### DELETE /addresses/{addressId}
Delete address.

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
- `VALIDATION_ERROR` (400): Invalid request data
- `NOT_FOUND` (404): Resource not found
- `FILE_TOO_LARGE` (413): Avatar file exceeds size limit
- `UNSUPPORTED_FILE_TYPE` (415): Invalid file format

## gRPC Services

### UserService

- `GetUserProfile`: Get complete user profile by ID
- `UpdateUserProfile`: Update user profile
- `GetUserPreferences`: Get user preferences
- `UpdateUserPreferences`: Update user preferences
- `LogUserActivity`: Log user activity
- `GetUserAnalytics`: Get user analytics data

## Rate Limiting

API endpoints are rate-limited:
- Profile endpoints: 100 requests per minute
- Activity endpoints: 200 requests per minute
- Analytics endpoints: 50 requests per minute
- Avatar upload: 10 requests per minute

## Security

- All user data is encrypted at rest
- Avatar images are scanned for malware
- Activity logs are retained for 90 days
- PII is masked in logs
