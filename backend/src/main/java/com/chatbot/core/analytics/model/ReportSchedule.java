package com.chatbot.core.analytics.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Schedule for automatic report generation.
 * nextRunAt is computed from cron expression; lastRunAt tracks execution.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "report_schedule", indexes = {
    @Index(name = "idx_report_schedule_due", columnList = "enabled, next_run_at")
})
@EqualsAndHashCode(callSuper = true)
public class ReportSchedule extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id", nullable = false)
    private Long reportId;

    /** Cron expression, e.g. "0 0 8 * * *" = daily 08:00 */
    @Column(name = "cron_expression", nullable = false, length = 50)
    private String cronExpression;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "next_run_at")
    private LocalDateTime nextRunAt;

    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;

    @Column(name = "last_run_status", length = 20)
    private String lastRunStatus;

    @Override
    public Long getId() {
        return id;
    }
}
