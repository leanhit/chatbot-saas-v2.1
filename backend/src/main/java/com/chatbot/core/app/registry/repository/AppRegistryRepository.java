package com.chatbot.core.app.registry.repository;

import com.chatbot.core.app.registry.model.AppRegistry;
import com.chatbot.core.app.registry.model.AppType;
import com.chatbot.core.app.registry.model.AppStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppRegistryRepository extends JpaRepository<AppRegistry, Long> {
    
    Optional<AppRegistry> findByName(String name);
    
    Optional<AppRegistry> findByNameAndIsActive(String name, Boolean isActive);
    
    List<AppRegistry> findByAppType(AppType appType);
    
    List<AppRegistry> findByStatus(AppStatus status);
    
    List<AppRegistry> findByAppTypeAndStatus(AppType appType, AppStatus status);
    
    List<AppRegistry> findByIsActive(Boolean isActive);
    
    List<AppRegistry> findByIsPublic(Boolean isPublic);
    
    List<AppRegistry> findByCreatedBy(String createdBy);
    
    @Query("SELECT a FROM AppRegistry a WHERE " +
           "(:name IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:appType IS NULL OR a.appType = :appType) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:isActive IS NULL OR a.isActive = :isActive)")
    Page<AppRegistry> searchApps(@Param("name") String name,
                                 @Param("appType") AppType appType,
                                 @Param("status") AppStatus status,
                                 @Param("isActive") Boolean isActive,
                                 Pageable pageable);
    
    @Query("SELECT COUNT(a) FROM AppRegistry a WHERE a.appType = :appType AND a.status = :status")
    long countByAppTypeAndStatus(@Param("appType") AppType appType, @Param("status") AppStatus status);
    
    @Query("SELECT a FROM AppRegistry a WHERE a.apiEndpoint IS NOT NULL AND a.apiEndpoint != ''")
    List<AppRegistry> findAppsWithApiEndpoints();
    
    @Query("SELECT a FROM AppRegistry a WHERE a.webhookUrl IS NOT NULL AND a.webhookUrl != ''")
    List<AppRegistry> findAppsWithWebhooks();
    
    boolean existsByName(String name);
    
    boolean existsByNameAndIdNot(String name, Long id);
}
