# Saga Pattern Implementation

## Overview

The chatbot SaaS platform uses the Saga pattern to manage distributed transactions across multiple hubs. This ensures data consistency and provides reliable compensation mechanisms for long-running business processes.

## Saga Pattern Architecture

### Components
```
┌─────────────────────────────────────────────────────────────┐
│                    Saga Manager                             │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │   Saga      │  │   Saga      │  │   Saga      │         │
│  │ Definition  │  │ Instance    │  │ Step        │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                Compensation Actions                         │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │   Rollback  │  │   Retry     │  │   Timeout   │         │
│  │   Actions   │  │   Logic     │  │   Handling  │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
└─────────────────────────────────────────────────────────────┘
```

### Saga Types
1. **Choreography-Based**: Each service publishes events that trigger the next step
2. **Orchestration-Based**: Central coordinator manages the entire saga

## Saga Definitions

### User Registration Saga
```java
@Component
public class UserRegistrationSaga {
    
    @SagaDefinition("user-registration")
    public SagaDefinition createUserRegistrationSaga() {
        return SagaDefinition.builder()
            .sagaType("USER_REGISTRATION")
            .description("Complete user registration process")
            .steps(Arrays.asList(
                // Step 1: Create user in Identity Hub
                SagaStep.builder()
                    .stepName("create-user-identity")
                    .service("identity-service")
                    .action("CREATE_USER")
                    .compensation("DELETE_USER")
                    .timeout(Duration.ofSeconds(30))
                    .retryPolicy(RetryPolicy.exponential(3, Duration.ofSeconds(1)))
                    .build(),
                
                // Step 2: Create user profile in User Hub
                SagaStep.builder()
                    .stepName("create-user-profile")
                    .service("user-service")
                    .action("CREATE_USER_PROFILE")
                    .compensation("DELETE_USER_PROFILE")
                    .timeout(Duration.ofSeconds(30))
                    .retryPolicy(RetryPolicy.exponential(3, Duration.ofSeconds(1)))
                    .build(),
                
                // Step 3: Create default wallet in Wallet Hub
                SagaStep.builder()
                    .stepName("create-default-wallet")
                    .service("wallet-service")
                    .action("CREATE_WALLET")
                    .compensation("DELETE_WALLET")
                    .timeout(Duration.ofSeconds(30))
                    .retryPolicy(RetryPolicy.exponential(3, Duration.ofSeconds(1)))
                    .build(),
                
                // Step 4: Send welcome notification
                SagaStep.builder()
                    .stepName("send-welcome-notification")
                    .service("notification-service")
                    .action("SEND_WELCOME_EMAIL")
                    .compensation("SEND_CANCELLATION_EMAIL")
                    .timeout(Duration.ofSeconds(60))
                    .retryPolicy(RetryPolicy.fixed(3, Duration.ofSeconds(5)))
                    .build()
            ))
            .build();
    }
}
```

### App Subscription Saga
```java
@Component
public class AppSubscriptionSaga {
    
    @SagaDefinition("app-subscription")
    public SagaDefinition createAppSubscriptionSaga() {
        return SagaDefinition.builder()
            .sagaType("APP_SUBSCRIPTION")
            .description("Subscribe tenant to application")
            .steps(Arrays.asList(
                // Step 1: Validate tenant access
                SagaStep.builder()
                    .stepName("validate-tenant-access")
                    .service("tenant-service")
                    .action("VALIDATE_TENANT_ACCESS")
                    .timeout(Duration.ofSeconds(10))
                    .build(),
                
                // Step 2: Check billing entitlement
                SagaStep.builder()
                    .stepName("check-billing-entitlement")
                    .service("billing-service")
                    .action("CHECK_ENTITLEMENT")
                    .timeout(Duration.ofSeconds(10))
                    .build(),
                
                // Step 3: Create subscription
                SagaStep.builder()
                    .stepName("create-subscription")
                    .service("app-service")
                    .action("CREATE_SUBSCRIPTION")
                    .compensation("CANCEL_SUBSCRIPTION")
                    .timeout(Duration.ofSeconds(30))
                    .build(),
                
                // Step 4: Configure app
                SagaStep.builder()
                    .stepName("configure-app")
                    .service("app-service")
                    .action("CONFIGURE_APP")
                    .compensation("RESET_APP_CONFIG")
                    .timeout(Duration.ofSeconds(30))
                    .build(),
                
                // Step 5: Activate subscription
                SagaStep.builder()
                    .stepName("activate-subscription")
                    .service("app-service")
                    .action("ACTIVATE_SUBSCRIPTION")
                    .compensation("DEACTIVATE_SUBSCRIPTION")
                    .timeout(Duration.ofSeconds(10))
                    .build()
            ))
            .build();
    }
}
```

