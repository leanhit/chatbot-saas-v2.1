package com.chatbot.spokes.botpress.repository;

import com.chatbot.spokes.botpress.model.UserIdMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface UserIdMappingRepository extends JpaRepository<UserIdMapping, Long> {
    
    // Tìm theo userId và tenantId
    @Query("SELECT uim FROM UserIdMapping uim WHERE uim.userId = :userId AND uim.tenantId = :tenantId")
    Optional<UserIdMapping> findByUserIdAndTenantId(@Param("userId") String userId, @Param("tenantId") Long tenantId);
    
    @Deprecated
    Optional<UserIdMapping> findByUserId(String userId);
    
    // Lấy tất cả theo tenantId
    @Query("SELECT uim FROM UserIdMapping uim WHERE uim.tenantId = :tenantId")
    List<UserIdMapping> findAllByTenantId(@Param("tenantId") Long tenantId);
}