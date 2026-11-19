package com.chatbot.core.analytics.dto;

import com.chatbot.core.analytics.model.CustomReport;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class CustomReportRequest {
    @NotBlank(message = "name is required")
    private String name;
    private String description;
    @NotNull(message = "reportType is required")
    private CustomReport.ReportType reportType;
    @Min(value = 1, message = "dateRangeDays must be >= 1")
    @Max(value = 365, message = "dateRangeDays must be <= 365")
    private Integer dateRangeDays;
    private Map<String, Object> filters;
}
