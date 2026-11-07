package com.chatbot.core.analytics.repository;

import com.chatbot.core.analytics.model.FunnelStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FunnelStageRepository extends JpaRepository<FunnelStage, Long> {

    List<FunnelStage> findByTenantIdAndActiveTrueOrderByStageOrderAsc(Long tenantId);

    List<FunnelStage> findByTenantIdOrderByStageOrderAsc(Long tenantId);
}
