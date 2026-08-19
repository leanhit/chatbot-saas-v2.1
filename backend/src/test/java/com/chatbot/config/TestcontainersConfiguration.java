package com.chatbot.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers configuration for integration tests
 * Provides PostgreSQL container for testing with real database
 */
@TestConfiguration
public class TestcontainersConfiguration {

    /**
     * PostgreSQL container for integration tests
     */
    @Bean(destroyMethod = "stop")
    @Primary
    public PostgreSQLContainer<?> postgresContainer() {
        PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15-alpine")
        )
            .withDatabaseName("chatbot_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);
        
        postgresContainer.start();
        return postgresContainer;
    }
}
