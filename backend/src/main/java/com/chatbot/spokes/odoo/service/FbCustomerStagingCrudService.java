package com.chatbot.spokes.odoo.service;
import lombok.extern.slf4j.Slf4j;

import com.chatbot.spokes.odoo.model.*;
import com.chatbot.spokes.odoo.repository.FbCustomerStagingRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
public class FbCustomerStagingCrudService {

    private final FbCustomerStagingRepository repository;
    private final ObjectMapper objectMapper;

    public FbCustomerStagingCrudService(FbCustomerStagingRepository repository) {
        this.repository = repository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        this.objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /** 🔹 Tạo hoặc cập nhật thông tin tạm */
    public FbCustomerStaging upsert(FbCustomerStaging customer) {
        Optional<FbCustomerStaging> existing = repository.findById(customer.getPsid());

        if (existing.isPresent()) {
            FbCustomerStaging current = existing.get();

            if (customer.getOwnerId() != null)
                current.setOwnerId(customer.getOwnerId());
            if (customer.getPageId() != null)
                current.setPageId(customer.getPageId());
            if (customer.getStatus() != null)
                current.setStatus(customer.getStatus());

            // ⭐️ BỔ SUNG: CẬP NHẬT TRƯỜNG PHONES ⭐️
            if (customer.getPhones() != null) {
                current.setPhones(customer.getPhones());
            }

            current.setDataJson(mergeJson(current.getDataJson(), customer.getDataJson()));
            return repository.save(current);
        } else {
            if (customer.getDataJson() == null) customer.setDataJson("{}");
            return repository.save(customer);
        }
    }

    /** 🔹 Lấy theo PSID nhưng chỉ trong phạm vi của ownerId và tenantId */
    public Optional<FbCustomerStaging> getByPsid(String psid, String ownerId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        return repository.findByPsidAndOwnerIdAndTenantId(psid, ownerId, tenantId);
    }

    /** 🔹 Lấy tất cả khách hàng theo ownerId và tenantId */
    public List<FbCustomerStaging> getAllByOwnerId(String ownerId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        return repository.findByOwnerIdAndTenantId(ownerId, tenantId);
    }

    /** 🔹 Xóa theo psid, ownerId và tenantId */
    public void delete(String psid, String ownerId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        repository.deleteByPsidAndOwnerIdAndTenantId(psid, ownerId, tenantId);
    }

    /** Hợp nhất JSON cũ và mới */
    private String mergeJson(String oldJson, String newJson) {
        try {
            Map<String, Object> oldMap = objectMapper.readValue(
                    Optional.ofNullable(oldJson).orElse("{}"), new TypeReference<>() {});
            Map<String, Object> newMap = objectMapper.readValue(
                    Optional.ofNullable(newJson).orElse("{}"), new TypeReference<>() {});
            oldMap.putAll(newMap);
            return objectMapper.writeValueAsString(oldMap);
        } catch (Exception e) {
            return newJson;
        }
    }

    public List<FbCustomerStaging> getAll() {
        // Giả định FbCustomerStagingRepository kế thừa JpaRepository hoặc tương đương
        return repository.findAll(); 
    }

    /** 🔹 Cập nhật riêng dataJson và status của user theo psid + ownerId + tenantId */
    @Transactional
    public FbCustomerStaging updateDataJsonAndStatus(String psid, String ownerId, String dataJson, CustomerStatus  status) {
        log.info("Update " + psid + " with dataJson " + dataJson + " and status " + status);
        
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        
        // Tìm bản ghi theo psid + ownerId + tenantId
        Optional<FbCustomerStaging> existingOpt = repository.findByPsidAndOwnerIdAndTenantId(psid, ownerId, tenantId);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Temp user not found for psid=" + psid + " and ownerId=" + ownerId + " and tenantId=" + tenantId);
        }

        FbCustomerStaging existing = existingOpt.get();

        // Cập nhật status nếu có
        if (status != null) {
            existing.setStatus(status);
        }

        // Cập nhật hoặc gộp JSON nếu có
        if (dataJson != null) {
            existing.setDataJson(mergeJson(existing.getDataJson(), dataJson));
        }

        // Lưu lại
        return repository.save(existing);
    }
}
