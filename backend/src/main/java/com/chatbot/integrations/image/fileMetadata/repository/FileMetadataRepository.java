package com.chatbot.integrations.image.fileMetadata.repository;

import com.chatbot.integrations.image.fileMetadata.model.FileMetadata;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {

    // Lấy 1 file kèm category + tags theo tenantId
    @EntityGraph(attributePaths = {"category", "tags"})
    @Query("SELECT fm FROM FileMetadata fm WHERE fm.id = :id AND fm.tenantId = :tenantId")
    Optional<FileMetadata> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") Long tenantId);
    
    // Deprecated method without tenantId
    @Deprecated
    @Override
    @EntityGraph(attributePaths = {"category", "tags"})
    Optional<FileMetadata> findById(UUID id);

    // Lấy tất cả file kèm category + tags theo tenantId
    @EntityGraph(attributePaths = {"category", "tags"})
    @Query("SELECT fm FROM FileMetadata fm WHERE fm.tenantId = :tenantId")
    List<FileMetadata> findAllByTenantId(@Param("tenantId") Long tenantId);
    
    // Deprecated method without tenantId
    @Deprecated
    @Override
    @EntityGraph(attributePaths = {"category", "tags"})
    List<FileMetadata> findAll();
}
