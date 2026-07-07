# Chatbot SaaS v2.1 Architecture

This document describes the high-level architecture of the Chatbot SaaS platform.

## High Level Overview

The system is designed as a modular monolith transitioning towards a **Hub and Spoke** microservices architecture using **gRPC** for inter-service communication and **RabbitMQ/Kafka** for asynchronous event-driven flows.

### 1. Hub & Spoke Topology (gRPC)
The architecture organizes domain boundaries into "Hubs" and "Spokes":
- **Hubs**: Core domain services that hold state and orchestrate complex business rules (e.g., `Identity Hub`, `Tenant Hub`).
- **Spokes**: Edge services that interact with external integrations or specific focused tasks (e.g., `Facebook Spoke`, `Notification Spoke`).

**Communication:**
- Sync communication: **gRPC** with Protobuf definitions.
- Async communication: **RabbitMQ** (Events like `ConversationCreated`, `PaymentCompleted`).

### 2. Core Modules (Domain-Driven Design)
- **Tenant Module (`com.chatbot.core.tenant`)**: Handles multi-tenancy, SaaS packages, tenant members, and permissions.
- **Identity Module (`com.chatbot.core.identity`)**: Authentication, JWT token validation, user sessions.
- **Message Store Module (`com.chatbot.core.message.store`)**: Handles conversations, metrics, and routing (including AI escalations).
- **Simple Payment Module (`com.chatbot.core.simplepayment`)**: Handles subscription payments via QR Code bank transfers.

### 3. Database Architecture
- **PostgreSQL**: Primary transactional database.
- **Redis**: Multi-level caching (via Spring Cache) and rate limiting.
- **MinIO/S3**: Object storage for tenant logos, conversation attachments.

### 4. Observability
- **Micrometer + Zipkin**: Distributed tracing via `TraceID` and `SpanID` across HTTP and gRPC calls.
- **Resilience4j**: Circuit breakers on external calls (e.g., Facebook API, LLM API, Slack Webhooks).
- **Prometheus + Actuator**: Exposing health and JVM metrics.

## Request Flow Example (Takeover Conversation)
1. Frontend sends `POST /api/conversations/{id}/takeover`.
2. `ConversationController` intercepts and delegates to `ConversationService`.
3. `ConversationService` validates permissions and updates `isTakenOverByAgent = true`.
4. Service publishes a `ConversationStatusChanged` event via ApplicationEventPublisher/RabbitMQ.
5. Botpress Spoke receives event and silences the AI bot for this specific conversation.
6. Notification Spoke receives event and alerts the Slack channel via `SlackNotificationService`.
