package com.chatbot.configs;

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

// Temporarily disable multi-database configuration for development
// @Configuration
public class HubDatabaseConfig {

    // ========================================
    // Identity Hub Database Configuration
    // ========================================
    // @Bean
    // Temporarily disable multi-database configuration for development
// @ConfigurationProperties(prefix = "app.datasource.identity")
    public DataSource identityDataSource() {
        return DataSourceBuilder.create().build();
    }

    // @Bean
    public LocalContainerEntityManagerFactoryBean identityEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(identityDataSource());
        em.setPackagesToScan(
            "com.chatbot.core.identity.model",
            "com.chatbot.core.identity.repository"
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

    // @Bean
    public PlatformTransactionManager identityTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(identityEntityManagerFactory().getObject());
        return transactionManager;
    }

    // ========================================
    // User Hub Database Configuration
    // ========================================
    // @Bean
    // Temporarily disable multi-database configuration for development
// @ConfigurationProperties(prefix = "app.datasource.user")
    public DataSource userDataSource() {
        return DataSourceBuilder.create().build();
    }

    // @Bean
    public LocalContainerEntityManagerFactoryBean userEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(userDataSource());
        em.setPackagesToScan(
            "com.chatbot.core.user.model",
            "com.chatbot.core.user.profile",
            "com.chatbot.core.user.repository"
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

    // @Bean
    public PlatformTransactionManager userTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(userEntityManagerFactory().getObject());
        return transactionManager;
    }

    // ========================================
    // Tenant Hub Database Configuration
    // ========================================
    // @Bean
    // Temporarily disable multi-database configuration for development
// @ConfigurationProperties(prefix = "app.datasource.tenant")
    public DataSource tenantDataSource() {
        return DataSourceBuilder.create().build();
    }

    // @Bean
    public LocalContainerEntityManagerFactoryBean tenantEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(tenantDataSource());
        em.setPackagesToScan(
            "com.chatbot.core.tenant.model",
            "com.chatbot.core.tenant.membership",
            "com.chatbot.core.tenant.profile",
            "com.chatbot.core.tenant.professional",
            "com.chatbot.core.tenant.infra",
            "com.chatbot.core.tenant.repository",
            "com.chatbot.core.tenant.membership.repository",
            "com.chatbot.core.tenant.profile.repository",
            "com.chatbot.core.tenant.professional.repository"
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

    // @Bean
    public PlatformTransactionManager tenantTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(tenantEntityManagerFactory().getObject());
        return transactionManager;
    }

    // ========================================
    // App Hub Database Configuration
    // ========================================
    // @Bean
    // Temporarily disable multi-database configuration for development
// @ConfigurationProperties(prefix = "app.datasource.app")
    public DataSource appDataSource() {
        return DataSourceBuilder.create().build();
    }

    // @Bean
    public LocalContainerEntityManagerFactoryBean appEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(appDataSource());
        em.setPackagesToScan(
            "com.chatbot.core.app.model",
            "com.chatbot.core.app.registry.model",
            "com.chatbot.core.app.subscription.model",
            "com.chatbot.core.app.guard.model",
            "com.chatbot.core.app.repository",
            "com.chatbot.core.app.registry.repository",
            "com.chatbot.core.app.subscription.repository",
            "com.chatbot.core.app.guard.repository"
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

    // @Bean
    public PlatformTransactionManager appTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(appEntityManagerFactory().getObject());
        return transactionManager;
    }

    // ========================================
    // Billing Hub Database Configuration
    // ========================================
    // @Bean
    // Temporarily disable multi-database configuration for development
// @ConfigurationProperties(prefix = "app.datasource.billing")
    public DataSource billingDataSource() {
        return DataSourceBuilder.create().build();
    }

    // @Bean
    public LocalContainerEntityManagerFactoryBean billingEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(billingDataSource());
        em.setPackagesToScan(
            "com.chatbot.core.billing.model",
            "com.chatbot.core.billing.account.model",
            "com.chatbot.core.billing.entitlement.model",
            "com.chatbot.core.billing.subscription.model",
            "com.chatbot.core.billing.currency.model",
            "com.chatbot.core.billing.repository",
            "com.chatbot.core.billing.account.repository",
            "com.chatbot.core.billing.entitlement.repository",
            "com.chatbot.core.billing.subscription.repository",
            "com.chatbot.core.billing.currency.repository"
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

    // @Bean
    public PlatformTransactionManager billingTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(billingEntityManagerFactory().getObject());
        return transactionManager;
    }

    // ========================================
    // Wallet Hub Database Configuration
    // ========================================
    // @Bean
    // Temporarily disable multi-database configuration for development
// @ConfigurationProperties(prefix = "app.datasource.wallet")
    public DataSource walletDataSource() {
        return DataSourceBuilder.create().build();
    }

    // @Bean
    public LocalContainerEntityManagerFactoryBean walletEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(walletDataSource());
        em.setPackagesToScan(
            "com.chatbot.core.wallet.model",
            "com.chatbot.core.wallet.ledger.model",
            "com.chatbot.core.wallet.transaction.model",
            "com.chatbot.core.wallet.wallet.model",
            "com.chatbot.core.wallet.repository",
            "com.chatbot.core.wallet.ledger.repository",
            "com.chatbot.core.wallet.transaction.repository",
            "com.chatbot.core.wallet.wallet.repository"
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

    // @Bean
    public PlatformTransactionManager walletTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(walletEntityManagerFactory().getObject());
        return transactionManager;
    }

    // ========================================
    // Config Hub Database Configuration
    // ========================================
    // @Bean
    // Temporarily disable multi-database configuration for development
// @ConfigurationProperties(prefix = "app.datasource.config")
    public DataSource configDataSource() {
        return DataSourceBuilder.create().build();
    }

    // @Bean
    public LocalContainerEntityManagerFactoryBean configEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(configDataSource());
        em.setPackagesToScan(
            "com.chatbot.core.config.model",
            "com.chatbot.core.config.environment.model",
            "com.chatbot.core.config.runtime.model",
            "com.chatbot.core.config.repository",
            "com.chatbot.core.config.environment.repository",
            "com.chatbot.core.config.runtime.repository"
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

    // @Bean
    public PlatformTransactionManager configTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(configEntityManagerFactory().getObject());
        return transactionManager;
    }

    // ========================================
    // Message Hub Database Configuration
    // ========================================
    // @Bean
    // Temporarily disable multi-database configuration for development
// @ConfigurationProperties(prefix = "app.datasource.message")
    public DataSource messageDataSource() {
        return DataSourceBuilder.create().build();
    }

    // @Bean
    public LocalContainerEntityManagerFactoryBean messageEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(messageDataSource());
        em.setPackagesToScan(
            "com.chatbot.core.message.model",
            "com.chatbot.core.message.store.model",
            "com.chatbot.core.message.repository",
            "com.chatbot.core.message.store.repository"
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

    // @Bean
    public PlatformTransactionManager messageTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(messageEntityManagerFactory().getObject());
        return transactionManager;
    }
}
