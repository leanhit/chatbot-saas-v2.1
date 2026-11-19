package com.chatbot.core.analytics.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReportScheduleRequest {
    private Long reportId; // required on create, ignored on update
    @NotBlank(message = "cronExpression is required")
    private String cronExpression;
    private Boolean enabled;
}
