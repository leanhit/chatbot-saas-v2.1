package com.chatbot.integrations.image.category.repository;

import com.chatbot.integrations.image.category.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    
    // Tìm theo tên và tenantId
    @Query("SELECT c FROM Category c WHERE c.name = :name AND c.tenantId = :tenantId")
    Optional<Category> findByNameAndTenantId(@Param("name") String name, @Param("tenantId") Long tenantId);
    
    // Tìm theo tên và tenantId = null (global)
    @Query("SELECT c FROM Category c WHERE c.name = :name AND c.tenantId IS NULL")
    Optional<Category> findByNameAndTenantIdIsNull(@Param("name") String name);
    
    @Deprecated
    Optional<Category> findByName(String name);
    
    // Lấy tất cả theo tenantId
    @Query("SELECT c FROM Category c WHERE c.tenantId = :tenantId")
    List<Category> findAllByTenantId(@Param("tenantId") Long tenantId);
    
    // Lấy tất cả global categories (tenantId = null)
    @Query("SELECT c FROM Category c WHERE c.tenantId IS NULL")
    List<Category> findAllByTenantIdIsNull();
}