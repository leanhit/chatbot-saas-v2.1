# Architecture Improvements - Spokes Module Decoupling

## Current State Analysis

### 1. Package Structure - ✅ FIXED
- **Issue**: Duplicate packages `com.chatbot.config` (2 files) and `com.chatbot.configs` (23 files)
- **Solution**: Merged `com.chatbot.config` into `com.chatbot.configs`
- **Status**: Completed
  - Moved `AsyncConfig.java` to `com.chatbot.configs`
  - Moved `KafkaConfig.java` to `com.chatbot.configs`
  - Updated imports in `FacebookKafkaProducer.java` and `FacebookEventConsumer.java`
  - Deleted old `com.chatbot.config` directory

### 2. Spokes Module Coupling - ANALYZED

#### Current Coupling Issues

**HubDatabaseConfig** registers Spokes entities directly into shared EntityManagerFactory:
```java
@EnableJpaRepositories(
    basePackages = {
        "com.chatbot.spokes.odoo.repository",
        "com.chatbot.spokes.facebook.repository",
        "com.chatbot.spokes.facebook.connection.repository",
        "com.chatbot.spokes.minio.repository",
        // ...
    },
    entityManagerFactoryRef = "sharedEntityManagerFactory"
)
```

**Cross-Spoke Dependencies**:
- `CustomerDataService` (Odoo spoke) uses `FacebookConnectionRepository` (Facebook spoke)
- Direct JPA entity access across spoke boundaries
- Tight coupling between integration modules

#### Problems
1. **High Coupling**: Spokes directly depend on each other's entities and repositories
2. **Database Schema Coupling**: Changes in one spoke's schema affect others
3. **Testing Difficulty**: Hard to test spokes in isolation
4. **Scalability Issues**: Cannot scale spokes independently
5. **Maintenance Burden**: Changes ripple across multiple spokes

## Proposed DTO/Event-Driven Architecture

### Design Principles
1. **Spoke Independence**: Each spoke should be self-contained
2. **Event-Driven Communication**: Use Kafka for inter-spoke communication
3. **DTO Boundaries**: Spokes expose DTOs, not entities
4. **Async Processing**: Events processed asynchronously

### Architecture Layers

```
┌─────────────────────────────────────────────────────────┐
│                    Core Domain                           │
│  (Conversation, Message, Tenant, User, etc.)             │
└────────────────────┬────────────────────────────────────┘
                     │ Kafka Events
                     ▼
┌─────────────────────────────────────────────────────────┐
│                   Event Bus (Kafka)                      │
│  - facebook.events                                       │
│  - odoo.events                                           │
│  - minio.events                                          │
└────────────────────┬────────────────────────────────────┘
                     │
        ┌────────────┴────────────┐
        ▼                         ▼
┌──────────────┐          ┌──────────────┐
│  Facebook    │          │    Odoo      │
│    Spoke     │          │    Spoke     │
│              │          │              │
│ - DTOs       │          │ - DTOs       │
│ - Services   │          │ - Services   │
│ - Repos      │          │ - Repos      │
└──────────────┘          └──────────────┘
```

### Implementation Plan

#### Phase 1: Create DTO Layer
- Create DTOs for each spoke's public API
- Map entities to DTOs at spoke boundaries
- Remove direct entity access from other spokes

#### Phase 2: Define Kafka Events
```java
// Facebook Events
public record FacebookConnectionCreatedEvent(
    UUID connectionId,
    Long tenantId,
    String pageId,
    String ownerId
) {}

public record FacebookMessageReceivedEvent(
    UUID connectionId,
    String senderId,
    String message,
    LocalDateTime timestamp
) {}

// Odoo Events
public record CustomerDataExtractedEvent(
    String senderId,
    String phone,
    String email,
    String ownerId
) {}
```

#### Phase 3: Event Producers
- Facebook spoke produces events for connection changes, messages
- Odoo spoke produces events for customer data updates
- MinIO spoke produces events for file uploads

#### Phase 4: Event Consumers
- Core domain consumes spoke events
- Spokes consume events from other spokes via DTOs
- Remove direct repository dependencies

#### Phase 5: Separate Database Configs
- Create separate EntityManagerFactory for each spoke
- Each spoke manages its own database schema
- Core domain has its own EntityManagerFactory

### Migration Strategy

1. **Dual-Write Phase**: Keep current implementation + add event publishing
2. **Consumer Phase**: Add event consumers, validate data consistency
3. **Cutover Phase**: Switch consumers to use event data instead of direct DB access
4. **Cleanup Phase**: Remove direct dependencies, clean up old code

### Benefits
- **Loose Coupling**: Spokes communicate via events only
- **Independent Scaling**: Each spoke can scale independently
- **Schema Isolation**: Database changes isolated to individual spokes
- **Better Testing**: Spokes can be tested with mock events
- **Easier Maintenance**: Changes in one spoke don't affect others

### Estimated Effort
- **Phase 1 (DTOs)**: 2-3 days
- **Phase 2 (Events)**: 1-2 days
- **Phase 3 (Producers)**: 2-3 days
- **Phase 4 (Consumers)**: 3-4 days
- **Phase 5 (DB Configs)**: 2-3 days
- **Testing & Migration**: 5-7 days

**Total**: 15-22 days

### Priority
**LOW/FUTURE** - This is a significant architectural refactoring that requires careful planning and testing. Should be done when:
- Team has capacity for major refactoring
- System is stable with current architecture
- Business requirements demand better scalability
- Before adding new spokes

## Summary

### Completed
- ✅ Package structure consolidation (config → configs)

### Recommended (Future)
- DTO/Event-driven architecture for Spokes decoupling
- Separate database configurations per spoke
- Kafka-based inter-spoke communication

### Risk Assessment
- **Current**: Medium risk due to tight coupling
- **After Refactoring**: Low risk, better maintainability
- **Migration Risk**: High during transition period (requires careful testing)
