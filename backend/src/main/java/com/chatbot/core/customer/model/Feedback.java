package com.chatbot.core.customer.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Feedback entity for collecting customer feedback on conversations and services
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "feedback")
@EqualsAndHashCode(callSuper = true)
public class Feedback extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_profile_id")
    private Long customerProfileId;

    @Column(name = "conversation_id")
    private Long conversationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedbackType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedbackSource source;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(columnDefinition = "TEXT")
    private String category;

    @Column(name = "is_anonymous")
    private Boolean isAnonymous;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Enumerated(EnumType.STRING)
    private FeedbackStatus status;

    @Column(columnDefinition = "TEXT")
    private String adminResponse;

    @Column(name = "response_at")
    private LocalDateTime responseAt;

    @Override
    public Long getId() {
        return id;
    }

    public enum FeedbackType {
        CONVERSATION, AGENT_PERFORMANCE, BOT_RESPONSE, GENERAL
    }

    public enum FeedbackSource {
        WEBCHAT, FACEBOOK, EMAIL, SURVEY, API
    }

    public enum FeedbackStatus {
        PENDING, REVIEWED, RESPONDED, CLOSED
    }
}
