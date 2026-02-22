package com.chatbot.spokes.minio.image.fileMetadata.model;

import com.chatbot.spokes.minio.image.category.model.Category;
import com.chatbot.core.user.model.User;
import com.chatbot.core.tenant.infra.BaseTenantEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "file_metadata")
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
    private User user;

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
    
    // Manual getters/setters for Lombok compatibility issues
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
