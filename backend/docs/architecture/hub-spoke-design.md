# Hub & Spoke Architecture Design

## Overview
The chatbot SaaS platform follows a Hub & Spoke architecture pattern to achieve:
- **Scalability**: Independent scaling of hubs and spokes
- **Resilience**: Isolation prevents cascading failures
- **Maintainability**: Clear separation of concerns
- **Flexibility**: Easy addition of new integrations

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    API Gateway                              │
│                 (Load Balancer)                            │
└─────────────────────┬───────────────────────────────────────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
┌───────▼──────┐ ┌────▼────┐ ┌─────▼──────┐
│  Web Client  │ │ Mobile  │ │ External   │
│              │ │ Client  │ │ Systems    │
└──────────────┘ └─────────┘ └────────────┘
                      │
        ┌─────────────▼─────────────────┐
        │        Message Hub           │
        │    (Single Decision Point)   │
        └─────────────┬─────────────────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
┌───────▼──────┐ ┌───▼────┐ ┌─────▼──────┐
│ Identity Hub │ │User Hub│ │Tenant Hub  │
└──────────────┘ └────────┘ └────────────┘
        │             │             │
        └─────────────┼─────────────┘
                      │
        ┌─────────────▼─────────────────┐
        │         Shared Services       │
        │  (Security, Utils, Saga, etc) │
        └─────────────┬─────────────────┘
                      │
        ┌─────────────▼─────────────────┐
        │          Spokes              │
        │  Facebook, Botpress, Odoo   │
        └──────────────────────────────┘
```

## Core Hubs

### Identity Hub
- **Purpose**: Authentication and authorization
- **Database**: `chatbot_identity_db`
- **Key Features**: JWT tokens, user validation
- **gRPC Port**: 50051

### User Hub
- **Purpose**: User profile management
- **Database**: `chatbot_user_db`
- **Key Features**: Profiles, preferences, analytics
- **gRPC Port**: 50052

### Tenant Hub
- **Purpose**: Multi-tenant workspace management
- **Database**: `chatbot_tenant_db`
- **Key Features**: Tenants, memberships, roles
- **gRPC Port**: 50053

### App Hub
- **Purpose**: Application registry and subscriptions
- **Database**: `chatbot_app_db`
- **Key Features**: App management, guards, subscriptions
- **gRPC Port**: 50054

### Billing Hub
- **Purpose**: Billing and entitlement management
- **Database**: `chatbot_billing_db`
- **Key Features**: READ-ONLY billing data, entitlements
- **gRPC Port**: 50055

### Wallet Hub
- **Purpose**: Financial transactions
- **Database**: `chatbot_wallet_db`
- **Key Features**: Wallets, transactions, ledger
- **gRPC Port**: 50056

### Config Hub
- **Purpose**: Runtime configuration
- **Database**: `chatbot_config_db`
- **Key Features**: Dynamic config, environment settings
- **gRPC Port**: 50057

### Message Hub
- **Purpose**: Message routing and processing
- **Database**: `chatbot_message_db`
- **Key Features**: Single decision point routing
- **gRPC Port**: 50058

## Spokes (External Integrations)

### Facebook Spoke
- **Purpose**: Facebook Messenger integration
- **Features**: Webhooks, user management, auto-connect
- **Communication**: REST API + Webhooks

### Botpress Spoke
- **Purpose**: Botpress chatbot integration
- **Features**: API integration, user mapping, authentication
- **Communication**: REST API

### Odoo Spoke
- **Purpose**: ERP system integration
- **Features**: Customer data, phone capture, staging
- **Communication**: XML-RPC API

### MinIO Spoke
- **Purpose**: File storage and processing
- **Features**: Object storage, image processing, metadata
- **Communication**: S3-compatible API

## Communication Patterns

### Synchronous Communication
- **gRPC**: Inter-hub communication
- **REST API**: External client communication
- **Response Time**: < 100ms for intra-hub calls

### Asynchronous Communication
- **Message Queues**: Event-driven architecture
- **Saga Pattern**: Distributed transactions
- **Event Sourcing**: Audit trails and replay capability

## Data Flow

1. **Request Ingestion**: API Gateway routes to appropriate hub
2. **Authentication**: Identity Hub validates JWT token
3. **Authorization**: Tenant Hub checks permissions
4. **Processing**: Business logic in respective hub
5. **Integration**: Spokes handle external system calls
6. **Response**: Results flow back through message hub

## Scaling Strategy

### Horizontal Scaling
- Each hub can be scaled independently
- Database sharding per hub
- Load balancing at hub level

### Vertical Scaling
- Resource allocation based on hub workload
- Memory-optimized for message hub
- CPU-optimized for processing hubs

## Fault Tolerance

### Circuit Breakers
- Inter-hub communication protection
- Automatic failover mechanisms
- Graceful degradation

### Data Consistency
- Eventual consistency for cross-hub data
- Strong consistency within hub boundaries
- Compensation transactions via Saga

## Security Model

### Authentication
- JWT-based authentication
- Token refresh mechanism
- Multi-factor authentication support

### Authorization
- Role-based access control (RBAC)
- Tenant-level isolation
- API-level permissions

### Data Protection
- Encryption at rest and in transit
- PII masking and anonymization
- Audit logging for compliance

## Monitoring & Observability

### Metrics
- Per-hub performance metrics
- Inter-hub communication latency
- Business KPI tracking

### Logging
- Structured logging with correlation IDs
- Centralized log aggregation
- Real-time log analysis

### Tracing
- Distributed tracing across hubs
- Request flow visualization
- Performance bottleneck identification

## Deployment Architecture

### Container Strategy
- One container per hub
- Sidecar patterns for cross-cutting concerns
- Health checks and readiness probes

### Orchestration
- Kubernetes for container orchestration
- Helm charts for deployment management
- GitOps for deployment automation

### Environment Management
- Separate namespaces per environment
- Configuration externalization
- Blue-green deployment strategy