### Wallet Transaction Saga
```java
@Component
public class WalletTransactionSaga {
    
    @SagaDefinition("wallet-transaction")
    public SagaDefinition createWalletTransactionSaga() {
        return SagaDefinition.builder()
            .sagaType("WALLET_TRANSACTION")
            .description("Process wallet transaction with double-entry")
            .steps(Arrays.asList(
                // Step 1: Validate wallet
                SagaStep.builder()
                    .stepName("validate-wallet")
                    .service("wallet-service")
                    .action("VALIDATE_WALLET")
                    .timeout(Duration.ofSeconds(10))
                    .build(),
                
                // Step 2: Reserve funds
                SagaStep.builder()
                    .stepName("reserve-funds")
                    .service("wallet-service")
                    .action("RESERVE_FUNDS")
                    .compensation("RELEASE_FUNDS")
                    .timeout(Duration.ofSeconds(30))
                    .build(),
                
                // Step 3: Process payment
                SagaStep.builder()
                    .stepName("process-payment")
                    .service("payment-service")
                    .action("PROCESS_PAYMENT")
                    .compensation("REFUND_PAYMENT")
                    .timeout(Duration.ofSeconds(60))
                    .build(),
                
                // Step 4: Create ledger entries
                SagaStep.builder()
                    .stepName("create-ledger-entries")
                    .service("wallet-service")
                    .action("CREATE_LEDGER_ENTRIES")
                    .compensation("REVERSE_LEDGER_ENTRIES")
                    .timeout(Duration.ofSeconds(30))
                    .build(),
                
                // Step 5: Update wallet balance
                SagaStep.builder()
                    .stepName("update-wallet-balance")
                    .service("wallet-service")
                    .action("UPDATE_WALLET_BALANCE")
                    .compensation("RESTORE_WALLET_BALANCE")
                    .timeout(Duration.ofSeconds(30))
                    .build()
            ))
            .build();
    }
}
```

## Saga Manager Implementation

