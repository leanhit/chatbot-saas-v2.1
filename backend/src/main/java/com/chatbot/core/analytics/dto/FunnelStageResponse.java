package com.chatbot.core.analytics.dto;

import com.chatbot.core.analytics.model.FunnelStage;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FunnelStageResponse {
    private Long id;
    private String name;
    private Integer stageOrder;
    private FunnelStage.MatchType matchType;
    private String matchValue;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FunnelStageResponse from(FunnelStage s) {
        FunnelStageResponse r = new FunnelStageResponse();
        r.setId(s.getId());
        r.setName(s.getName());
        r.setStageOrder(s.getStageOrder());
        r.setMatchType(s.getMatchType());
        r.setMatchValue(s.getMatchValue());
        r.setActive(s.getActive());
        r.setCreatedAt(s.getCreatedAt());
        r.setUpdatedAt(s.getUpdatedAt());
        return r;
    }
}
