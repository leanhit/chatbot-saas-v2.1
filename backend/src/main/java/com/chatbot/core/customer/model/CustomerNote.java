package com.chatbot.core.customer.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Agent note attached to a customer profile (Phase 4).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "customer_notes", indexes = {
    @Index(name = "idx_customer_notes_tenant", columnList = "tenant_id"),
    @Index(name = "idx_customer_notes_profile", columnList = "customer_profile_id")
})
@EqualsAndHashCode(callSuper = true)
public class CustomerNote extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_profile_id", nullable = false)
    private Long customerProfileId;

    @Column(name = "author_id")
    private String authorId;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Override
    public Long getId() {
        return id;
    }
}
