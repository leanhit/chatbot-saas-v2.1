package com.chatbot.shared.saga;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SagaManager {

    @Autowired
    private SagaOrchestration sagaOrchestration;

    private final Map<String, SagaInstance> runningSagas = new ConcurrentHashMap<>();

    public SagaInstance startSaga(String sagaType, Object sagaData) {
        SagaDefinitionData sagaDefinition = sagaOrchestration.getSagaDefinition(sagaType);
        if (sagaDefinition == null) {
            throw new IllegalArgumentException("Saga definition not found: " + sagaType);
        }

        SagaInstance sagaInstance = new SagaInstance();
        sagaInstance.setSagaId(generateSagaId());
        sagaInstance.setSagaType(sagaType);
        sagaInstance.setSagaData(sagaData);
        sagaInstance.setStatus(SagaInstance.SagaStatus.RUNNING);
        sagaInstance.setCurrentStep(0);

        runningSagas.put(sagaInstance.getSagaId(), sagaInstance);

        try {
            executeNextStep(sagaInstance);
        } catch (Exception e) {
            handleSagaFailure(sagaInstance, e);
        }

        return sagaInstance;
    }

    public void handleStepCompletion(String sagaId, Object stepResult) {
        SagaInstance sagaInstance = runningSagas.get(sagaId);
        if (sagaInstance == null) {
            throw new IllegalArgumentException("Saga instance not found: " + sagaId);
        }

        sagaInstance.getStepResults().put(sagaInstance.getCurrentStep(), stepResult);
        sagaInstance.setCurrentStep(sagaInstance.getCurrentStep() + 1);

        try {
            executeNextStep(sagaInstance);
        } catch (Exception e) {
            handleSagaFailure(sagaInstance, e);
        }
    }

    public void handleStepFailure(String sagaId, Exception error) {
        SagaInstance sagaInstance = runningSagas.get(sagaId);
        if (sagaInstance == null) {
            throw new IllegalArgumentException("Saga instance not found: " + sagaId);
        }

        handleSagaFailure(sagaInstance, error);
    }

    private void executeNextStep(SagaInstance sagaInstance) {
        SagaDefinitionData sagaDefinition = sagaOrchestration.getSagaDefinition(sagaInstance.getSagaType());
        SagaStep currentStep = sagaDefinition.getSteps().get(sagaInstance.getCurrentStep());

        if (currentStep == null) {
            completeSaga(sagaInstance);
            return;
        }

        try {
            Object stepData = prepareStepData(sagaInstance, currentStep);
            currentStep.execute(stepData, sagaInstance.getSagaId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute saga step: " + currentStep.getName(), e);
        }
    }

    private Object prepareStepData(SagaInstance sagaInstance, SagaStep step) {
        Map<String, Object> stepData = new ConcurrentHashMap<>();
        stepData.put("sagaData", sagaInstance.getSagaData());
        stepData.put("stepResults", sagaInstance.getStepResults());
        stepData.put("sagaId", sagaInstance.getSagaId());
        stepData.put("sagaType", sagaInstance.getSagaType());
        
        return stepData;
    }

    private void handleSagaFailure(SagaInstance sagaInstance, Exception error) {
        sagaInstance.setStatus(SagaInstance.SagaStatus.COMPENSATING);
        sagaInstance.setError(error.getMessage());

        try {
            compensateSteps(sagaInstance);
        } catch (Exception e) {
            sagaInstance.setStatus(SagaInstance.SagaStatus.FAILED);
            sagaInstance.setError("Saga failed and compensation also failed: " + e.getMessage());
        }
    }

    private void compensateSteps(SagaInstance sagaInstance) {
        SagaDefinitionData sagaDefinition = sagaOrchestration.getSagaDefinition(sagaInstance.getSagaType());
        
        for (int i = sagaInstance.getCurrentStep() - 1; i >= 0; i--) {
            SagaStep step = sagaDefinition.getSteps().get(i);
            if (step.getCompensatingTransaction() != null) {
                try {
                    Object stepResult = sagaInstance.getStepResults().get(i);
                    step.getCompensatingTransaction().compensate(stepResult, sagaInstance.getSagaId());
                } catch (Exception e) {
                    throw new RuntimeException("Failed to compensate step: " + step.getName(), e);
                }
            }
        }

        sagaInstance.setStatus(SagaInstance.SagaStatus.COMPENSATED);
    }

    private void completeSaga(SagaInstance sagaInstance) {
        sagaInstance.setStatus(SagaInstance.SagaStatus.COMPLETED);
        runningSagas.remove(sagaInstance.getSagaId());
    }

    public SagaInstance getSagaInstance(String sagaId) {
        return runningSagas.get(sagaId);
    }

    public Map<String, SagaInstance> getRunningSagas() {
        return new ConcurrentHashMap<>(runningSagas);
    }

    public void cancelSaga(String sagaId) {
        SagaInstance sagaInstance = runningSagas.get(sagaId);
        if (sagaInstance != null) {
            handleSagaFailure(sagaInstance, new RuntimeException("Saga cancelled"));
        }
    }

    public void retrySaga(String sagaId) {
        SagaInstance sagaInstance = runningSagas.get(sagaId);
        if (sagaInstance != null && 
            (sagaInstance.getStatus() == SagaInstance.SagaStatus.FAILED || 
             sagaInstance.getStatus() == SagaInstance.SagaStatus.COMPENSATED)) {
            
            sagaInstance.setStatus(SagaInstance.SagaStatus.RUNNING);
            sagaInstance.setCurrentStep(0);
            sagaInstance.getStepResults().clear();
            sagaInstance.setError(null);

            try {
                executeNextStep(sagaInstance);
            } catch (Exception e) {
                handleSagaFailure(sagaInstance, e);
            }
        }
    }

    private String generateSagaId() {
        return "SAGA_" + System.currentTimeMillis() + "_" + 
               Integer.toHexString((int) (Math.random() * 0xFFFF));
    }

    public void cleanupCompletedSagas() {
        runningSagas.entrySet().removeIf(entry -> 
            entry.getValue().getStatus() == SagaInstance.SagaStatus.COMPLETED ||
            entry.getValue().getStatus() == SagaInstance.SagaStatus.COMPENSATED ||
            entry.getValue().getStatus() == SagaInstance.SagaStatus.FAILED);
    }

    public int getRunningSagaCount() {
        return (int) runningSagas.values().stream()
                .filter(saga -> saga.getStatus() == SagaInstance.SagaStatus.RUNNING)
                .count();
    }

    public int getFailedSagaCount() {
        return (int) runningSagas.values().stream()
                .filter(saga -> saga.getStatus() == SagaInstance.SagaStatus.FAILED)
                .count();
    }
}
