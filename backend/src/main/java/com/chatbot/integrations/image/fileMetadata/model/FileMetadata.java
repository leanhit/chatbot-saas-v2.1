package com.chatbot.integrations.image.fileMetadata.model;

import com.chatbot.integrations.image.category.model.Category;
import com.chatbot.core.identity.model.Auth;
import com.chatbot.core.tenant.infra.BaseTenantEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "file_metadata")
@Data
@EqualsAndHashCode(callSuper = true)
public class FileMetadata extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")   // ép Hibernate tạo cột UUID
    private UUID id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileUrl;

    private long fileSize;
    private String contentType;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime updatedAt;

    private String title;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ElementCollection
    @CollectionTable(
        name = "file_tags",
        joinColumns = @JoinColumn(
            name = "file_id", 
            referencedColumnName = "id", 
            columnDefinition = "UUID"   // ⚡ ép cột FK là UUID luôn
        )
    )
    @Column(name = "tag")
    private List<String> tags;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Auth user;

    @Column(length = 100)   // để filter sau này
    private String code;
    
    @Column(name = "tenant_id")
    private Long tenantId;
    
    @Override
    public UUID getId() {
        return id;
    }
    
    @Override
    public Long getTenantId() {
        return tenantId;
    }
    
    @Override
    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}
