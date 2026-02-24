package com.chatbot.spokes.botpress.service;

import com.chatbot.spokes.botpress.model.UserIdMapping;
import com.chatbot.spokes.botpress.repository.UserIdMappingRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserIdMappingService {
    private final UserIdMappingRepository userIdMappingRepository;

    public UserIdMappingService(UserIdMappingRepository userIdMappingRepository) {
        this.userIdMappingRepository = userIdMappingRepository;
    }

    @Transactional
    public Long getOrCreateInternalId(String userId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context khi mapping userId");
        }
        
        return userIdMappingRepository.findByUserIdAndTenantId(userId, tenantId)
                .map(UserIdMapping::getInternalId)
                .orElseGet(() -> {
                    UserIdMapping newMapping = new UserIdMapping();
                    newMapping.setUserId(userId);
                    // tenantId sẽ được tự động gán trong @PrePersist của BaseTenantEntity
                    return userIdMappingRepository.save(newMapping).getInternalId();
                });
    }
}