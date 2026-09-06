package com.chatbot.shared.penny.repository;

import com.chatbot.shared.penny.model.PennyKnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PennyKnowledgeDocumentRepository extends JpaRepository<PennyKnowledgeDocument, UUID> {

    List<PennyKnowledgeDocument> findByBotId(UUID botId);

    List<PennyKnowledgeDocument> findByBotIdAndTenantId(UUID botId, Long tenantId);

    Optional<PennyKnowledgeDocument> findByBotIdAndDocumentName(UUID botId, String documentName);

    List<PennyKnowledgeDocument> findByStatus(String status);

    List<PennyKnowledgeDocument> findByTenantId(Long tenantId);

    @Query("SELECT d FROM PennyKnowledgeDocument d WHERE d.botId = :botId AND d.tenantId = :tenantId AND d.status = :status")
    List<PennyKnowledgeDocument> findByBotIdAndTenantIdAndStatus(
        @Param("botId") UUID botId,
        @Param("tenantId") Long tenantId,
        @Param("status") String status
    );

    @Query("SELECT COUNT(d) FROM PennyKnowledgeDocument d WHERE d.botId = :botId AND d.tenantId = :tenantId")
    long countByBotIdAndTenantId(@Param("botId") UUID botId, @Param("tenantId") Long tenantId);

    void deleteByBotId(UUID botId);
}
