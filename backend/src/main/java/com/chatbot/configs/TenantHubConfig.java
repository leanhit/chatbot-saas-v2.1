package com.chatbot.configs;

import org.springframework.beans.factory.annotation.Value;
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
    basePackages = {
        "com.chatbot.core.tenant.repository",
        "com.chatbot.core.tenant.membership.repository",
        "com.chatbot.core.tenant.profile.repository",
        "com.chatbot.core.tenant.professional.repository"
    },
    entityManagerFactoryRef = "tenantEntityManagerFactory",
    transactionManagerRef = "tenantTransactionManager"
)
public class TenantHubConfig {

    @Value("${app.hibernate.ddl-auto:update}")
    private String ddlAuto;

    @Bean
    @ConfigurationProperties(prefix = "app.datasource.tenant")
    public DataSource tenantDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean tenantEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(tenantDataSource());
        em.setPackagesToScan(
            "com.chatbot.core.tenant.model",
            "com.chatbot.core.tenant.membership.model",
            "com.chatbot.core.tenant.profile",
            "com.chatbot.core.tenant.professional"
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
    public PlatformTransactionManager tenantTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(tenantEntityManagerFactory().getObject());
        return transactionManager;
    }
}
