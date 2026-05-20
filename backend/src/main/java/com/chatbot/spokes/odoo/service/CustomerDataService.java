package com.chatbot.spokes.odoo.service;

import com.chatbot.spokes.facebook.connection.model.FacebookConnection;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.spokes.odoo.model.CustomerStatus;
import com.chatbot.spokes.odoo.model.FbCapturedPhone;
import com.chatbot.spokes.odoo.model.FbCustomerStaging;
import com.chatbot.spokes.odoo.model.CustomerInfo;
import lombok.extern.slf4j.Slf4j;
import com.chatbot.spokes.odoo.service.FbCapturedPhoneService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CustomerDataService {

    private final FacebookConnectionRepository connectionRepository;
    private final FbCustomerStagingCrudService crudService;
    private final CustomerInfoExtractor infoExtractor;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FbCapturedPhoneService phoneService;

    public CustomerDataService(
            FbCustomerStagingCrudService crudService,
            FacebookConnectionRepository connectionRepository,
            CustomerInfoExtractor infoExtractor,
            FbCapturedPhoneService phoneService
    ) {
        this.crudService = crudService;
        this.connectionRepository = connectionRepository;
        this.infoExtractor = infoExtractor;
        this.phoneService = phoneService;
    }

    public boolean processAndAccumulate(String pageId, String senderId, String text) {
        log.info("➡️ [DEBUG] Vào processAndAccumulate() | pageId={} | senderId={} | rawText='{}'", pageId, senderId, text);

        if (text == null || text.isBlank()) {
            log.warn("⚠️ [SKIP] Tin nhắn rỗng hoặc null từ senderId={}", senderId);
            return false;
        }

        String currentOwnerId = null;

        try {
            // B1. Lấy tenantId và ownerId
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                log.warn("⚠️ [TENANT NOT FOUND] Không tìm thấy tenantId trong context cho pageId={}", pageId);
                return false;
            }

            // B2. Tìm kết nối với tenantId và pageId
            currentOwnerId = connectionRepository.findByTenantIdAndPageId(tenantId, pageId)
                    .map(FacebookConnection::getOwnerId)
                    .orElse(null);

            if (currentOwnerId == null) {
                log.warn("⚠️ [OWNER NOT FOUND] Không tìm thấy ownerId cho tenant={} và pageId={}", tenantId, pageId);
                return false;
            }

            final String finalOwnerId = currentOwnerId;
            log.debug("👤 [OWNER] pageId={} thuộc ownerId={}", pageId, finalOwnerId);

            // B2. Trích xuất thông tin SĐT và Email
            log.debug("🔍 [EXTRACT] Bắt đầu trích xuất thông tin từ text='{}'", text);
            CustomerInfo info = infoExtractor.extractInfo(text);
            String newPhone = info.getPhone();
            String newEmail = info.getEmail();

            log.info("📞📧 [EXTRACT RESULT] PSID={} | phone={} | email={}", senderId, newPhone, newEmail);

            // B3. Bỏ qua nếu không bắt được thông tin nào hữu ích
            if (newPhone == null && newEmail == null) {
                log.debug("ℹ️ [NO INFO] Không trích xuất được số điện thoại hoặc email hợp lệ từ text='{}'", text);
                return false;
            }

            // 🔹 B4. Lấy hoặc tạo mới record staging từ DB để giữ luồng hoàn toàn Stateless
            FbCustomerStaging staging = crudService
                    .getByPsid(senderId, finalOwnerId)
                    .orElseGet(() -> {
                        log.info("🆕 [NEW RECORD] Tạo mới bản ghi staging cho PSID={}", senderId);
                        FbCustomerStaging c = new FbCustomerStaging(senderId);
                        c.setOwnerId(finalOwnerId);
                        c.setPageId(pageId);
                        c.setStatus(CustomerStatus.PENDING);
                        return c;
                    });

            // 🔹 B5. Đọc dữ liệu tạm từ DB
            Map<String, String> current = new HashMap<>();
            if (staging.getDataJson() != null && !staging.getDataJson().trim().isEmpty()) {
                try {
                    current = objectMapper.readValue(staging.getDataJson(), new TypeReference<Map<String, String>>() {});
                } catch (Exception e) {
                    log.error("💥 [JSON ERROR] Parse dataJson cũ lỗi cho PSID={} | msg={}", senderId, e.getMessage());
                }
            }

            // Cập nhật dữ liệu mới thu thập
            if (newPhone != null) {
                current.put("phone", newPhone);
            }
            if (newEmail != null) {
                current.put("email", newEmail);
            }

            // 🔹 B6. Lưu SĐT vào bảng captured_phone (nếu có SĐT mới)
            if (newPhone != null) {
                boolean isNewPhoneForOwner = phoneService.saveNewPhoneNumber(finalOwnerId, newPhone);
                if (isNewPhoneForOwner) {
                    log.info("✅ [FB_PHONE_NEW] SĐT '{}' đã được ghi nhận mới vào fb_captured_phone.", newPhone);
                } else {
                    log.warn("ℹ️ [PHONE EXISTS] SĐT '{}' đã tồn tại trong fb_captured_phone.", newPhone);
                }

                // Cập nhật danh sách phonesSet tích lũy trong staging
                Set<String> phonesSet;
                try {
                    String existingPhonesJson = staging.getPhones() != null ? staging.getPhones() : "[]";
                    phonesSet = objectMapper.readValue(existingPhonesJson, new TypeReference<Set<String>>() {});
                } catch (Exception e) {
                    log.error("💥 [JSON ERROR] Parse phones JSON lỗi cho PSID={} | msg={}", senderId, e.getMessage());
                    phonesSet = new HashSet<>();
                }

                if (phonesSet.add(newPhone)) {
                    log.info("📲 [PHONES UPDATED] Thêm SĐT '{}' vào Staging.phones cho PSID={}", newPhone, senderId);
                }

                try {
                    staging.setPhones(objectMapper.writeValueAsString(phonesSet));
                } catch (Exception e) {
                    log.error("💥 [JSON ERROR] Serialize phonesSet lỗi cho PSID={} | msg={}", senderId, e.getMessage());
                }
            }

            // 🔹 B7. Cập nhật dataJson và trạng thái hoàn thành
            staging.setDataJson(toJson(current));

            // Trạng thái được coi là COMPLETED khi có ít nhất SĐT hoặc Email
            boolean isComplete = current.containsKey("phone") || current.containsKey("email");
            if (isComplete) {
                staging.setStatus(CustomerStatus.COMPLETED);
            } else {
                staging.setStatus(CustomerStatus.PENDING);
            }

            staging.setUpdatedAt(LocalDateTime.now());
            crudService.upsert(staging);
            log.info("💾 [OK] Đã ghi CSDL thành công cho PSID={} | status={}", senderId, staging.getStatus());

            return true;

        } catch (Exception e) {
            log.error("❌ [EXCEPTION] Lỗi khi xử lý PSID={} | msg={}", senderId, e.getMessage(), e);

            try {
                final String finalOwnerId = currentOwnerId != null ? currentOwnerId : "UNKNOWN_OWNER";
                FbCustomerStaging failedRecord = crudService.getByPsid(senderId, finalOwnerId)
                        .orElseGet(() -> {
                            FbCustomerStaging c = new FbCustomerStaging(senderId);
                            c.setOwnerId(finalOwnerId);
                            c.setPageId(pageId);
                            return c;
                        });

                failedRecord.setStatus(CustomerStatus.FAILED);
                failedRecord.setUpdatedAt(LocalDateTime.now());
                crudService.upsert(failedRecord);

                log.warn("⚠️ [FAILED RECORD SAVED] Đã set trạng thái FAILED cho PSID={}", senderId);
            } catch (Exception inner) {
                log.error("💥 [INNER ERROR] Không thể lưu trạng thái FAILED cho PSID={} | msg={}", senderId, inner.getMessage());
            }
        }

        log.info("🏁 [EXIT] processAndAccumulate() | PSID={}", senderId);
        return false;
    }
    
    /** Kiểm tra chỉ cần phone hoặc email là đủ */
    private boolean isDataComplete(Map<String, String> data) {
        return data.containsKey("phone") || data.containsKey("email");
    }

    /** Chuyển map → JSON */
    private String toJson(Map<String, String> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.error("💥 [JSON SERIALIZE ERROR] Không thể chuyển Map thành JSON: {}", e.getMessage());
            return "{}"; 
        }
    }

    /** * 🛠️ [MULTI-TENANT SAFE] Lấy danh sách khách hàng hoàn tất CỦA MỘT OWNER CỤ THỂ. */
    public List<FbCustomerStaging> getCompletedCustomersByOwner(String ownerId) {
        return crudService.getAllByOwnerId(ownerId).stream()
                .filter(c -> c.getStatus() == CustomerStatus.COMPLETED)
                .toList();
    }

    /** Lấy danh sách khách hàng hoàn tất CỦA TẤT CẢ CÁC OWNER. */
    public List<FbCustomerStaging> getCompletedCustomers() { 
        return crudService.getAll().stream()
                .filter(c -> c.getStatus() == CustomerStatus.COMPLETED)
                .toList();
    }

    /** Lấy danh sách khách hàng thất bại CỦA TẤT CẢ CÁC OWNER. */
    public List<FbCustomerStaging> getFailedCustomers() {
        return crudService.getAll().stream()
                .filter(c -> c.getStatus() == CustomerStatus.FAILED)
                .toList();
    }
}