package com.chatbot.shared.saga;

public class SagaStep {

    private String name;
    private String description;
    private SagaStepExecutor executor;
    private CompensatingTransaction compensatingTransaction;
    private int timeoutSeconds;
    private int retryCount;
    private boolean critical;
    private boolean async;

    public SagaStep() {
        this.timeoutSeconds = 30;
        this.retryCount = 3;
        this.critical = true;
        this.async = false;
    }

    public SagaStep(String name, SagaStepExecutor executor) {
        this();
        this.name = name;
        this.executor = executor;
    }

    public SagaStep(String name, SagaStepExecutor executor, CompensatingTransaction compensatingTransaction) {
        this(name, executor);
        this.compensatingTransaction = compensatingTransaction;
    }

    public void execute(Object stepData, String sagaId) throws Exception {
        if (executor == null) {
            throw new IllegalStateException("No executor defined for step: " + name);
        }

        Exception lastException = null;
        
        for (int attempt = 1; attempt <= retryCount + 1; attempt++) {
            try {
                if (async) {
                    executeAsync(stepData, sagaId);
                } else {
                    executor.execute(stepData, sagaId);
                }
                return;
            } catch (Exception e) {
                lastException = e;
                if (attempt <= retryCount) {
                    try {
                        Thread.sleep(calculateRetryDelay(attempt));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Step execution interrupted", ie);
                    }
                }
            }
        }
        
        throw new RuntimeException("Step execution failed after " + (retryCount + 1) + " attempts", lastException);
    }

    private void executeAsync(Object stepData, String sagaId) throws Exception {
        Thread asyncThread = new Thread(() -> {
            try {
                executor.execute(stepData, sagaId);
            } catch (Exception e) {
                throw new RuntimeException("Async step execution failed", e);
            }
        });
        
        asyncThread.start();
        asyncThread.join(timeoutSeconds * 1000L);
        
        if (asyncThread.isAlive()) {
            asyncThread.interrupt();
            throw new RuntimeException("Async step execution timed out after " + timeoutSeconds + " seconds");
        }
    }

    private long calculateRetryDelay(int attempt) {
        return (long) Math.pow(2, attempt - 1) * 1000; // Exponential backoff
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SagaStepExecutor getExecutor() {
        return executor;
    }

    public void setExecutor(SagaStepExecutor executor) {
        this.executor = executor;
    }

    public CompensatingTransaction getCompensatingTransaction() {
        return compensatingTransaction;
    }

    public void setCompensatingTransaction(CompensatingTransaction compensatingTransaction) {
        this.compensatingTransaction = compensatingTransaction;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public boolean isCritical() {
        return critical;
    }

    public void setCritical(boolean critical) {
        this.critical = critical;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public boolean hasCompensation() {
        return compensatingTransaction != null;
    }

    public static SagaStepBuilder builder() {
        return new SagaStepBuilder();
    }

    public static class SagaStepBuilder {
        private final SagaStep step;

        private SagaStepBuilder() {
            this.step = new SagaStep();
        }

        public SagaStepBuilder name(String name) {
            step.setName(name);
            return this;
        }

        public SagaStepBuilder description(String description) {
            step.setDescription(description);
            return this;
        }

        public SagaStepBuilder executor(SagaStepExecutor executor) {
            step.setExecutor(executor);
            return this;
        }

        public SagaStepBuilder compensation(CompensatingTransaction compensatingTransaction) {
            step.setCompensatingTransaction(compensatingTransaction);
            return this;
        }

        public SagaStepBuilder timeout(int timeoutSeconds) {
            step.setTimeoutSeconds(timeoutSeconds);
            return this;
        }

        public SagaStepBuilder retry(int retryCount) {
            step.setRetryCount(retryCount);
            return this;
        }

        public SagaStepBuilder critical(boolean critical) {
            step.setCritical(critical);
            return this;
        }

        public SagaStepBuilder async(boolean async) {
            step.setAsync(async);
            return this;
        }

        public SagaStep build() {
            if (step.getName() == null) {
                throw new IllegalStateException("Step name is required");
            }
            if (step.getExecutor() == null) {
                throw new IllegalStateException("Step executor is required");
            }
            return step;
        }
    }

    @Override
    public String toString() {
        return "SagaStep{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", timeoutSeconds=" + timeoutSeconds +
                ", retryCount=" + retryCount +
                ", critical=" + critical +
                ", async=" + async +
                ", hasCompensation=" + hasCompensation() +
                '}';
    }
}
