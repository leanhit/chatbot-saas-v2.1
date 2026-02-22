package com.chatbot.shared.saga;

import java.util.Map;

public interface SagaStepExecutor {

    void execute(Object stepData, String sagaId) throws Exception;

    default String getStepName() {
        return this.getClass().getSimpleName();
    }

    default boolean isAsync() {
        return false;
    }

    default int getTimeoutSeconds() {
        return 30;
    }

    default int getRetryCount() {
        return 3;
    }

    default void beforeExecution(Object stepData, String sagaId) {
        // Default implementation - can be overridden
    }

    default void afterExecution(Object stepData, String sagaId) {
        // Default implementation - can be overridden
    }

    default void onExecutionSuccess(Object stepData, String sagaId) {
        // Default implementation - can be overridden
    }

    default void onExecutionFailure(Object stepData, String sagaId, Exception error) {
        // Default implementation - can be overridden
    }

    default Object prepareStepData(Object sagaData, Map<Integer, Object> stepResults) {
        // Default implementation - can be overridden
        return sagaData;
    }

    default boolean shouldExecute(Object stepData, String sagaId) {
        // Default implementation - can be overridden
        return true;
    }

    default void validateStepData(Object stepData) throws Exception {
        // Default implementation - can be overridden
    }
}
