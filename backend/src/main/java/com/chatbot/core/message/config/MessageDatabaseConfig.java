package com.chatbot.core.message.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
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
    basePackages = "com.chatbot.core.message.store.repository",
    entityManagerFactoryRef = "messageEntityManagerFactory",
    transactionManagerRef = "messageTransactionManager"
)
public class MessageDatabaseConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.message")
    public DataSource messageDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean messageEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(messageDataSource());
        em.setPackagesToScan("com.chatbot.core.message.store.model");

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
    public PlatformTransactionManager messageTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(messageEntityManagerFactory().getObject());
        return transactionManager;
    }
}
