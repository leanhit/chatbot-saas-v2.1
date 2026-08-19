package com.chatbot.configs;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Multi-DataSource Flyway Configuration
 * Executes database migrations independently for each Hub & Spoke database datasource
 * before Hibernate EntityManagerFactory instances are initialized.
 */
@Configuration
public class MultiFlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway identityFlyway(@Qualifier("identityDataSource") DataSource identityDataSource) {
        return Flyway.configure()
                .dataSource(identityDataSource)
                .locations("classpath:db/migration/identity")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .outOfOrder(true)
                .table("flyway_identity_schema_history")
                .load();
    }

    @Bean(initMethod = "migrate")
    public Flyway userFlyway(@Qualifier("userDataSource") DataSource userDataSource) {
        return Flyway.configure()
                .dataSource(userDataSource)
                .locations("classpath:db/migration/user")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .outOfOrder(true)
                .table("flyway_user_schema_history")
                .load();
    }

    @Bean(initMethod = "migrate")
    public Flyway tenantFlyway(@Qualifier("tenantDataSource") DataSource tenantDataSource) {
        return Flyway.configure()
                .dataSource(tenantDataSource)
                .locations("classpath:db/migration/tenant")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .outOfOrder(true)
                .table("flyway_tenant_schema_history")
                .load();
    }

    @Bean(initMethod = "migrate")
    public Flyway appFlyway(@Qualifier("appDataSource") DataSource appDataSource) {
        return Flyway.configure()
                .dataSource(appDataSource)
                .locations("classpath:db/migration/app")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .outOfOrder(true)
                .table("flyway_app_schema_history")
                .load();
    }

    @Bean(initMethod = "migrate")
    public Flyway configFlyway(@Qualifier("configDataSource") DataSource configDataSource) {
        return Flyway.configure()
                .dataSource(configDataSource)
                .locations("classpath:db/migration/config")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .outOfOrder(true)
                .table("flyway_config_schema_history")
                .load();
    }

    @Bean(initMethod = "migrate")
    public Flyway messageFlyway(@Qualifier("messageDataSource") DataSource messageDataSource) {
        return Flyway.configure()
                .dataSource(messageDataSource)
                .locations("classpath:db/migration/message")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .outOfOrder(true)
                .table("flyway_message_schema_history")
                .load();
    }

    @Bean(initMethod = "migrate")
    public Flyway sharedFlyway(@Qualifier("sharedDataSource") DataSource sharedDataSource) {
        return Flyway.configure()
                .dataSource(sharedDataSource)
                .locations("classpath:db/migration/shared")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .outOfOrder(true)
                .validateOnMigrate(false)
                .table("flyway_shared_schema_history")
                .load();
    }

    @Bean(initMethod = "migrate")
    public Flyway spokesFlyway(@Qualifier("spokesDataSource") DataSource spokesDataSource) {
        return Flyway.configure()
                .dataSource(spokesDataSource)
                .locations("classpath:db/migration/spokes")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .outOfOrder(true)
                .table("flyway_spokes_schema_history")
                .load();
    }
}
