package com.chatbot.core.analytics.dto;

import com.chatbot.core.analytics.model.CustomReport;
import lombok.Data;

import java.util.Map;

@Data
public class CustomReportRequest {
    private String name;
    private String description;
    private CustomReport.ReportType reportType;
    private Integer dateRangeDays;
    private Map<String, Object> filters;
}
