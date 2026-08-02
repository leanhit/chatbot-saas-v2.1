package com.chatbot.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
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
        "com.chatbot.spokes.odoo.repository",
        "com.chatbot.spokes.facebook.repository",
        "com.chatbot.spokes.facebook.connection.repository",
        "com.chatbot.spokes.facebook.user.repository",
        "com.chatbot.spokes.minio.repository",
        "com.chatbot.spokes.minio.image.fileMetadata.repository",
        "com.chatbot.spokes.minio.image.category.repository"
    },
    entityManagerFactoryRef = "spokesEntityManagerFactory",
    transactionManagerRef = "spokesTransactionManager"
)
public class SpokesDatabaseConfig {

    @Value("${app.hibernate.ddl-auto:none}")
    private String ddlAuto;

    @Bean
    @ConfigurationProperties(prefix = "app.datasource.spokes")
    public DataSource spokesDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @DependsOn("spokesFlyway")
    public LocalContainerEntityManagerFactoryBean spokesEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(spokesDataSource());
        em.setPackagesToScan(
            "com.chatbot.spokes.odoo.model",
            "com.chatbot.spokes.facebook.model",
            "com.chatbot.spokes.facebook.connection.model",
            "com.chatbot.spokes.facebook.user.model",
            "com.chatbot.spokes.minio.model",
            "com.chatbot.spokes.minio.image.fileMetadata.model",
            "com.chatbot.spokes.minio.image.category.model"
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
    public PlatformTransactionManager spokesTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(spokesEntityManagerFactory().getObject());
        return transactionManager;
    }
}
