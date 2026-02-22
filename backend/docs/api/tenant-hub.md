# Tenant Hub API Documentation

## Overview
The Tenant Hub manages workspace, tenant, and membership operations for the multi-tenant chatbot SaaS platform.

## Base URL
```
http://localhost:8080/api/v1/tenants
```

## Authentication
All endpoints require JWT token in Authorization header:
```
Authorization: Bearer <jwt-token>
```

## Endpoints

### Tenant Management

#### GET /tenants
Get list of tenants accessible to current user.

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `search`: Search term (optional)
- `status`: Filter by status (optional)

**Response:**
```json
{
  "content": [
    {
      "id": "tenant_123",
      "name": "Acme Corporation",
      "description": "Customer service chatbot",
      "status": "ACTIVE",
      "visibility": "PUBLIC",
      "ownerId": "user_456",
      "memberCount": 15,
      "createdAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-01-01T00:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 5,
  "totalPages": 1
}
```

#### POST /tenants
Create a new tenant.

**Request Body:**
```json
{
  "name": "New Company",
  "description": "Customer support automation",
  "visibility": "PRIVATE",
  "industry": "TECHNOLOGY",
  "companySize": "MEDIUM"
}
```

**Response:**
```json
{
  "id": "tenant_789",
  "name": "New Company",
  "description": "Customer support automation",
  "status": "ACTIVE",
  "visibility": "PRIVATE",
  "ownerId": "user_456",
  "memberCount": 1,
  "createdAt": "2024-01-01T00:00:00Z"
}
```

#### GET /tenants/{tenantId}
Get tenant details by ID.

**Response:**
```json
{
  "id": "tenant_123",
  "name": "Acme Corporation",
  "description": "Customer service chatbot",
  "status": "ACTIVE",
  "visibility": "PUBLIC",
  "ownerId": "user_456",
  "profile": {
    "industry": "RETAIL",
    "companySize": "LARGE",
    "website": "https://acme.com",
    "logo": "https://example.com/logo.png",
    "settings": {
      "timezone": "America/New_York",
      "currency": "USD",
      "language": "en"
    }
  },
  "memberCount": 15,
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

#### PUT /tenants/{tenantId}
Update tenant information.

**Request Body:**
```json
{
  "name": "Updated Company Name",
  "description": "Updated description",
  "visibility": "PUBLIC"
}
```

#### DELETE /tenants/{tenantId}
Delete tenant (only owner can delete).

### Tenant Membership

#### GET /tenants/{tenantId}/members
Get tenant members.

**Response:**
```json
{
  "members": [
    {
      "id": "member_123",
      "userId": "user_456",
      "tenantId": "tenant_123",
      "role": "OWNER",
      "status": "ACTIVE",
      "joinedAt": "2024-01-01T00:00:00Z",
      "user": {
        "id": "user_456",
        "email": "owner@example.com",
        "firstName": "John",
        "lastName": "Doe"
      }
    }
  ]
}
```

#### POST /tenants/{tenantId}/members
Invite a new member to tenant.

**Request Body:**
```json
{
  "email": "newmember@example.com",
  "role": "ADMIN",
  "message": "Join our workspace!"
}
```

#### PUT /tenants/{tenantId}/members/{memberId}
Update member role.

**Request Body:**
```json
{
  "role": "ADMIN"
}
```

#### DELETE /tenants/{tenantId}/members/{memberId}
Remove member from tenant.

#### POST /tenants/{tenantId}/members/{memberId}/leave
Leave tenant (member can remove themselves).

### Tenant Profile

#### GET /tenants/{tenantId}/profile
Get tenant profile details.

**Response:**
```json
{
  "tenantId": "tenant_123",
  "industry": "RETAIL",
  "companySize": "LARGE",
  "website": "https://acme.com",
  "logo": "https://example.com/logo.png",
  "settings": {
    "timezone": "America/New_York",
    "currency": "USD",
    "language": "en",
    "businessHours": {
      "monday": { "open": "09:00", "close": "17:00" },
      "tuesday": { "open": "09:00", "close": "17:00" }
    }
  },
  "professional": {
    "subscription": "PREMIUM",
    "features": ["MULTI_BOT", "ANALYTICS", "CUSTOM_BRANDING"],
    "limits": {
      "maxUsers": 100,
      "maxBots": 10,
      "storageGB": 50
    }
  }
}
```

#### PUT /tenants/{tenantId}/profile
Update tenant profile.

**Request Body:**
```json
{
  "industry": "TECHNOLOGY",
  "companySize": "MEDIUM",
  "website": "https://newcompany.com",
  "settings": {
    "timezone": "UTC",
    "currency": "EUR"
  }
}
```

#### POST /tenants/{tenantId}/profile/logo
Upload tenant logo.

**Request:** multipart/form-data
- `logo`: Image file (max 2MB)

### Tenant Search

#### GET /tenants/search
Search for public tenants.

**Query Parameters:**
- `q`: Search query (required)
- `industry`: Filter by industry (optional)
- `size`: Result size (default: 10)

**Response:**
```json
{
  "results": [
    {
      "id": "tenant_123",
      "name": "Acme Corporation",
      "description": "Customer service chatbot",
      "industry": "RETAIL",
      "memberCount": 15,
      "logo": "https://example.com/logo.png"
    }
  ],
  "total": 1
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
- `NOT_FOUND` (404): Tenant not found
- `VALIDATION_ERROR` (400): Invalid request data
- `ALREADY_EXISTS` (409): Tenant already exists
- `MEMBER_LIMIT_EXCEEDED` (429): Member limit reached
- `INSUFFICIENT_PERMISSIONS` (403): Cannot perform action

## gRPC Services

### TenantService

- `GetTenantById`: Get tenant by ID
- `CreateTenant`: Create new tenant
- `UpdateTenant`: Update tenant information
- `DeleteTenant`: Delete tenant
- `GetTenantMembers`: Get tenant members
- `AddTenantMember`: Add member to tenant
- `RemoveTenantMember`: Remove member from tenant
- `ValidateTenantAccess`: Check if user has access to tenant

### MembershipService

- `GetMembershipByUserId`: Get user's memberships
- `UpdateMemberRole`: Update member role
- `ValidateMembership`: Check if user is member of tenant

## Rate Limiting

API endpoints are rate-limited:
- Tenant CRUD: 50 requests per minute
- Member management: 100 requests per minute
- Search: 200 requests per minute
- Profile updates: 30 requests per minute

## Security

- Tenant isolation enforced at database level
- Members can only access tenants they belong to
- Audit logs for all membership changes
- Sensitive tenant data encrypted at rest
