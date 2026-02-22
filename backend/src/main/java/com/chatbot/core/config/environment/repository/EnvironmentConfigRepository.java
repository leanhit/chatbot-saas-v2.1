package com.chatbot.core.config.environment.repository;

import com.chatbot.core.config.environment.model.EnvironmentConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnvironmentConfigRepository extends JpaRepository<EnvironmentConfig, Long> {
    
    Optional<EnvironmentConfig> findByEnvironmentAndConfigKeyAndIsActive(String environment, String configKey, Boolean isActive);
    
    List<EnvironmentConfig> findByEnvironmentAndIsActiveOrderByConfigKey(String environment, Boolean isActive);
    
    List<EnvironmentConfig> findByConfigKeyAndIsActiveOrderByEnvironment(String configKey, Boolean isActive);
    
    @Query("SELECT ec FROM EnvironmentConfig ec WHERE ec.environment = :environment AND ec.isActive = true ORDER BY ec.configKey")
    List<EnvironmentConfig> findActiveConfigsByEnvironment(@Param("environment") String environment);
    
    @Query("SELECT ec FROM EnvironmentConfig ec WHERE ec.configKey = :configKey AND ec.isActive = true ORDER BY ec.environment")
    List<EnvironmentConfig> findActiveConfigsByKey(@Param("configKey") String configKey);
    
    @Query("SELECT DISTINCT ec.environment FROM EnvironmentConfig ec WHERE ec.isActive = true")
    List<String> findActiveEnvironments();
    
    @Query("SELECT COUNT(ec) FROM EnvironmentConfig ec WHERE ec.environment = :environment AND ec.isActive = true")
    long countActiveConfigsByEnvironment(@Param("environment") String environment);
}
