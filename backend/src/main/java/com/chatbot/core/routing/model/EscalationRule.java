package com.chatbot.core.routing.model;

import com.chatbot.core.routing.converter.JsonMapConverter;
import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

@Entity
@Table(name = "escalation_rules")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscalationRule extends BaseTenantEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Override
    public Object getId() {
        return id;
    }

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false)
    private TriggerType triggerType;

    @Column(name = "trigger_value")
    private String triggerValue;

    @Column(name = "threshold_value")
    private Integer thresholdValue;

    @Column(name = "time_threshold_minutes")
    private Integer timeThresholdMinutes;

    @Convert(converter = JsonMapConverter.class)
    @Column(name = "conditions", columnDefinition = "json")
    private Map<String, Object> conditions;

    @Enumerated(EnumType.STRING)
    @Column(name = "escalation_action", nullable = false)
    private EscalationAction escalationAction;

    @Column(name = "escalate_to_agent_id")
    private Long escalateToAgentId;

    @Column(name = "escalate_to_team_id")
    private Long escalateToTeamId;

    @Column(name = "escalate_to_supervisor")
    @Builder.Default
    private Boolean escalateToSupervisor = false;

    @Column(name = "notify_supervisor")
    @Builder.Default
    private Boolean notifySupervisor = true;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public enum TriggerType {
        WAIT_TIME_EXCEEDED,
        NO_RESPONSE,
        LOW_CUSTOMER_SATISFACTION,
        HIGH_PRIORITY_CUSTOMER,
        CUSTOMER_TIER,
        CUSTOM_CONDITION
    }

    public enum EscalationAction {
        ASSIGN_TO_AGENT,
        ASSIGN_TO_TEAM,
        ASSIGN_TO_SUPERVISOR,
        MOVE_TO_QUEUE,
        SEND_NOTIFICATION
    }
}
