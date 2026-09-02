package com.chatbot.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractTestcontainersIntegrationTest {

    @MockitoBean
    protected RabbitTemplate rabbitTemplate;

    @MockitoBean
    protected ConnectionFactory connectionFactory;

    protected static final PostgreSQLContainer<?> POSTGRES;
    protected static final GenericContainer<?> REDIS;

    static {
        System.setProperty("api.version", "1.44");

        POSTGRES = new PostgreSQLContainer<>(
                DockerImageName.parse("ankane/pgvector:latest").asCompatibleSubstituteFor("postgres")
        )
                .withDatabaseName("chatbot_test")
                .withUsername("test")
                .withPassword("test")
                .withInitScript("init_schemas.sql");
        POSTGRES.start();

        REDIS = new GenericContainer<>(
                DockerImageName.parse("redis:7-alpine")
        ).withExposedPorts(6379);
        REDIS.start();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        String jdbcUrl = POSTGRES.getJdbcUrl();
        String username = POSTGRES.getUsername();
        String password = POSTGRES.getPassword();
        String driver = POSTGRES.getDriverClassName();

        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.username", () -> username);
        registry.add("spring.datasource.password", () -> password);
        registry.add("spring.datasource.driver-class-name", () -> driver);
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");

        registry.add("app.datasource.identity.jdbc-url", () -> jdbcUrl + "?currentSchema=identity_db");
        registry.add("app.datasource.identity.username", () -> username);
        registry.add("app.datasource.identity.password", () -> password);
        registry.add("app.datasource.identity.driver-class-name", () -> driver);

        registry.add("app.datasource.user.jdbc-url", () -> jdbcUrl + "?currentSchema=user_db");
        registry.add("app.datasource.user.username", () -> username);
        registry.add("app.datasource.user.password", () -> password);
        registry.add("app.datasource.user.driver-class-name", () -> driver);

        registry.add("app.datasource.tenant.jdbc-url", () -> jdbcUrl + "?currentSchema=tenant_db");
        registry.add("app.datasource.tenant.username", () -> username);
        registry.add("app.datasource.tenant.password", () -> password);
        registry.add("app.datasource.tenant.driver-class-name", () -> driver);

        registry.add("app.datasource.app.jdbc-url", () -> jdbcUrl + "?currentSchema=app_db");
        registry.add("app.datasource.app.username", () -> username);
        registry.add("app.datasource.app.password", () -> password);
        registry.add("app.datasource.app.driver-class-name", () -> driver);

        registry.add("app.datasource.billing.jdbc-url", () -> jdbcUrl + "?currentSchema=billing_db");
        registry.add("app.datasource.billing.username", () -> username);
        registry.add("app.datasource.billing.password", () -> password);
        registry.add("app.datasource.billing.driver-class-name", () -> driver);

        registry.add("app.datasource.wallet.jdbc-url", () -> jdbcUrl + "?currentSchema=wallet_db");
        registry.add("app.datasource.wallet.username", () -> username);
        registry.add("app.datasource.wallet.password", () -> password);
        registry.add("app.datasource.wallet.driver-class-name", () -> driver);

        registry.add("app.datasource.config.jdbc-url", () -> jdbcUrl + "?currentSchema=config_db");
        registry.add("app.datasource.config.username", () -> username);
        registry.add("app.datasource.config.password", () -> password);
        registry.add("app.datasource.config.driver-class-name", () -> driver);

        registry.add("app.datasource.message.jdbc-url", () -> jdbcUrl + "?currentSchema=message_db");
        registry.add("app.datasource.message.username", () -> username);
        registry.add("app.datasource.message.password", () -> password);
        registry.add("app.datasource.message.driver-class-name", () -> driver);

        registry.add("app.datasource.shared.jdbc-url", () -> jdbcUrl + "?currentSchema=shared_db");
        registry.add("app.datasource.shared.username", () -> username);
        registry.add("app.datasource.shared.password", () -> password);
        registry.add("app.datasource.shared.driver-class-name", () -> driver);

        registry.add("app.datasource.spokes.jdbc-url", () -> jdbcUrl + "?currentSchema=spokes_db");
        registry.add("app.datasource.spokes.username", () -> username);
        registry.add("app.datasource.spokes.password", () -> password);
        registry.add("app.datasource.spokes.driver-class-name", () -> driver);

        registry.add("payment.datasource.jdbc-url", () -> jdbcUrl + "?currentSchema=user_db");
        registry.add("payment.datasource.username", () -> username);
        registry.add("payment.datasource.password", () -> password);
        registry.add("payment.datasource.driver-class-name", () -> driver);
    }
}
