package com.chatbot.core.app.guard.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "app_guards")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class AppGuard extends BaseTenantEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "app_id", nullable = false)
    private Long appId;
    
    @Column(name = "guard_name", nullable = false)
    private String guardName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "guard_type", nullable = false)
    private GuardType guardType;
    
    @Column(name = "description")
    private String description;
    
    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Builder.Default
    @Column(name = "priority")
    private Integer priority = 0;
    
    @Builder.Default
    @OneToMany(mappedBy = "appGuard", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GuardRule> rules = new java.util.ArrayList<>();
    
    @Override
    public Object getId() {
        return id;
    }
    
    @Override
    public Long getTenantId() {
        return super.getTenantId();
    }
    
    @Override
    public void setTenantId(Long tenantId) {
        super.setTenantId(tenantId);
    }
    
    @PrePersist
    protected void onCreate() {
        super.onCreate();
    }
}
