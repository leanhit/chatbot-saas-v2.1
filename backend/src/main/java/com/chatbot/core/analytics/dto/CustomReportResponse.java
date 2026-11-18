package com.chatbot.core.analytics.dto;

import com.chatbot.core.analytics.model.CustomReport;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class CustomReportResponse {
    private Long id;
    private String name;
    private String description;
    private CustomReport.ReportType reportType;
    private Integer dateRangeDays;
    private Map<String, Object> filters;
    private LocalDateTime lastGeneratedAt;
    private Map<String, Object> lastResult;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CustomReportResponse from(CustomReport r) {
        CustomReportResponse d = new CustomReportResponse();
        d.setId(r.getId());
        d.setName(r.getName());
        d.setDescription(r.getDescription());
        d.setReportType(r.getReportType());
        d.setDateRangeDays(r.getDateRangeDays());
        d.setFilters(r.getFilters());
        d.setLastGeneratedAt(r.getLastGeneratedAt());
        d.setLastResult(r.getLastResult());
        d.setCreatedAt(r.getCreatedAt());
        d.setUpdatedAt(r.getUpdatedAt());
        return d;
    }
}
