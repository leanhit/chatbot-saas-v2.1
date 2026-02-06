package com.chatbot.modules.config.core.repository;

import com.chatbot.modules.config.core.model.ConfigEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Config Entry repository for Config Hub
 * Data access operations for configuration entries
 */
@Repository
public interface ConfigEntryRepository extends JpaRepository<ConfigEntry, Long> {

    /**
     * Find config by key and scope
     */
    Optional<ConfigEntry> findByKeyAndScope(String key, ConfigEntry.ConfigScope scope);

    /**
     * Find config by key, scope, and scopeId
     */
    Optional<ConfigEntry> findByKeyAndScopeAndScopeId(String key, ConfigEntry.ConfigScope scope, UUID scopeId);

    /**
     * Find all configs by scope
     */
    List<ConfigEntry> findByScope(ConfigEntry.ConfigScope scope);

    /**
     * Find all configs by scope and scopeId
     */
    List<ConfigEntry> findByScopeAndScopeId(ConfigEntry.ConfigScope scope, UUID scopeId);

    /**
     * Find system config or fallback to default
     * Returns tenant-specific config if exists, otherwise system config
     */
    @Query("SELECT c FROM ConfigEntry c WHERE c.key = :key AND c.scope = 'TENANT' AND c.scopeId = :tenantId " +
           "UNION " +
           "SELECT c FROM ConfigEntry c WHERE c.key = :key AND c.scope = 'SYSTEM' " +
           "ORDER BY CASE WHEN c.scope = 'TENANT' THEN 1 ELSE 2 END " +
           "LIMIT 1")
    Optional<ConfigEntry> findTenantConfigOrSystem(@Param("key") String key, @Param("tenantId") UUID tenantId);

    /**
     * Find tenant app config or fallback to app config or system config
     * Priority: TENANT_APP > APP > SYSTEM
     */
    @Query("SELECT c FROM ConfigEntry c WHERE c.key = :key AND c.scope = 'TENANT_APP' AND c.scopeId = :tenantId " +
           "UNION " +
           "SELECT c FROM ConfigEntry c WHERE c.key = :key AND c.scope = 'APP' " +
           "UNION " +
           "SELECT c FROM ConfigEntry c WHERE c.key = :key AND c.scope = 'SYSTEM' " +
           "ORDER BY CASE " +
           "  WHEN c.scope = 'TENANT_APP' THEN 1 " +
           "  WHEN c.scope = 'APP' THEN 2 " +
           "  WHEN c.scope = 'SYSTEM' THEN 3 " +
           "END " +
           "LIMIT 1")
    Optional<ConfigEntry> findAppConfigWithFallback(@Param("key") String key, @Param("tenantId") UUID tenantId);
}
