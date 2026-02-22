package com.chatbot.shared.saga;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SagaInstance {

    private String sagaId;
    private String sagaType;
    private Object sagaData;
    private SagaStatus status;
    private int currentStep;
    private Map<Integer, Object> stepResults;
    private String error;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    public enum SagaStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED,
        COMPENSATING,
        COMPENSATED
    }

    public SagaInstance() {
        this.stepResults = new ConcurrentHashMap<>();
        this.status = SagaStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getSagaId() {
        return sagaId;
    }

    public void setSagaId(String sagaId) {
        this.sagaId = sagaId;
        this.updatedAt = LocalDateTime.now();
    }

    public String getSagaType() {
        return sagaType;
    }

    public void setSagaType(String sagaType) {
        this.sagaType = sagaType;
        this.updatedAt = LocalDateTime.now();
    }

    public Object getSagaData() {
        return sagaData;
    }

    public void setSagaData(Object sagaData) {
        this.sagaData = sagaData;
        this.updatedAt = LocalDateTime.now();
    }

    public SagaStatus getStatus() {
        return status;
    }

    public void setStatus(SagaStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
        
        if (status == SagaStatus.COMPLETED || 
            status == SagaStatus.COMPENSATED || 
            status == SagaStatus.FAILED) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
        this.updatedAt = LocalDateTime.now();
    }

    public Map<Integer, Object> getStepResults() {
        return stepResults;
    }

    public void setStepResults(Map<Integer, Object> stepResults) {
        this.stepResults = stepResults;
        this.updatedAt = LocalDateTime.now();
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public boolean isRunning() {
        return status == SagaStatus.RUNNING;
    }

    public boolean isCompleted() {
        return status == SagaStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == SagaStatus.FAILED;
    }

    public boolean isCompensating() {
        return status == SagaStatus.COMPENSATING;
    }

    public boolean isCompensated() {
        return status == SagaStatus.COMPENSATED;
    }

    public boolean isFinished() {
        return status == SagaStatus.COMPLETED || 
               status == SagaStatus.COMPENSATED || 
               status == SagaStatus.FAILED;
    }

    public long getDurationInMinutes() {
        if (completedAt == null) {
            return java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes();
        }
        return java.time.Duration.between(createdAt, completedAt).toMinutes();
    }

    public long getDurationInSeconds() {
        if (completedAt == null) {
            return java.time.Duration.between(createdAt, LocalDateTime.now()).getSeconds();
        }
        return java.time.Duration.between(createdAt, completedAt).getSeconds();
    }

    public void addStepResult(int stepIndex, Object result) {
        this.stepResults.put(stepIndex, result);
        this.updatedAt = LocalDateTime.now();
    }

    public Object getStepResult(int stepIndex) {
        return this.stepResults.get(stepIndex);
    }

    public boolean hasStepResult(int stepIndex) {
        return this.stepResults.containsKey(stepIndex);
    }

    public void clearStepResults() {
        this.stepResults.clear();
        this.updatedAt = LocalDateTime.now();
    }

    public int getCompletedStepsCount() {
        return this.stepResults.size();
    }

    public double getProgressPercentage(int totalSteps) {
        if (totalSteps == 0) return 0.0;
        return (double) getCompletedStepsCount() / totalSteps * 100;
    }

    @Override
    public String toString() {
        return "SagaInstance{" +
                "sagaId='" + sagaId + '\'' +
                ", sagaType='" + sagaType + '\'' +
                ", status=" + status +
                ", currentStep=" + currentStep +
                ", stepResultsCount=" + stepResults.size() +
                ", error='" + error + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", completedAt=" + completedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SagaInstance that = (SagaInstance) o;
        return sagaId != null ? sagaId.equals(that.sagaId) : that.sagaId == null;
    }

    @Override
    public int hashCode() {
        return sagaId != null ? sagaId.hashCode() : 0;
    }
}
