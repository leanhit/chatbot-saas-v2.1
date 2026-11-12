package com.chatbot.core.penny.repository;

import com.chatbot.core.penny.model.PennyBotApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PennyBotApiKeyRepository extends JpaRepository<PennyBotApiKey, UUID> {

    List<PennyBotApiKey> findByBotIdAndTenantIdAndIsActiveTrue(UUID botId, Long tenantId);

    Optional<PennyBotApiKey> findByIdAndBotIdAndTenantId(UUID id, UUID botId, Long tenantId);

    List<PennyBotApiKey> findByBotId(UUID botId);
}
