package com.chatbot.core.analytics.dto;

import lombok.Data;

@Data
public class ReportScheduleRequest {
    private Long reportId;
    private String cronExpression;
    private Boolean enabled;
}
