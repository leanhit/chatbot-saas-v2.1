package com.chatbot.core.analytics.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Custom report definition. Reports are generated on demand or via schedule
 * from aggregated analytics data (tenant + date range scoped).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "custom_report", indexes = {
    @Index(name = "idx_custom_report_tenant", columnList = "tenant_id, is_deleted")
})
@EqualsAndHashCode(callSuper = true)
public class CustomReport extends BaseTenantEntity {

    public enum ReportType {
        CONVERSATIONS,
        SENTIMENT,
        AI_USAGE,
        FEEDBACK,
        FUNNEL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 20)
    private ReportType reportType;

    @Column(name = "date_range_days", nullable = false)
    private Integer dateRangeDays = 30;

    /** Optional extra filters, e.g. {"status":"CLOSED","channel":"FACEBOOK"} */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "filters")
    private Map<String, Object> filters;

    @Column(name = "last_generated_at")
    private LocalDateTime lastGeneratedAt;

    /** Latest generated report payload (JSON). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "last_result")
    private Map<String, Object> lastResult;

    @Override
    public Long getId() {
        return id;
    }
}
