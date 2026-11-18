package com.chatbot.core.analytics.dto;

import com.chatbot.core.analytics.model.ReportSchedule;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportScheduleResponse {
    private Long id;
    private Long reportId;
    private String cronExpression;
    private Boolean enabled;
    private LocalDateTime nextRunAt;
    private LocalDateTime lastRunAt;
    private String lastRunStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReportScheduleResponse from(ReportSchedule s) {
        ReportScheduleResponse r = new ReportScheduleResponse();
        r.setId(s.getId());
        r.setReportId(s.getReportId());
        r.setCronExpression(s.getCronExpression());
        r.setEnabled(s.getEnabled());
        r.setNextRunAt(s.getNextRunAt());
        r.setLastRunAt(s.getLastRunAt());
        r.setLastRunStatus(s.getLastRunStatus());
        r.setCreatedAt(s.getCreatedAt());
        r.setUpdatedAt(s.getUpdatedAt());
        return r;
    }
}
