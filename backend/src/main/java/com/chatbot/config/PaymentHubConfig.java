package com.chatbot.config;

import org.springframework.beans.factory.annotation.Value;
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

/**
 * Payment Hub Configuration
 * Enables JPA repositories for payment module
 */
@Configuration
@EnableJpaRepositories(
    basePackages = {
        "com.chatbot.core.payment.transaction.repository",
        "com.chatbot.core.payment.plan.repository",
        "com.chatbot.core.payment.gateway.repository",
        "com.chatbot.core.payment.invoice.repository",
        "com.chatbot.core.payment.merchant.repository",
        "com.chatbot.core.payment.common.audit"
    },
    entityManagerFactoryRef = "paymentEntityManagerFactory",
    transactionManagerRef = "paymentTransactionManager"
)
public class PaymentHubConfig {

    @Value("${app.hibernate.ddl-auto:none}")
    private String ddlAuto;

    @Bean
    @DependsOn("sharedFlyway")
    public LocalContainerEntityManagerFactoryBean paymentEntityManagerFactory(
            @org.springframework.beans.factory.annotation.Qualifier("sharedDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan(
            "com.chatbot.core.payment.transaction.model",
            "com.chatbot.core.payment.plan.model",
            "com.chatbot.core.payment.gateway.model",
            "com.chatbot.core.payment.invoice.model",
            "com.chatbot.core.payment.merchant.model",
            "com.chatbot.core.payment.common.audit"
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
    public PlatformTransactionManager paymentTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(paymentEntityManagerFactory(null).getObject());
        return transactionManager;
    }
}
