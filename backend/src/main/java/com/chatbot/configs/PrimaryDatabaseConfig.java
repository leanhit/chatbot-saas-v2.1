package com.chatbot.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(
    basePackages = {
        "com.chatbot.core.identity.repository",
        "com.chatbot.core.user.repository",
        "com.chatbot.core.tenant.repository",
        "com.chatbot.core.tenant.membership.repository",
        "com.chatbot.core.tenant.profile.repository",
        "com.chatbot.core.tenant.professional.repository",
        "com.chatbot.core.app.repository",
        "com.chatbot.core.app.registry.repository",
        "com.chatbot.core.app.subscription.repository",
        "com.chatbot.core.app.guard.repository",
        "com.chatbot.core.billing.repository",
        "com.chatbot.core.billing.account.repository",
        "com.chatbot.core.billing.entitlement.repository",
        "com.chatbot.core.billing.subscription.repository",
        "com.chatbot.core.wallet.repository",
        "com.chatbot.core.wallet.ledger.repository",
        "com.chatbot.core.wallet.transaction.repository",
        "com.chatbot.core.wallet.wallet.repository",
        "com.chatbot.core.config.repository",
        "com.chatbot.core.config.environment.repository",
        "com.chatbot.core.config.runtime.repository",
        "com.chatbot.core.message.repository",
        "com.chatbot.core.message.store.repository",
        "com.chatbot.shared.address.repository",
        "com.chatbot.shared.infrastructure.repository",
        "com.chatbot.shared.penny.repository",
        "com.chatbot.shared.penny.rules",
        "com.chatbot.spokes.odoo.repository",
        "com.chatbot.spokes.facebook.repository",
        "com.chatbot.spokes.facebook.connection.repository",
        "com.chatbot.spokes.facebook.user.repository",
        "com.chatbot.spokes.botpress.repository",
        "com.chatbot.spokes.minio.repository",
        "com.chatbot.spokes.minio.image.fileMetadata.repository",
        "com.chatbot.spokes.minio.image.category.repository"
    },
    entityManagerFactoryRef = "primaryEntityManagerFactory",
    transactionManagerRef = "primaryTransactionManager"
)
public class PrimaryDatabaseConfig {

    @Bean
    @Primary
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:postgresql://localhost:5432/traloitudong_db")
                .username("traloitudong_user")
                .password("traloitudong_Admin_2025")
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(primaryDataSource());
        em.setPackagesToScan(
            "com.chatbot.core.identity.model",
            "com.chatbot.core.user.model",
            "com.chatbot.core.user.profile",
            "com.chatbot.core.tenant.model",
            "com.chatbot.core.tenant.membership",
            "com.chatbot.core.tenant.profile",
            "com.chatbot.core.tenant.professional",
            "com.chatbot.core.tenant.infra",
            "com.chatbot.core.app.model",
        "com.chatbot.core.app.registry.model",
        "com.chatbot.core.app.subscription.model",
        "com.chatbot.core.app.guard.model",
            "com.chatbot.core.billing.model",
        "com.chatbot.core.billing.account.model",
        "com.chatbot.core.billing.entitlement.model",
        "com.chatbot.core.billing.subscription.model",
            "com.chatbot.core.wallet.model",
        "com.chatbot.core.wallet.ledger.model",
        "com.chatbot.core.wallet.transaction.model",
        "com.chatbot.core.wallet.wallet.model",
            "com.chatbot.core.config.model",
        "com.chatbot.core.config.environment.model",
        "com.chatbot.core.config.runtime.model",
            "com.chatbot.core.message.model",
        "com.chatbot.core.message.store.model",
            "com.chatbot.shared.address.model",
            "com.chatbot.shared.infrastructure.model",
            "com.chatbot.shared.penny.model",
            "com.chatbot.shared.penny.rules",
            "com.chatbot.spokes.odoo.model",
            "com.chatbot.spokes.facebook.model",
        "com.chatbot.spokes.facebook.connection.model",
        "com.chatbot.spokes.facebook.user.model",
            "com.chatbot.spokes.botpress.model",
            "com.chatbot.spokes.minio.model",
        "com.chatbot.spokes.minio.image.fileMetadata.model",
        "com.chatbot.spokes.minio.image.category.model"
        );

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.show_sql", "false");
        properties.put("hibernate.format_sql", "false");
        properties.put("hibernate.jdbc.lob.non_contextual_creation", "true");
        Properties jpaProperties = new Properties();
        jpaProperties.putAll(properties);
        em.setJpaProperties(jpaProperties);

        return em;
    }

    @Bean
    @Primary
    public PlatformTransactionManager primaryTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(primaryEntityManagerFactory().getObject());
        return transactionManager;
    }
}
