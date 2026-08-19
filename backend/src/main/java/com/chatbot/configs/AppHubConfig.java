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
        "com.chatbot.core.app.repository",
        "com.chatbot.core.app.registry.repository",
        "com.chatbot.core.app.subscription.repository",
        "com.chatbot.core.app.guard.repository",
        "com.chatbot.core.license.repository"
    },
    entityManagerFactoryRef = "appEntityManagerFactory",
    transactionManagerRef = "appTransactionManager"
)
public class AppHubConfig {

    @Value("${app.hibernate.ddl-auto:none}")
    private String ddlAuto;

    @Bean
    @ConfigurationProperties(prefix = "app.datasource.app")
    public DataSource appDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @DependsOn("appFlyway")
    public LocalContainerEntityManagerFactoryBean appEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(appDataSource());
        em.setPackagesToScan(
            "com.chatbot.core.app.model",
            "com.chatbot.core.app.registry.model",
            "com.chatbot.core.app.subscription.model",
            "com.chatbot.core.app.guard.model",
            "com.chatbot.core.license.model"
        );

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", ddlAuto);
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.show_sql", "false");
        properties.put("hibernate.format_sql", "false");
        properties.put("hibernate.jdbc.lob.non_contextual_creation", "true");
        properties.put("hibernate.jdbc.time_out", "30");
        
        Properties jpaProperties = new Properties();
        jpaProperties.putAll(properties);
        em.setJpaProperties(jpaProperties);
        return em;
    }

    @Bean
    public PlatformTransactionManager appTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(appEntityManagerFactory().getObject());
        return transactionManager;
    }
}
