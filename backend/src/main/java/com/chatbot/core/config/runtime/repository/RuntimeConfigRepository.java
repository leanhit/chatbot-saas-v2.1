package com.chatbot.core.config.runtime.repository;

import com.chatbot.core.config.runtime.model.RuntimeConfig;
import com.chatbot.core.config.runtime.model.ConfigScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RuntimeConfigRepository extends JpaRepository<RuntimeConfig, Long> {
    
    Optional<RuntimeConfig> findByConfigKeyAndTenantIdAndUserId(String configKey, Long tenantId, Long userId);
    
    Optional<RuntimeConfig> findByConfigKeyAndTenantId(String configKey, Long tenantId);
    
    Optional<RuntimeConfig> findByConfigKeyAndConfigScope(String configKey, ConfigScope configScope);
    
    List<RuntimeConfig> findByTenantIdOrderByConfigKey(Long tenantId);
    
    List<RuntimeConfig> findByUserIdOrderByConfigKey(Long userId);
    
    List<RuntimeConfig> findByConfigScopeOrderByConfigKey(ConfigScope configScope);
    
    @Query("SELECT c FROM RuntimeConfig c WHERE " +
           "(c.configScope = 'GLOBAL' OR " +
           "(c.configScope = 'TENANT' AND c.tenantId = :tenantId) OR " +
           "(c.configScope = 'USER' AND c.userId = :userId)) " +
           "ORDER BY c.configKey")
    List<RuntimeConfig> findEffectiveConfigs(@Param("tenantId") Long tenantId, @Param("userId") Long userId);
    
    @Query("SELECT c FROM RuntimeConfig c WHERE " +
           "c.configKey = :configKey AND " +
           "(c.configScope = 'GLOBAL' OR " +
           "(c.configScope = 'TENANT' AND c.tenantId = :tenantId) OR " +
           "(c.configScope = 'USER' AND c.userId = :userId)) " +
           "ORDER BY c.configScope DESC")
    List<RuntimeConfig> findEffectiveConfigByKey(@Param("configKey") String configKey, 
                                                 @Param("tenantId") Long tenantId, 
                                                 @Param("userId") Long userId);
    
    @Query("SELECT COUNT(c) FROM RuntimeConfig c WHERE c.configKey = :configKey AND c.configScope = :configScope")
    long countByKeyAndScope(@Param("configKey") String configKey, @Param("configScope") ConfigScope configScope);
}
