package com.chatbot.modules.facebook.user.model;

import com.chatbot.modules.facebook.connection.model.FacebookConnection;
import com.chatbot.modules.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "facebook_users",
       indexes = {
           @Index(name = "idx_facebook_user_psid", columnList = "psid"),
           @Index(name = "idx_facebook_user_odoo_partner", columnList = "odoo_partner_id")
       },
       uniqueConstraints = @UniqueConstraint(
           name = "uk_facebook_user_tenant_psid",
           columnNames = {"tenant_id", "psid"}
       ))
@EqualsAndHashCode(callSuper = true)
public class FacebookUser extends BaseTenantEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String psid;

    @Column(name = "name")
    private String name;

    @Column(name = "profile_pic", length = 1000)
    private String profilePic;

    @Column(name = "odoo_partner_id")
    private Integer odooPartnerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false),
        @JoinColumn(name = "page_id", referencedColumnName = "page_id", insertable = false, updatable = false)
    })
    private FacebookConnection page;

    @Column(name = "page_id", nullable = false)
    private String pageId;

    @Column(name = "last_interaction")
    private LocalDateTime lastInteraction;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.lastInteraction == null) {
            this.lastInteraction = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateFromFacebook(String name, String profilePic) {
        this.name = name != null ? name : this.name;
        this.profilePic = profilePic != null ? profilePic : this.profilePic;
        this.updatedAt = LocalDateTime.now();
        this.lastInteraction = LocalDateTime.now();
    }

    public void updateOdooPartnerId(Integer odooPartnerId) {
        this.odooPartnerId = odooPartnerId != null ? odooPartnerId : this.odooPartnerId;
        this.updatedAt = LocalDateTime.now();
    }
}