package com.chatbot.core.routing.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "supervisor_sessions")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupervisorSession extends BaseTenantEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Override
    public Object getId() {
        return id;
    }

    @Column(name = "supervisor_id", nullable = false)
    private Long supervisorId;

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false)
    private SessionType sessionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_status", nullable = false)
    private SessionStatus sessionStatus;

    @Column(name = "monitored_agent_id")
    private Long monitoredAgentId;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "is_silent")
    @Builder.Default
    private Boolean isSilent = true;

    @Column(name = "can_intervene")
    @Builder.Default
    private Boolean canIntervene = false;

    @Column(name = "intervention_count")
    @Builder.Default
    private Integer interventionCount = 0;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "rating")
    private Integer rating;

    public enum SessionType {
        MONITORING,
        COACHING,
        TAKEOVER,
        ASSISTING
    }

    public enum SessionStatus {
        ACTIVE,
        ENDED,
        INTERRUPTED
    }
}
