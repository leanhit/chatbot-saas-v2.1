package com.chatbot.shared.messaging;

public interface EventProcessor {

    void process(Event event) throws Exception;

    default String getProcessorName() {
        return this.getClass().getSimpleName();
    }

    default boolean canProcess(Event event) {
        return true;
    }

    default boolean isAsync() {
        return false;
    }

    default int getRetryCount() {
        return 3;
    }

    default long getRetryDelayMillis() {
        return 1000;
    }

    default void beforeProcessing(Event event) {
        // Default implementation - can be overridden
    }

    default void afterProcessing(Event event) {
        // Default implementation - can be overridden
    }

    default void onProcessingSuccess(Event event) {
        // Default implementation - can be overridden
    }

    default void onProcessingFailure(Event event, Exception error) {
        // Default implementation - can be overridden
    }

    default void validateEvent(Event event) throws Exception {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        if (event.getEventType() == null || event.getEventType().trim().isEmpty()) {
            throw new IllegalArgumentException("Event type cannot be null or empty");
        }
    }

    default boolean shouldRetry(Event event, Exception error, int attempt) {
        return attempt < getRetryCount();
    }

    default void handleRetry(Event event, Exception error, int attempt) {
        try {
            Thread.sleep(getRetryDelayMillis() * attempt);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Event processing interrupted during retry", ie);
        }
    }
}
