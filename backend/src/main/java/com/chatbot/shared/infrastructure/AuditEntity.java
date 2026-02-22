package com.chatbot.shared.infrastructure;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class AuditEntity {

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = java.time.LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = java.time.LocalDateTime.now();
    }
}