### Core Saga Manager
```java
@Service
@Transactional
public class SagaManager {
    
    private final SagaDefinitionRepository sagaDefinitionRepository;
    private final SagaInstanceRepository sagaInstanceRepository;
    private final MessagePublisher messagePublisher;
    private final SagaOrchestrator orchestrator;
    
    @Autowired
    public SagaManager(SagaDefinitionRepository sagaDefinitionRepository,
                      SagaInstanceRepository sagaInstanceRepository,
                      MessagePublisher messagePublisher,
                      SagaOrchestrator orchestrator) {
        this.sagaDefinitionRepository = sagaDefinitionRepository;
        this.sagaInstanceRepository = sagaInstanceRepository;
        this.messagePublisher = messagePublisher;
        this.orchestrator = orchestrator;
    }
    
    public String startSaga(String sagaType, Map<String, Object> sagaData) {
        // Get saga definition
        SagaDefinition definition = sagaDefinitionRepository
            .findBySagaType(sagaType)
            .orElseThrow(() -> new SagaException("Saga definition not found: " + sagaType));
        
        // Create saga instance
        SagaInstance instance = SagaInstance.builder()
            .id(UUID.randomUUID().toString())
            .sagaType(sagaType)
            .status(SagaStatus.STARTED)
            .currentStep(0)
            .sagaData(sagaData)
            .createdAt(Instant.now())
            .build();
        
        instance = sagaInstanceRepository.save(instance);
        
        // Start first step
        executeNextStep(instance);
        
        return instance.getId();
    }
    
    @EventListener
    public void handleSagaStepCompleted(SagaStepCompletedEvent event) {
        SagaInstance instance = sagaInstanceRepository.findById(event.getSagaInstanceId())
            .orElseThrow(() -> new SagaException("Saga instance not found"));
        
        // Update instance
        instance.setCurrentStep(instance.getCurrentStep() + 1);
        instance.setStepData(event.getStepData());
        
        if (instance.getCurrentStep() >= getSteps(instance).size()) {
            // Saga completed
            instance.setStatus(SagaStatus.COMPLETED);
            instance.setCompletedAt(Instant.now());
            sagaInstanceRepository.save(instance);
            
            // Publish completion event
            messagePublisher.publish(SagaCompletedEvent.builder()
                .sagaInstanceId(instance.getId())
                .sagaType(instance.getSagaType())
                .sagaData(instance.getSagaData())
                .build());
        } else {
            // Execute next step
            sagaInstanceRepository.save(instance);
            executeNextStep(instance);
        }
    }
    
    @EventListener
    public void handleSagaStepFailed(SagaStepFailedEvent event) {
        SagaInstance instance = sagaInstanceRepository.findById(event.getSagaInstanceId())
            .orElseThrow(() -> new SagaException("Saga instance not found"));
        
        // Check if we should retry
        SagaStep currentStep = getCurrentStep(instance);
        if (shouldRetry(instance, currentStep, event)) {
            // Retry the step
            executeStep(instance, currentStep);
        } else {
            // Start compensation
            instance.setStatus(SagaStatus.COMPENSATING);
            sagaInstanceRepository.save(instance);
            startCompensation(instance, event);
        }
    }
    
    private void executeNextStep(SagaInstance instance) {
        List<SagaStep> steps = getSteps(instance);
        if (instance.getCurrentStep() < steps.size()) {
            SagaStep currentStep = steps.get(instance.getCurrentStep());
            executeStep(instance, currentStep);
        }
    }
    
    private void executeStep(SagaInstance instance, SagaStep step) {
        // Create step execution
        StepExecution execution = StepExecution.builder()
            .id(UUID.randomUUID().toString())
            .sagaInstanceId(instance.getId())
            .stepName(step.getStepName())
            .status(StepStatus.EXECUTING)
            .startedAt(Instant.now())
            .build();
        
        // Publish step execution event
        messagePublisher.publish(ExecuteStepCommand.builder()
            .executionId(execution.getId())
            .sagaInstanceId(instance.getId())
            .stepName(step.getStepName())
            .service(step.getService())
            .action(step.getAction())
            .sagaData(instance.getSagaData())
            .stepData(instance.getStepData())
            .timeout(step.getTimeout())
            .build());
    }
    
    private void startCompensation(SagaInstance instance, SagaStepFailedEvent event) {
        List<SagaStep> completedSteps = getCompletedSteps(instance);
        
        // Execute compensations in reverse order
        Collections.reverse(completedSteps);
        
        for (SagaStep step : completedSteps) {
            if (step.getCompensation() != null) {
                executeCompensation(instance, step);
            }
        }
    }
    
    private void executeCompensation(SagaInstance instance, SagaStep step) {
        messagePublisher.publish(ExecuteCompensationCommand.builder()
            .sagaInstanceId(instance.getId())
            .stepName(step.getStepName())
            .service(step.getService())
            .action(step.getCompensation())
            .sagaData(instance.getSagaData())
            .stepData(instance.getStepData())
            .build());
    }
    
    private boolean shouldRetry(SagaInstance instance, SagaStep step, SagaStepFailedEvent event) {
        RetryPolicy retryPolicy = step.getRetryPolicy();
        if (retryPolicy == null) {
            return false;
        }
        
        int attemptCount = instance.getRetryAttempts().getOrDefault(step.getStepName(), 0);
        return attemptCount < retryPolicy.getMaxAttempts();
    }
}
```

