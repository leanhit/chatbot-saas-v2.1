# Phase 4 Intelligent Routing API Contract

## Overview
This document defines the API contract for Phase 4 Intelligent Routing endpoints. All endpoints require tenant context authentication via the `X-Tenant-Key` header.

## Base URL
```
https://truyenthongviet.vn/api/routing
```

## Authentication
- **Header**: `X-Tenant-Key`
- **Description**: Unique tenant identifier for multi-tenant isolation
- **Example**: `dac00205-1817-431f-b870-daa410ef5e75`

---

## 1. Routing Rules Management

### 1.1 Create Routing Rule
**Endpoint**: `POST /routing-rules`

**Request Body**:
```json
{
  "name": "VIP Customer Routing",
  "ruleType": "SKILL_BASED",
  "priority": 1,
  "targetType": "AGENT",
  "targetAgentId": 10,
  "targetTeamId": 20,
  "targetSkillId": 5,
  "conditions": {
    "customerTier": "VIP",
    "language": "en"
  },
  "isActive": true,
  "isDefault": false
}
```

**Response**:
```json
{
  "id": 1,
  "name": "VIP Customer Routing",
  "ruleType": "SKILL_BASED",
  "priority": 1,
  "targetType": "AGENT",
  "targetAgentId": 10,
  "targetTeamId": 20,
  "targetSkillId": 5,
  "conditions": {
    "customerTier": "VIP",
    "language": "en"
  },
  "isActive": true,
  "isDefault": false,
  "tenantId": 1,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**Rule Types**:
- `SKILL_BASED` - Route based on agent skills
- `PRIORITY_BASED` - Route based on agent priority score
- `ROUND_ROBIN` - Round-robin distribution
- `LEAST_BUSY` - Route to least busy agent
- `CUSTOM` - Custom routing logic

**Target Types**:
- `AGENT` - Route to specific agent
- `TEAM` - Route to team
- `SKILL` - Route based on skill requirements

**Condition Operators**:
- Simple equality: `{"customerTier": "VIP"}`
- Comparison: `{"age>18": true}`, `{"score>=80": true}`
- Contains: `{"name contains John": true}`
- In: `{"status in": ["open", "pending"]}`
- AND: `{"AND": [{"customerTier": "VIP"}, {"language": "en"}]}`
- OR: `{"OR": [{"customerTier": "VIP"}, {"customerTier": "ENTERPRISE"}]}`

---

### 1.2 Update Routing Rule
**Endpoint**: `PUT /routing-rules/{id}`

**Request Body**: Same as create

**Response**: Same as create response

---

### 1.3 Delete Routing Rule
**Endpoint**: `DELETE /routing-rules/{id}`

**Response**: `204 No Content`

---

### 1.4 Get Routing Rule by ID
**Endpoint**: `GET /routing-rules/{id}`

**Response**: Same as create response

---

### 1.5 List All Routing Rules
**Endpoint**: `GET /routing-rules`

**Query Parameters**:
- `isActive` (optional): Filter by active status
- `ruleType` (optional): Filter by rule type
- `isDefault` (optional): Filter by default status

**Response**:
```json
{
  "rules": [
    {
      "id": 1,
      "name": "VIP Customer Routing",
      "ruleType": "SKILL_BASED",
      "priority": 1,
      "targetType": "AGENT",
      "targetAgentId": 10,
      "targetTeamId": 20,
      "targetSkillId": 5,
      "conditions": {
        "customerTier": "VIP"
      },
      "isActive": true,
      "isDefault": false,
      "tenantId": 1,
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ]
}
```

---

### 1.6 Get Default Routing Rule
**Endpoint**: `GET /routing-rules/default`

**Response**: Same as create response

---

## 2. Agent Workload Management

### 2.1 Create/Update Agent Workload
**Endpoint**: `POST /agent-workload`

**Request Body**:
```json
{
  "agentId": 10,
  "teamId": 20,
  "activeConversations": 5,
  "maxConcurrentConversations": 10,
  "status": "AVAILABLE",
  "priorityScore": 50
}
```

**Response**:
```json
{
  "id": 1,
  "agentId": 10,
  "tenantId": 1,
  "teamId": 20,
  "activeConversations": 5,
  "maxConcurrentConversations": 10,
  "status": "AVAILABLE",
  "priorityScore": 50,
  "workloadPercentage": 50.0,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**Agent Status**:
- `AVAILABLE` - Agent is available for new conversations
- `BUSY` - Agent is at capacity
- `OFFLINE` - Agent is offline

---

### 2.2 Get Agent Workload
**Endpoint**: `GET /agent-workload/{agentId}`

**Response**: Same as create/update response

---

### 2.3 List All Agent Workloads
**Endpoint**: `GET /agent-workload`

**Query Parameters**:
- `status` (optional): Filter by status
- `teamId` (optional): Filter by team

**Response**:
```json
{
  "workloads": [
    {
      "id": 1,
      "agentId": 10,
      "tenantId": 1,
      "teamId": 20,
      "activeConversations": 5,
      "maxConcurrentConversations": 10,
      "status": "AVAILABLE",
      "priorityScore": 50,
      "workloadPercentage": 50.0,
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ]
}
```

---

### 2.4 Update Agent Status
**Endpoint**: `PATCH /agent-workload/{agentId}/status`

**Request Body**:
```json
{
  "status": "BUSY"
}
```

**Response**: Same as create/update response

---

## 3. Conversation Routing

### 3.1 Route Conversation
**Endpoint**: `POST /route-conversation`

**Request Body**:
```json
{
  "conversationId": 100,
  "context": {
    "customerTier": "VIP",
    "language": "en",
    "age": 25,
    "status": "open"
  }
}
```

**Response**:
```json
{
  "conversationId": 100,
  "assigned": true,
  "assignedAgentId": 10,
  "assignedTeamId": 20,
  "routingRuleId": 1,
  "routingStrategy": "SKILL_BASED",
  "assignedAt": "2024-01-15T10:30:00",
  "reason": "Routing rule matched: VIP Customer Routing"
}
```

**Context Fields**:
- `customerTier`: Customer tier (VIP, Enterprise, Standard)
- `language`: Language code (en, vi, etc.)
- `age`: Customer age (for comparison operators)
- `status`: Conversation status
- Custom fields can be added as needed

---

### 3.2 Get Routing History
**Endpoint**: `GET /routing-history/{conversationId}`

**Response**:
```json
{
  "history": [
    {
      "id": 1,
      "conversationId": 100,
      "tenantId": 1,
      "assignedAgentId": 10,
      "assignedTeamId": 20,
      "routingRuleId": 1,
      "routingStrategy": "SKILL_BASED",
      "context": {
        "customerTier": "VIP",
        "language": "en"
      },
      "assignedAt": "2024-01-15T10:30:00",
      "createdAt": "2024-01-15T10:30:00"
    }
  ]
}
```

---

## 4. Team Workload Management

### 4.1 Get Team Workload
**Endpoint**: `GET /team-workload/{teamId}`

**Response**:
```json
{
  "id": 1,
  "teamId": 20,
  "tenantId": 1,
  "totalAgents": 5,
  "activeAgents": 4,
  "totalActiveConversations": 15,
  "totalMaxConversations": 50,
  "averageWorkloadPercentage": 30.0,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

---

### 4.2 List All Team Workloads
**Endpoint**: `GET /team-workload`

**Response**:
```json
{
  "workloads": [
    {
      "id": 1,
      "teamId": 20,
      "tenantId": 1,
      "totalAgents": 5,
      "activeAgents": 4,
      "totalActiveConversations": 15,
      "totalMaxConversations": 50,
      "averageWorkloadPercentage": 30.0,
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ]
}
```

---

## 5. Supervisor Sessions (Existing)

### 5.1 Create Supervisor Session
**Endpoint**: `POST /supervisor-sessions`

**Request Body**:
```json
{
  "supervisorId": 5,
  "conversationId": 100,
  "mode": "MONITOR"
}
```

**Response**:
```json
{
  "id": 1,
  "supervisorId": 5,
  "conversationId": 100,
  "tenantId": 1,
  "mode": "MONITOR",
  "joinedAt": "2024-01-15T10:30:00",
  "leftAt": null,
  "notes": null
}
```

**Supervisor Modes**:
- `MONITOR` - Supervisor can only view conversation
- `COACH` - Supervisor can send coaching messages to agent
- `TAKEOVER` - Supervisor takes control of conversation

---

### 5.2 Get All Supervisor Sessions
**Endpoint**: `GET /supervisor-sessions`

**Query Parameters**:
- `supervisorId` (optional): Filter by supervisor
- `conversationId` (optional): Filter by conversation
- `mode` (optional): Filter by mode

**Response**:
```json
{
  "sessions": [
    {
      "id": 1,
      "supervisorId": 5,
      "conversationId": 100,
      "tenantId": 1,
      "mode": "MONITOR",
      "joinedAt": "2024-01-15T10:30:00",
      "leftAt": null,
      "notes": null
    }
  ]
}
```

---

### 5.3 End Supervisor Session
**Endpoint**: `DELETE /supervisor-sessions/{id}`

**Response**: `204 No Content`

---

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid request body",
  "path": "/api/routing/routing-rules"
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Missing or invalid tenant key",
  "path": "/api/routing/routing-rules"
}
```

### 404 Not Found
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Routing rule not found",
  "path": "/api/routing/routing-rules/999"
}
```

### 500 Internal Server Error
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "path": "/api/routing/route-conversation"
}
```

---

## Rate Limiting
- **Default**: 100 requests per minute per tenant
- **Burst**: 200 requests per minute per tenant
- **Headers**: `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`

---

## Versioning
- **Current Version**: v1
- **Version Header**: `X-API-Version: 1`
- **Backward Compatibility**: Maintained for minor versions

---

## Changelog
- **v1.0** (2024-01-15): Initial Phase 4 API release
  - Routing rules CRUD operations
  - Agent workload management
  - Conversation routing
  - Team workload tracking
  - Supervisor sessions
