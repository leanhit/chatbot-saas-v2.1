package com.chatbot.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
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
        "com.chatbot.shared.address.repository",
        "com.chatbot.shared.infrastructure.repository",
        "com.chatbot.shared.penny.rules",
        "com.chatbot.spokes.odoo.repository",
        "com.chatbot.spokes.facebook.repository",
        "com.chatbot.spokes.facebook.connection.repository",
        "com.chatbot.spokes.facebook.user.repository",
        "com.chatbot.spokes.minio.repository",
        "com.chatbot.spokes.minio.image.fileMetadata.repository",
        "com.chatbot.spokes.minio.image.category.repository",
        "com.chatbot.core.simplepayment.repository"
    },
    entityManagerFactoryRef = "sharedEntityManagerFactory",
    transactionManagerRef = "sharedTransactionManager"
)
public class HubDatabaseConfig {

    @Value("${app.hibernate.ddl-auto:update}")
    private String ddlAuto;

    // ========================================
    // Shared/Spokes Database Configuration
    // ========================================
    @Bean
    @ConfigurationProperties(prefix = "app.datasource.shared")
    public DataSource sharedDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(sharedDataSource());
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean sharedEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(sharedDataSource());
        em.setPackagesToScan(
            "com.chatbot.shared.address.model",
            "com.chatbot.shared.infrastructure.model",
            "com.chatbot.shared.penny.rules",
            "com.chatbot.spokes.odoo.model",
            "com.chatbot.spokes.facebook.model",
            "com.chatbot.spokes.facebook.connection.model",
            "com.chatbot.spokes.facebook.user.model",
            "com.chatbot.spokes.minio.model",
            "com.chatbot.spokes.minio.image.fileMetadata.model",
            "com.chatbot.spokes.minio.image.category.model",
            "com.chatbot.core.simplepayment.model"
            );

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", ddlAuto);
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
    public PlatformTransactionManager sharedTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(sharedEntityManagerFactory().getObject());
        return transactionManager;
    }
}
