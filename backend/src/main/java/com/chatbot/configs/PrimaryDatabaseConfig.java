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
        "com.chatbot.shared.address.repository",
        "com.chatbot.shared.infrastructure.repository"
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
            "com.chatbot.shared.address.model",
            "com.chatbot.shared.infrastructure.model"
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