### Saga Orchestrator
```java
@Service
public class SagaOrchestrator {
    
    private final Map<String, ServiceClient> serviceClients;
    
    @EventListener
    @Async
    public void handleExecuteStepCommand(ExecuteStepCommand command) {
        try {
            ServiceClient client = serviceClients.get(command.getService());
            if (client == null) {
                throw new SagaException("Service client not found: " + command.getService());
            }
            
            // Execute step with timeout
            CompletableFuture<StepResult> future = CompletableFuture
                .supplyAsync(() -> client.execute(command))
                .orTimeout(command.getTimeout().toMillis(), TimeUnit.MILLISECONDS);
            
            StepResult result = future.get();
            
            // Publish success event
            messagePublisher.publish(SagaStepCompletedEvent.builder()
                .sagaInstanceId(command.getSagaInstanceId())
                .stepName(command.getStepName())
                .stepData(result.getData())
                .build());
                
        } catch (TimeoutException e) {
            // Handle timeout
            messagePublisher.publish(SagaStepFailedEvent.builder()
                .sagaInstanceId(command.getSagaInstanceId())
                .stepName(command.getStepName())
                .errorType("TIMEOUT")
                .errorMessage("Step execution timed out")
                .build());
                
        } catch (Exception e) {
            // Handle other errors
            messagePublisher.publish(SagaStepFailedEvent.builder()
                .sagaInstanceId(command.getSagaInstanceId())
                .stepName(command.getStepName())
                .errorType("EXECUTION_ERROR")
                .errorMessage(e.getMessage())
                .build());
        }
    }
    
    @EventListener
    @Async
    public void handleExecuteCompensationCommand(ExecuteCompensationCommand command) {
        try {
            ServiceClient client = serviceClients.get(command.getService());
            if (client == null) {
                throw new SagaException("Service client not found: " + command.getService());
            }
            
            // Execute compensation
            client.compensate(command);
            
            // Publish compensation completed event
            messagePublisher.publish(SagaCompensationCompletedEvent.builder()
                .sagaInstanceId(command.getSagaInstanceId())
                .stepName(command.getStepName())
                .build());
                
        } catch (Exception e) {
            // Log compensation failure but don't stop the process
            log.error("Compensation failed for saga {} step {}", 
                command.getSagaInstanceId(), command.getStepName(), e);
                
            messagePublisher.publish(SagaCompensationFailedEvent.builder()
                .sagaInstanceId(command.getSagaInstanceId())
                .stepName(command.getStepName())
                .errorMessage(e.getMessage())
                .build());
        }
    }
}
```

## Data Models

### Saga Definition
```java
@Entity
@Table(name = "saga_definitions")
public class SagaDefinition {
    
    @Id
    private String id;
    
    @Column(unique = true, nullable = false)
    private String sagaType;
    
    private String description;
    
    @OneToMany(mappedBy = "sagaDefinition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SagaStep> steps;
    
    private boolean active;
    
    private Instant createdAt;
    private Instant updatedAt;
    
    // Getters and setters
}
```

### Saga Instance
```java
@Entity
@Table(name = "saga_instances")
public class SagaInstance {
    
    @Id
    private String id;
    
    @Column(nullable = false)
    private String sagaType;
    
    @Enumerated(EnumType.STRING)
    private SagaStatus status;
    
    private int currentStep;
    
    @Type(type = "jsonb")
    private Map<String, Object> sagaData;
    
    @Type(type = "jsonb")
    private Map<String, Object> stepData;
    
    @Type(type = "jsonb")
    private Map<String, Integer> retryAttempts;
    
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;
    
    // Getters and setters
}
```

### Saga Step
```java
@Entity
@Table(name = "saga_steps")
public class SagaStep {
    
    @Id
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saga_definition_id")
    private SagaDefinition sagaDefinition;
    
    @Column(nullable = false)
    private String stepName;
    
    @Column(nullable = false)
    private String service;
    
    @Column(nullable = false)
    private String action;
    
    private String compensation;
    
    @Column(nullable = false)
    private Duration timeout;
    
    @Type(type = "jsonb")
    private RetryPolicy retryPolicy;
    
    private int stepOrder;
    
    // Getters and setters
}
```

