package com.chatbot.modules.identity.config;

import com.chatbot.modules.identity.security.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Identity Hub configuration - isolated from legacy auth
 * Uses same datasource but separate entity management
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
@EnableJpaRepositories(
    basePackages = {
        "com.chatbot.modules.identity.repository",
        "com.chatbot.modules.tenant.core.repository",
        "com.chatbot.modules.tenant.membership.repository",
        "com.chatbot.modules.app.core.repository",
        "com.chatbot.modules.facebook.facebook.connection.repository",
        "com.chatbot.modules.billing.core.repository",
        "com.chatbot.modules.wallet.core.repository",
        "com.chatbot.modules.messagehub.core.repository",
        "com.chatbot.modules.config.core.repository",
        "com.chatbot.modules.penny.repository",
        "com.chatbot.modules.penny.rules"
    },
    entityManagerFactoryRef = "identityEntityManagerFactory",
    transactionManagerRef = "identityTransactionManager"
)
@EnableTransactionManagement
public class IdentityConfig {
    
    /**
     * Password encoder for credential hashing
     */
    @Bean("identityPasswordEncoder")
    public PasswordEncoder identityPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Identity Hub Entity Manager Factory
     * Scans only identity module entities
     */
    @Bean(name = "identityEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean identityEntityManagerFactory(
            DataSource dataSource) {
        
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan(
            "com.chatbot.modules.identity.model",
            "com.chatbot.modules.tenant.core.model",
            "com.chatbot.modules.tenant.membership.model",
            "com.chatbot.modules.app.core.model",
            "com.chatbot.modules.facebook.facebook.connection.model",
            "com.chatbot.modules.billing.core.model",
            "com.chatbot.modules.wallet.core.model",
            "com.chatbot.modules.messagehub.core.model",
            "com.chatbot.modules.config.core.model",
            "com.chatbot.modules.penny.model",
            "com.chatbot.modules.penny.rules"
        );
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        emf.setJpaVendorAdapter(vendorAdapter);
        
        // Set JPA properties
        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        jpaProperties.put("hibernate.hbm2ddl.auto", "create");
        jpaProperties.put("hibernate.show_sql", "true");
        jpaProperties.put("hibernate.format_sql", "true");
        jpaProperties.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        jpaProperties.put("hibernate.implicit_naming_strategy", "org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl");
        
        emf.setJpaPropertyMap(jpaProperties);
        emf.setPersistenceUnitName("identity");
        
        return emf;
    }
    
    /**
     * Identity Hub Transaction Manager
     * Separate transaction management for identity operations
     */
    @Bean(name = "identityTransactionManager")
    public PlatformTransactionManager identityTransactionManager(
            LocalContainerEntityManagerFactoryBean identityEntityManagerFactory) {
        
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(
            identityEntityManagerFactory.getObject());
        
        return transactionManager;
    }
}
