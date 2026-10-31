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
@Table(name = "routing_rules")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingRule extends BaseTenantEntity {
    
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
    @Column(name = "rule_type", nullable = false)
    private RoutingRuleType ruleType;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;

    @Column(name = "target_agent_id")
    private Long targetAgentId;

    @Column(name = "target_team_id")
    private Long targetTeamId;

    @Column(name = "target_skill_id")
    private Long targetSkillId;

    @Convert(converter = JsonMapConverter.class)
    @Column(name = "conditions", columnDefinition = "json")
    private Map<String, Object> conditions;

    @Column(name = "max_concurrent_conversations")
    private Integer maxConcurrentConversations;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    public enum RoutingRuleType {
        SKILL_BASED,
        PRIORITY_BASED,
        ROUND_ROBIN,
        LEAST_BUSY,
        CUSTOM
    }

    public enum TargetType {
        AGENT,
        TEAM,
        SKILL,
        QUEUE
    }
}
