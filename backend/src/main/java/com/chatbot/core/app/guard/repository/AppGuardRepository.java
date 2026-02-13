package com.chatbot.core.app.guard.repository;

import com.chatbot.core.app.guard.model.AppGuard;
import com.chatbot.core.app.guard.model.GuardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppGuardRepository extends JpaRepository<AppGuard, Long> {
    
    List<AppGuard> findByAppId(Long appId);
    
    List<AppGuard> findByAppIdAndIsActive(Long appId, Boolean isActive);
    
    List<AppGuard> findByGuardType(GuardType guardType);
    
    Optional<AppGuard> findByAppIdAndGuardName(Long appId, String guardName);
    
    boolean existsByAppId(Long appId);
    
    boolean existsByAppIdAndGuardName(Long appId, String guardName);
    
    boolean existsByAppIdAndGuardNameAndIdNot(Long appId, String guardName, Long id);
}