## Events and Commands

### Commands
```java
@Data
@Builder
public class ExecuteStepCommand {
    private String executionId;
    private String sagaInstanceId;
    private String stepName;
    private String service;
    private String action;
    private Map<String, Object> sagaData;
    private Map<String, Object> stepData;
    private Duration timeout;
}

@Data
@Builder
public class ExecuteCompensationCommand {
    private String sagaInstanceId;
    private String stepName;
    private String service;
    private String action;
    private Map<String, Object> sagaData;
    private Map<String, Object> stepData;
}
```

### Events
```java
@Data
@Builder
public class SagaStepCompletedEvent {
    private String sagaInstanceId;
    private String stepName;
    private Map<String, Object> stepData;
}

@Data
@Builder
public class SagaStepFailedEvent {
    private String sagaInstanceId;
    private String stepName;
    private String errorType;
    private String errorMessage;
}

@Data
@Builder
public class SagaCompletedEvent {
    private String sagaInstanceId;
    private String sagaType;
    private Map<String, Object> sagaData;
}
```

## Retry Policies

### Retry Policy Configuration
```java
public class RetryPolicy {
    
    public enum Type {
        FIXED, EXPONENTIAL, LINEAR
    }
    
    private Type type;
    private int maxAttempts;
    private Duration initialDelay;
    private double backoffMultiplier;
    
    public static RetryPolicy fixed(int maxAttempts, Duration delay) {
        return RetryPolicy.builder()
            .type(Type.FIXED)
            .maxAttempts(maxAttempts)
            .initialDelay(delay)
            .build();
    }
    
    public static RetryPolicy exponential(int maxAttempts, Duration initialDelay) {
        return RetryPolicy.builder()
            .type(Type.EXPONENTIAL)
            .maxAttempts(maxAttempts)
            .initialDelay(initialDelay)
            .backoffMultiplier(2.0)
            .build();
    }
    
    public Duration getDelay(int attempt) {
        switch (type) {
            case FIXED:
                return initialDelay;
            case EXPONENTIAL:
                return Duration.ofMillis(
                    (long) (initialDelay.toMillis() * Math.pow(backoffMultiplier, attempt - 1))
                );
            case LINEAR:
                return Duration.ofMillis(initialDelay.toMillis() * attempt);
            default:
                return initialDelay;
        }
    }
}
```

## Monitoring and Observability

### Saga Metrics
```java
@Component
public class SagaMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter sagaStartedCounter;
    private final Counter sagaCompletedCounter;
    private final Counter sagaFailedCounter;
    private final Timer sagaExecutionTimer;
    
    public SagaMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.sagaStartedCounter = Counter.builder("saga.started")
            .description("Number of sagas started")
            .register(meterRegistry);
        this.sagaCompletedCounter = Counter.builder("saga.completed")
            .description("Number of sagas completed")
            .register(meterRegistry);
        this.sagaFailedCounter = Counter.builder("saga.failed")
            .description("Number of sagas failed")
            .register(meterRegistry);
        this.sagaExecutionTimer = Timer.builder("saga.execution.time")
            .description("Saga execution time")
            .register(meterRegistry);
    }
    
    public void recordSagaStarted(String sagaType) {
        sagaStartedCounter.increment(Tags.of("type", sagaType));
    }
    
    public void recordSagaCompleted(String sagaType, Duration duration) {
        sagaCompletedCounter.increment(Tags.of("type", sagaType));
        sagaExecutionTimer.record(duration, Tags.of("type", sagaType));
    }
    
    public void recordSagaFailed(String sagaType, String errorType) {
        sagaFailedCounter.increment(Tags.of("type", sagaType, "error", errorType));
    }
}
```

This Saga pattern implementation provides reliable distributed transaction management with proper compensation, retry logic, and monitoring capabilities for the chatbot SaaS platform.
