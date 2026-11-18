package com.chatbot.core.analytics.dto;

import com.chatbot.core.analytics.model.FunnelStage;
import lombok.Data;

@Data
public class FunnelStageRequest {
    private String name;
    private Integer stageOrder;
    private FunnelStage.MatchType matchType;
    private String matchValue;
    private Boolean active;
}
