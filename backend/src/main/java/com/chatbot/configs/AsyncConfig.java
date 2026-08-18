package com.chatbot.configs;

import com.chatbot.core.tenant.infra.TenantContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.Executor;

/**
 * Configuration for asynchronous execution across the application.
 * Enables @Async support and provides a thread‑pool executor used by the Kafka consumer
 * and any other async services.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-pool-");
        executor.setTaskDecorator(runnable -> {
            Long tenantId = TenantContext.getTenantId();
            String tenantKey = TenantContext.getCurrentTenant();
            SecurityContext securityContext = SecurityContextHolder.getContext();
            return () -> {
                try {
                    if (tenantId != null) TenantContext.setTenantId(tenantId);
                    if (tenantKey != null) TenantContext.setCurrentTenant(tenantKey);
                    if (securityContext != null) SecurityContextHolder.setContext(securityContext);
                    runnable.run();
                } finally {
                    TenantContext.clear();
                    SecurityContextHolder.clearContext();
                }
            };
        });
        executor.initialize();
        return executor;
    }
}
