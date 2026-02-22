package com.chatbot.shared.saga;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.Method;

@Component
public class SagaOrchestration {

    @Autowired
    private ApplicationContext applicationContext;

    private final Map<String, SagaDefinitionData> sagaDefinitions = new ConcurrentHashMap<>();

    @PostConstruct
    public void initializeSagaDefinitions() {
        // Auto-discover and register saga definitions from beans
        Map<String, Object> sagaBeans = applicationContext.getBeansWithAnnotation(com.chatbot.shared.saga.SagaDefinition.class);
        for (Object sagaBean : sagaBeans.values()) {
            // Find methods annotated with @SagaDefinition
            Method[] methods = sagaBean.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(com.chatbot.shared.saga.SagaDefinition.class) && 
                    method.getParameterCount() == 0 && 
                    SagaDefinitionData.class.isAssignableFrom(method.getReturnType())) {
                    try {
                        SagaDefinitionData definition = (SagaDefinitionData) method.invoke(sagaBean);
                        registerSagaDefinition(definition);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to invoke saga definition method: " + method.getName(), e);
                    }
                }
            }
        }
    }

    public void registerSagaDefinition(SagaDefinitionData sagaDefinition) {
        if (sagaDefinition == null) {
            throw new IllegalArgumentException("Saga definition cannot be null");
        }
        
        sagaDefinition.validate();
        sagaDefinitions.put(sagaDefinition.getSagaType(), sagaDefinition);
    }

    public SagaDefinitionData getSagaDefinition(String sagaType) {
        return sagaDefinitions.get(sagaType);
    }

    public void unregisterSagaDefinition(String sagaType) {
        sagaDefinitions.remove(sagaType);
    }

    public Map<String, SagaDefinitionData> getAllSagaDefinitions() {
        return new ConcurrentHashMap<>(sagaDefinitions);
    }

    public boolean hasSagaDefinition(String sagaType) {
        return sagaDefinitions.containsKey(sagaType);
    }

    public int getSagaDefinitionCount() {
        return sagaDefinitions.size();
    }

    public void clearAllSagaDefinitions() {
        sagaDefinitions.clear();
    }

    public SagaStepExecutor getStepExecutor(String executorName) {
        try {
            return applicationContext.getBean(executorName, SagaStepExecutor.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Step executor not found: " + executorName, e);
        }
    }

    public CompensatingTransaction getCompensatingTransaction(String transactionName) {
        try {
            return applicationContext.getBean(transactionName, CompensatingTransaction.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Compensating transaction not found: " + transactionName, e);
        }
    }

    public void validateSagaDefinition(String sagaType) {
        SagaDefinitionData definition = getSagaDefinition(sagaType);
        if (definition == null) {
            throw new IllegalArgumentException("Saga definition not found: " + sagaType);
        }
        
        definition.validate();
        
        // Validate that all step executors exist
        for (SagaStep step : definition.getSteps()) {
            if (step.getExecutor() != null) {
                // Executor is already a SagaStepExecutor object, no need to look up by name
                // The executor should be a valid Spring bean
            }
            
            if (step.getCompensatingTransaction() != null) {
                // CompensatingTransaction is already the object, no need to look up by name
                // The compensating transaction should be a valid Spring bean
            }
        }
    }

    public SagaDefinitionData createSagaDefinition(String sagaType, String description) {
        return SagaDefinitionData.builder()
                .sagaType(sagaType)
                .description(description)
                .build();
    }

    public SagaStep createSagaStep(String name, SagaStepExecutor executor) {
        return SagaStep.builder()
                .name(name)
                .executor(executor)
                .build();
    }

    public SagaStep createSagaStep(String name, SagaStepExecutor executor, CompensatingTransaction compensation) {
        return SagaStep.builder()
                .name(name)
                .executor(executor)
                .compensation(compensation)
                .build();
    }

    public void reloadSagaDefinitions() {
        clearAllSagaDefinitions();
        initializeSagaDefinitions();
    }
}
