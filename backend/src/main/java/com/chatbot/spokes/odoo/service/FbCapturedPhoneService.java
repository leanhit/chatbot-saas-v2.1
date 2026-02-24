package com.chatbot.spokes.odoo.service;

import com.chatbot.spokes.odoo.model.FbCapturedPhone;
import com.chatbot.spokes.odoo.repository.FbCapturedPhoneRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors; 

@Service
@Slf4j
public class FbCapturedPhoneService {

    private final FbCapturedPhoneRepository repository;

    public FbCapturedPhoneService(FbCapturedPhoneRepository repository) {
        this.repository = repository;
    }

    /**
     * Lưu SĐT mới vào bảng fb_captured_phone.
     * 💡 LOGIC MỚI: Chỉ lưu nếu SĐT CHƯA TỒN TẠI VỚI ownerId CỤ THỂ.
     */
    public boolean saveNewPhoneNumber(String ownerId, String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank() || ownerId == null || ownerId.isBlank()) {
            log.warn("⚠️ [SKIP CAPTURE] Dữ liệu đầu vào không hợp lệ: ownerId='{}', phoneNumber='{}'", ownerId, phoneNumber);
            return false;
        }
        
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            log.error("❌ Không tìm thấy tenant ID trong context khi lưu SĐT");
            return false;
        }
        
        // ✅ KIỂM TRA SỰ TỒN TẠI THEO phoneNumber, ownerId VÀ tenantId
        if (repository.existsByPhoneNumberAndOwnerIdAndTenantId(phoneNumber, ownerId, tenantId)) { 
            log.debug("⚠️ [SKIP CAPTURE] SĐT '{}' đã tồn tại với ownerId={} và tenantId={} trong fb_captured_phone.", phoneNumber, ownerId, tenantId);
            return false;
        }
        // Lưu ý: Do SĐT có unique=true, nên vẫn có thể xảy ra race condition khi 2 owner khác nhau cố gắng lưu cùng 1 SĐT
        // Tuy nhiên, logic kiểm tra kết hợp ownerId đã đảm bảo tính "độc nhất trong phạm vi owner".

        try {
            FbCapturedPhone newRecord = new FbCapturedPhone();
            newRecord.setOwnerId(ownerId);
            newRecord.setPhoneNumber(phoneNumber);
            // tenantId sẽ được tự động gán trong @PrePersist của BaseTenantEntity
            // Lưu SĐT mới
            repository.save(newRecord); 
            log.info("✅ [CAPTURED] Đã lưu SĐT mới: '{}' vào fb_captured_phone cho ownerId={} và tenantId={}", phoneNumber, ownerId, tenantId);
            return true;
        } catch (DataIntegrityViolationException e) {
            // Lỗi này xảy ra nếu SĐT đã tồn tại (unique=true) dù là với owner khác, 
            // HOẶC nếu có race condition (2 luồng cùng cố gắng lưu cùng 1 SĐT + ownerId)
            log.warn("❌ [CONCURRENT CAPTURE] Lỗi trùng lặp SĐT '{}' khi lưu (race condition). ownerId={}", phoneNumber, ownerId);
            return false;
        } catch (Exception e) {
            log.error("💥 [DB ERROR] Lỗi không xác định khi lưu SĐT '{}'. ownerId={} | msg={}", phoneNumber, ownerId, e.getMessage());
            return false;
        }
    }
    
    // -------------------------------------------------------------------------
    // 📞 Phương thức trả về List<FbCapturedPhone> (Sửa Lỗi 1)
    // -------------------------------------------------------------------------
    
    /** * 💡 [HÀM NÀY ĐƯỢC GỌI BỞI CONTROLLER]
     * Lấy danh sách các Entity FbCapturedPhone theo OwnerId.
     * @param ownerId ID của chủ sở hữu
     * @return List các bản ghi FbCapturedPhone
     */
    public List<FbCapturedPhone> getAllPhoneRecordsByOwner(String ownerId) {
        log.info("🔍 [QUERY] Lấy các bản ghi SĐT đã bắt được cho ownerId={}", ownerId);
        
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            log.error("❌ Không tìm thấy tenant ID trong context khi lấy danh sách SĐT");
            return List.of();
        }
        
        return repository.findByOwnerIdAndTenantId(ownerId, tenantId); 
    }
    
    // -------------------------------------------------------------------------
    // Phương thức trả về List<String> (không bắt buộc, nhưng là logic tốt)
    // -------------------------------------------------------------------------

    public List<String> getAllPhonesByOwner(String ownerId) {
        return this.getAllPhoneRecordsByOwner(ownerId).stream()
            .map(FbCapturedPhone::getPhoneNumber)
            .collect(Collectors.toList());
    }
}