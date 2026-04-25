package com.chatbot.core.license.model;

import com.chatbot.core.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "licenses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "plan_name", nullable = false)
    private String planName;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @ElementCollection
    @CollectionTable(name = "license_features", joinColumns = @JoinColumn(name = "license_id"))
    @Column(name = "feature")
    private List<String> features;

    @ElementCollection
    @CollectionTable(name = "license_modules", joinColumns = @JoinColumn(name = "license_id"))
    @Column(name = "module")
    private List<String> modules;

    @ElementCollection
    @CollectionTable(name = "license_limits", joinColumns = @JoinColumn(name = "license_id"))
    @MapKeyColumn(name = "limit_key")
    @Column(name = "limit_value")
    private java.util.Map<String, Integer> limits;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }

    public boolean isValid() {
        return isActive && !isExpired();
    }

    public boolean hasFeature(String feature) {
        return features != null && features.contains(feature);
    }

    public boolean hasModule(String module) {
        return modules != null && modules.contains(module);
    }

    public Integer getLimit(String limitKey) {
        return limits != null ? limits.get(limitKey) : null;
    }
}
