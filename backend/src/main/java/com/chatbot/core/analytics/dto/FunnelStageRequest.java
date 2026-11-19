package com.chatbot.core.analytics.dto;

import com.chatbot.core.analytics.model.FunnelStage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class FunnelStageRequest {
    @NotBlank(message = "name is required")
    private String name;
    @NotNull(message = "stageOrder is required")
    @PositiveOrZero(message = "stageOrder must be >= 0")
    private Integer stageOrder;
    @NotNull(message = "matchType is required")
    private FunnelStage.MatchType matchType;
    private String matchValue;
    private Boolean active;
}
