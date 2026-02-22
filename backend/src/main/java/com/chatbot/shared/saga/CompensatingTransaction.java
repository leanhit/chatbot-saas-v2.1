package com.chatbot.shared.saga;

public interface CompensatingTransaction {

    void compensate(Object stepResult, String sagaId) throws Exception;

    default String getCompensationName() {
        return this.getClass().getSimpleName();
    }

    default boolean isCritical() {
        return true;
    }

    default int getTimeoutSeconds() {
        return 30;
    }

    default int getRetryCount() {
        return 3;
    }

    default void onCompensationSuccess(Object stepResult, String sagaId) {
        // Default implementation - can be overridden
    }

    default void onCompensationFailure(Object stepResult, String sagaId, Exception error) {
        // Default implementation - can be overridden
    }

    default void beforeCompensation(Object stepResult, String sagaId) {
        // Default implementation - can be overridden
    }

    default void afterCompensation(Object stepResult, String sagaId) {
        // Default implementation - can be overridden
    }
}
