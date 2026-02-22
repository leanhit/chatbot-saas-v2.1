package com.chatbot.core.identity.config;

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
    basePackages = {"com.chatbot.core.identity.repository"},
    entityManagerFactoryRef = "identityEntityManagerFactory",
    transactionManagerRef = "identityTransactionManager"
)
public class IdentityDatabaseConfig {

    @Bean
    public DataSource identityDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:postgresql://localhost:5432/traloitudong_db")
                .username("traloitudong_user")
                .password("traloitudong_Admin_2025")
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean identityEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(identityDataSource());
        em.setPackagesToScan("com.chatbot.core.identity.model", "com.chatbot.core.user.model", "com.chatbot.core.user.profile");

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
    public PlatformTransactionManager identityTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(identityEntityManagerFactory().getObject());
        return transactionManager;
    }
}
