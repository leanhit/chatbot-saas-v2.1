package com.chatbot.core.app.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        "com.chatbot.core.app.*.repository",
        "com.chatbot.spokes.minio.image.fileMetadata.repository",
        "com.chatbot.spokes.minio.image.category.repository",
        "com.chatbot.spokes.facebook.connection.repository",
        "com.chatbot.spokes.facebook.user.repository",
        "com.chatbot.spokes.botpress.repository",
        "com.chatbot.integrations.botpress.repository",
        "com.chatbot.integrations.odoo.repository",
        "com.chatbot.shared.penny.repository",
        "com.chatbot.shared.penny.rules"
    },
    entityManagerFactoryRef = "appEntityManagerFactory",
    transactionManagerRef = "appTransactionManager"
)
public class AppDatabaseConfig {

    @Bean
    public DataSource appDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:postgresql://localhost:5432/traloitudong_db")
                .username("traloitudong_user")
                .password("traloitudong_Admin_2025")
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean appEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(appDataSource());
        em.setPackagesToScan(
            "com.chatbot.core.app.registry.model",
            "com.chatbot.core.app.guard.model",
            "com.chatbot.core.app.subscription.model",
            "com.chatbot.spokes.minio.image.fileMetadata.model",
            "com.chatbot.spokes.minio.image.category.model",
            "com.chatbot.core.user.model",
            "com.chatbot.core.user.profile",
            "com.chatbot.spokes.facebook.connection.model",
            "com.chatbot.spokes.facebook.user.model",
            "com.chatbot.spokes.botpress.model",
            "com.chatbot.integrations.botpress.model",
            "com.chatbot.integrations.odoo.model",
            "com.chatbot.shared.penny.model",
            "com.chatbot.shared.penny.rules"
        );

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.hbm2ddl.auto", "update");
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        jpaProperties.put("hibernate.show_sql", "false");
        jpaProperties.put("hibernate.format_sql", "false");
        jpaProperties.put("hibernate.jdbc.lob.non_contextual_creation", "true");
        Properties properties = new Properties();
        properties.putAll(jpaProperties);
        em.setJpaProperties(properties);

        return em;
    }

    @Bean
    public PlatformTransactionManager appTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(appEntityManagerFactory().getObject());
        return transactionManager;
    }
}
