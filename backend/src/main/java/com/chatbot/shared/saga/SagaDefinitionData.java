package com.chatbot.shared.saga;

import java.util.ArrayList;
import java.util.List;

public class SagaDefinitionData {

    private String sagaType;
    private String description;
    private List<SagaStep> steps;
    private boolean compensatable;
    private int globalTimeoutSeconds;
    private boolean retryOnFailure;

    public SagaDefinitionData() {
        this.steps = new ArrayList<>();
        this.compensatable = true;
        this.globalTimeoutSeconds = 300; // 5 minutes
        this.retryOnFailure = false;
    }

    public SagaDefinitionData(String sagaType) {
        this();
        this.sagaType = sagaType;
    }

    public SagaDefinitionData(String sagaType, String description) {
        this(sagaType);
        this.description = description;
    }

    public void addStep(SagaStep step) {
        if (step == null) {
            throw new IllegalArgumentException("Step cannot be null");
        }
        this.steps.add(step);
    }

    public void addStep(int index, SagaStep step) {
        if (step == null) {
            throw new IllegalArgumentException("Step cannot be null");
        }
        if (index < 0 || index > steps.size()) {
            throw new IndexOutOfBoundsException("Invalid step index: " + index);
        }
        this.steps.add(index, step);
    }

    public void removeStep(int index) {
        if (index < 0 || index >= steps.size()) {
            throw new IndexOutOfBoundsException("Invalid step index: " + index);
        }
        this.steps.remove(index);
    }

    public void removeStep(String stepName) {
        this.steps.removeIf(step -> step.getName().equals(stepName));
    }

    public SagaStep getStep(int index) {
        if (index < 0 || index >= steps.size()) {
            throw new IndexOutOfBoundsException("Invalid step index: " + index);
        }
        return this.steps.get(index);
    }

    public SagaStep getStep(String stepName) {
        return this.steps.stream()
                .filter(step -> step.getName().equals(stepName))
                .findFirst()
                .orElse(null);
    }

    public int getStepCount() {
        return steps.size();
    }

    public boolean hasSteps() {
        return !steps.isEmpty();
    }

    public boolean isCompensatable() {
        return compensatable && steps.stream().anyMatch(SagaStep::hasCompensation);
    }

    public List<SagaStep> getCriticalSteps() {
        return steps.stream()
                .filter(SagaStep::isCritical)
                .toList();
    }

    public List<SagaStep> getAsyncSteps() {
        return steps.stream()
                .filter(SagaStep::isAsync)
                .toList();
    }

    public List<SagaStep> getCompensatableSteps() {
        return steps.stream()
                .filter(SagaStep::hasCompensation)
                .toList();
    }

    public int getTotalTimeoutSeconds() {
        return steps.stream()
                .mapToInt(SagaStep::getTimeoutSeconds)
                .sum();
    }

    public void validate() {
        if (sagaType == null || sagaType.trim().isEmpty()) {
            throw new IllegalStateException("Saga type is required");
        }

        if (steps.isEmpty()) {
            throw new IllegalStateException("At least one step is required");
        }

        for (int i = 0; i < steps.size(); i++) {
            SagaStep step = steps.get(i);
            if (step.getName() == null || step.getName().trim().isEmpty()) {
                throw new IllegalStateException("Step " + i + " name is required");
            }
            if (step.getExecutor() == null) {
                throw new IllegalStateException("Step " + i + " executor is required");
            }
        }

        if (compensatable && !isCompensatable()) {
            throw new IllegalStateException("Saga is marked as compensatable but no steps have compensation");
        }

        if (globalTimeoutSeconds > 0 && getTotalTimeoutSeconds() > globalTimeoutSeconds) {
            throw new IllegalStateException("Total step timeout exceeds global timeout");
        }
    }

    public String getSagaType() {
        return sagaType;
    }

    public void setSagaType(String sagaType) {
        this.sagaType = sagaType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<SagaStep> getSteps() {
        return new ArrayList<>(steps);
    }

    public void setSteps(List<SagaStep> steps) {
        if (steps == null) {
            this.steps = new ArrayList<>();
        } else {
            this.steps = new ArrayList<>(steps);
        }
    }

    public void setCompensatable(boolean compensatable) {
        this.compensatable = compensatable;
    }

    public int getGlobalTimeoutSeconds() {
        return globalTimeoutSeconds;
    }

    public void setGlobalTimeoutSeconds(int globalTimeoutSeconds) {
        this.globalTimeoutSeconds = globalTimeoutSeconds;
    }

    public boolean isRetryOnFailure() {
        return retryOnFailure;
    }

    public void setRetryOnFailure(boolean retryOnFailure) {
        this.retryOnFailure = retryOnFailure;
    }

    public static SagaDefinitionBuilder builder() {
        return new SagaDefinitionBuilder();
    }

    public static class SagaDefinitionBuilder {
        private final SagaDefinitionData definition;

        private SagaDefinitionBuilder() {
            this.definition = new SagaDefinitionData();
        }

        public SagaDefinitionBuilder sagaType(String sagaType) {
            definition.setSagaType(sagaType);
            return this;
        }

        public SagaDefinitionBuilder description(String description) {
            definition.setDescription(description);
            return this;
        }

        public SagaDefinitionBuilder step(SagaStep step) {
            definition.addStep(step);
            return this;
        }

        public SagaDefinitionBuilder compensatable(boolean compensatable) {
            definition.setCompensatable(compensatable);
            return this;
        }

        public SagaDefinitionBuilder globalTimeout(int timeoutSeconds) {
            definition.setGlobalTimeoutSeconds(timeoutSeconds);
            return this;
        }

        public SagaDefinitionBuilder retryOnFailure(boolean retryOnFailure) {
            definition.setRetryOnFailure(retryOnFailure);
            return this;
        }

        public SagaDefinitionData build() {
            definition.validate();
            return definition;
        }
    }

    @Override
    public String toString() {
        return "SagaDefinitionData{" +
                "sagaType='" + sagaType + '\'' +
                ", description='" + description + '\'' +
                ", stepCount=" + steps.size() +
                ", compensatable=" + compensatable +
                ", globalTimeoutSeconds=" + globalTimeoutSeconds +
                ", retryOnFailure=" + retryOnFailure +
                '}';
    }
}
