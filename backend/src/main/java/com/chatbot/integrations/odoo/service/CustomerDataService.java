package com.chatbot.integrations.odoo.service;

import com.chatbot.modules.facebook.connection.model.FacebookConnection;
import com.chatbot.modules.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.modules.tenant.infra.TenantContext;
import com.chatbot.integrations.odoo.model.CustomerStatus;
import com.chatbot.integrations.odoo.model.FbCapturedPhone;
import com.chatbot.integrations.odoo.model.FbCustomerStaging;
import com.chatbot.integrations.odoo.client.OdooApiClient;
import com.chatbot.integrations.odoo.model.CustomerInfo;
import com.chatbot.integrations.odoo.service.FbCapturedPhoneService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CustomerDataService {

    // Bộ nhớ tạm cho từng người (PSID)
    private final Map<String, Map<String, String>> tempStorage = new ConcurrentHashMap<>();

    private final FacebookConnectionRepository connectionRepository;
    private final FbCustomerStagingCrudService crudService;
    private final OdooApiClient odooClient;
    private final CustomerInfoExtractor infoExtractor;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FbCapturedPhoneService phoneService;

    public CustomerDataService(
            FbCustomerStagingCrudService crudService,
            OdooApiClient odooClient,
            FacebookConnectionRepository connectionRepository,
            CustomerInfoExtractor infoExtractor,
            FbCapturedPhoneService phoneService
    ) {
        this.crudService = crudService;
        this.odooClient = odooClient;
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

            // B2. Trích xuất phone
            log.debug("🔍 [EXTRACT] Bắt đầu trích xuất thông tin từ text='{}'", text);
            CustomerInfo info = infoExtractor.extractInfo(text);
            String newPhone = info.getPhone();

            log.info("📞 [EXTRACT RESULT] PSID={} | phone={}", senderId, newPhone);

            // B3. Chuẩn bị và Kiểm tra
            if (newPhone == null) {
                log.debug("ℹ️ [NO PHONE] Không trích xuất được số điện thoại hợp lệ từ text='{}'", text);
                return false;
            }
            
            // 💡 SỬA ĐỔI CỐT LÕI: Ghi vào fb_captured_phone NHƯNG KHÔNG DỪNG NẾU TRÙNG LẶP.
            // Mục đích: Đảm bảo SĐT ít nhất 1 lần được ghi nhận vào bảng CapturedPhone.
            boolean isNewPhoneForOwner = phoneService.saveNewPhoneNumber(finalOwnerId, newPhone);
            
            if (isNewPhoneForOwner) {
                log.info("✅ [FB_PHONE_NEW] SĐT '{}' đã được ghi nhận mới vào fb_captured_phone. TIẾP TỤC xử lý Staging.", newPhone);
            } else {
                // SĐT cũ, không cần lưu lại vào CapturedPhone, NHƯNG VẪN CẦN XỬ LÝ STAGING (B6)
                log.warn("ℹ️ [PHONE EXISTS] SĐT '{}' đã tồn tại trong fb_captured_phone. Vẫn TIẾP TỤC xử lý Staging.", newPhone);
                // ❌ ĐÃ XÓA: return false;
            }
            
            // --- LOGIC STAGING TIẾP TỤC ---
            
            // 🔹 B4. Lưu thông tin tạm thời
            Map<String, String> extracted = new HashMap<>();
            extracted.put("phone", newPhone);
            Map<String, String> current = tempStorage.computeIfAbsent(senderId, k -> new HashMap<>());
            current.put("phone", newPhone); 

            // 🔹 B5. Lấy hoặc tạo mới record
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

            // 🔹 B6. Xử lý danh sách phones (Luôn thêm SĐT duy nhất vào Set)
            Set<String> phonesSet;
            try {
                String existingPhonesJson = staging.getPhones() != null ? staging.getPhones() : "[]";
                phonesSet = objectMapper.readValue(existingPhonesJson, new TypeReference<Set<String>>() {});
            } catch (Exception e) {
                log.error("💥 [JSON ERROR] Parse phones JSON lỗi cho PSID={} | msg={}", senderId, e.getMessage());
                phonesSet = new HashSet<>();
            }
            
            // Logic Set đảm bảo chỉ lưu SĐT không giống nhau (duy nhất) vào Staging.phones
            boolean phoneAddedToStaging = phonesSet.add(newPhone); 
            if (phoneAddedToStaging) {
                log.info("📲 [PHONES UPDATED] Thêm SĐT '{}' vào Staging.phones cho PSID={}", newPhone, senderId);
            }

            try {// 💡 Thêm log để kiểm tra chuỗi JSON trước khi gán
                String newPhonesJson = objectMapper.writeValueAsString(phonesSet);
                log.info("💾 [DEBUG JSON] Chuỗi JSON mới sẵn sàng lưu: {}", newPhonesJson);
                staging.setPhones(newPhonesJson);
            } catch (Exception e) {
                log.error("💥 [JSON ERROR] Serialize phonesSet lỗi cho PSID={} | msg={}", senderId, e.getMessage());
            }

            // 🔹 B8-B11. Ghi dataJson, Kiểm tra Complete, Upsert và Sync Odoo (Giữ nguyên)
            staging.setDataJson(toJson(current));

            boolean isComplete = isDataComplete(current);
            CustomerStatus oldStatus = staging.getStatus();

            if (isComplete) {
                staging.setStatus(CustomerStatus.COMPLETED);
            } else {
                staging.setStatus(CustomerStatus.PENDING);
            }

            staging.setUpdatedAt(LocalDateTime.now());
            crudService.upsert(staging);
            log.info("💾 [OK] Đã ghi CSDL thành công cho PSID={} | status={}", senderId, staging.getStatus());

            if (isComplete && oldStatus != CustomerStatus.COMPLETED) {
                log.info("📢 [ASYNC SYNC] Kích hoạt đồng bộ Odoo ngay cho PSID={}", senderId);
                syncCustomerToOdoo(staging);
            }
            
            return true; 

        } catch (Exception e) {
            // ... (Logic xử lý lỗi FAILED giữ nguyên) ...
            log.error("❌ [EXCEPTION] Lỗi khi xử lý PSID={} | msg={}", senderId, e.getMessage(), e);

            try {
                final String finalOwnerId = currentOwnerId != null ? currentOwnerId : "UNKNOWN_OWNER";
                FbCustomerStaging failedRecord = crudService.getByPsid(senderId, finalOwnerId)
                        // ... (Khởi tạo/cập nhật Failed record) ...
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
    
    // ⚠️ HÀM processNameByContext ĐÃ BỊ XÓA/KHÔNG SỬ DỤNG
    // Nếu bạn muốn giữ lại class này, bạn cần xóa hàm processNameByContext khỏi file.
    
    /** Kiểm tra chỉ cần phone là đủ */
    private boolean isDataComplete(Map<String, String> data) {
        return data.containsKey("phone");
    }

    /** Chuyển map → JSON (SỬ DỤNG ObjectMapper AN TOÀN) */
    private String toJson(Map<String, String> map) {
        try {
            return objectMapper.writeValueAsString(map); // ✅ SỬ DỤNG OBJECTMAPPER
        } catch (Exception e) {
            log.error("💥 [JSON SERIALIZE ERROR] Không thể chuyển Map thành JSON: {}", e.getMessage());
            // Trả về JSON trống hoặc ném ra RuntimeException tùy theo yêu cầu xử lý lỗi của ứng dụng
            return "{}"; 
        }
    }

    /** Debug: Lấy dữ liệu tạm */
    public Map<String, String> getTempData(String senderId) {
        return tempStorage.getOrDefault(senderId, Map.of());
    }

    /** * 🛠️ [MULTI-TENANT SAFE] Lấy danh sách khách hàng hoàn tất CỦA MỘT OWNER CỤ THỂ. */
    public List<FbCustomerStaging> getCompletedCustomersByOwner(String ownerId) {
        return crudService.getAllByOwnerId(ownerId).stream()
                .filter(c -> c.getStatus() == CustomerStatus.COMPLETED)
                .toList();
    }

    /**
     * 🚀 [SCHEDULER USE] Lấy danh sách khách hàng hoàn tất CỦA TẤT CẢ CÁC OWNER.
     * KHÔNG CẦN THIẾT DÙNG NỮA SAU KHI GỌI ASYNC NGAY LẬP TỨC
     */
    public List<FbCustomerStaging> getCompletedCustomers() { 
        return crudService.getAll().stream()
                .filter(c -> c.getStatus() == CustomerStatus.COMPLETED)
                .toList();
    }

    /**
     * 🚀 [SCHEDULER USE] Lấy danh sách khách hàng thất bại CỦA TẤT CẢ CÁC OWNER.
     */
    public List<FbCustomerStaging> getFailedCustomers() {
        return crudService.getAll().stream()
                .filter(c -> c.getStatus() == CustomerStatus.FAILED)
                .toList();
    }

    /** Đồng bộ từng khách hàng sang Odoo (chạy async) */
    @Async
    public void syncCustomerToOdoo(FbCustomerStaging customer) {
        log.info("🚀 Đồng bộ PSID={} sang Odoo CRM Lead...", customer.getPsid());
        try {
            Map<String, String> data = objectMapper.readValue(customer.getDataJson(), new TypeReference<>() {});
            Map<String, Object> payload = new HashMap<>();
            
            // -------------------------------------------------------------------------
            // 💡 LOGIC: Dùng trường `phones` để tạo Description
            // -------------------------------------------------------------------------
            Set<String> phonesSet = new HashSet<>();
            try {
                String existingPhonesJson = customer.getPhones() != null ? customer.getPhones() : "[]";
                phonesSet = objectMapper.readValue(existingPhonesJson, new TypeReference<Set<String>>() {});
            } catch (Exception e) {
                log.error("Lỗi parse trường `phones` (JSON) khi đồng bộ PSID {}: {}. Sử dụng SĐT chính.", customer.getPsid(), e.getMessage());
                if(data.containsKey("phone")) phonesSet.add(data.get("phone"));
            }
            
            String phoneListDesc = phonesSet.isEmpty() 
                                   ? "Không có SĐT tích lũy." 
                                   : "Các SĐT đã bắt: " + String.join(", ", phonesSet);
            // -------------------------------------------------------------------------

            // Lấy SĐT cuối cùng được bắt (SĐT quan trọng nhất)
            String mainPhone = data.get("phone");
            
            // 💡 SỬA: Tạo tên Lead bằng cách kết hợp SĐT chính + Ngày/Giờ
            String leadName = String.format(
                "Lead %s (%s) - %s", 
                mainPhone,
                customer.getPsid(),
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            );

            // Mapping trường cho CRM Lead
            payload.put("name", leadName); // 💡 Dùng tên mới được gán
            payload.put("phone", mainPhone);
            
            // Cập nhật Description với danh sách SĐT tích lũy
            payload.put("description", "Tự động tạo từ Facebook PSID: " + customer.getPsid() + "\n" + phoneListDesc);
            
            payload.put("x_facebook_psid", customer.getPsid());
            payload.put("type", "lead"); 

            // Gọi API Odoo để tạo/cập nhật lead
            Integer odooId = odooClient.createOrUpdateLead(customer.getPsid(), payload);

            // Cập nhật DB
            customer.setOdooId(odooId);
            customer.setStatus(CustomerStatus.PENDING); 
            crudService.upsert(customer);

            log.info("✅ Đồng bộ Lead thành công PSID={} -> Odoo ID={}. Chuyển trạng thái sang PENDING.", customer.getPsid(), odooId);
        } catch (Exception e) {
            customer.setStatus(CustomerStatus.FAILED);
            crudService.upsert(customer);
            log.error("❌ Lỗi đồng bộ Odoo Lead cho PSID {}: {}", customer.getPsid(), e.getMessage(), e);
        }
    }
}