package com.chatbot.spokes.facebook.autoconnect.service;

import com.chatbot.spokes.facebook.connection.model.FacebookConnection;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.spokes.facebook.connection.service.FacebookConnectionService;
import com.chatbot.spokes.facebook.autoconnect.dto.AutoConnectResponse;
import com.chatbot.spokes.facebook.autoconnect.dto.ConnectionError;
import com.chatbot.spokes.facebook.webhook.service.FacebookApiGraphService;
import com.chatbot.core.tenant.infra.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.chatbot.shared.utils.DateUtils;

@Slf4j
@Service
public class FbAutoConnectService {

    private final FacebookConnectionRepository connectionRepository;
    private final FacebookConnectionService facebookConnectionService;
    private final FbConnectionPersistenceService persistenceService; // tách transaction ra service riêng
    private final FacebookApiGraphService facebookApiGraphService;

    private static class ConnectionToProcess {
        FacebookConnection connection;
        boolean needsWebhookSubscription;
        boolean needsWebhookUnsubscription;

        public ConnectionToProcess(FacebookConnection connection, boolean needsSub, boolean needsUnsub) {
            this.connection = connection;
            this.needsWebhookSubscription = needsSub;
            this.needsWebhookUnsubscription = needsUnsub;
        }
    }

    public FbAutoConnectService(FacebookConnectionRepository connectionRepository,
                                FacebookConnectionService facebookConnectionService,
                                FbConnectionPersistenceService persistenceService,
                                FacebookApiGraphService facebookApiGraphService) {
        this.connectionRepository = connectionRepository;
        this.facebookConnectionService = facebookConnectionService;
        this.persistenceService = persistenceService;
        this.facebookApiGraphService = facebookApiGraphService;
    }

    /**
     * Tự động kết nối fanpage
     */
    public synchronized AutoConnectResponse autoConnect(String ownerId, String botId, String userAccessToken) {
        log.info("🔹 Bắt đầu auto connect fanpage cho ownerId={}", ownerId);

        List<String> connectedPages = new ArrayList<>();
        List<String> reactivatedPages = new ArrayList<>();
        List<String> inactivePages = new ArrayList<>();
        List<ConnectionError> errors = new ArrayList<>();
        List<ConnectionToProcess> webhookQueue = new ArrayList<>();

        String fbUserId = facebookApiGraphService.getUserIdFromToken(userAccessToken);

        // 1️⃣ Lấy danh sách page từ Facebook
        List<Map<String, Object>> fbPages = facebookApiGraphService.getUserPages(userAccessToken);
        if (fbPages == null || fbPages.isEmpty()) {
            log.warn("⚠️ Không có fanpage nào hoặc không lấy được danh sách page.");
            return new AutoConnectResponse(true, "Không có fanpage nào để kết nối.",
                    Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }

        Set<String> fbPageIds = fbPages.stream()
                .map(p -> (String) p.get("id"))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 2️⃣ Lấy connection hiện tại
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        List<FacebookConnection> existingConnections = connectionRepository.findByOwnerIdAndTenantId(ownerId, tenantId);
        Map<String, FacebookConnection> existingMap = existingConnections.stream()
                .collect(Collectors.toMap(FacebookConnection::getPageId, c -> c));

        List<FacebookConnection> toSave = new ArrayList<>();

        // 3️⃣ Xử lý từng page
        for (Map<String, Object> page : fbPages) {
            String pageId = (String) page.get("id");
            String pageName = (String) page.get("name");
            String pageToken = (String) page.get("access_token");

            if (pageId == null || pageToken == null) {
                log.warn("⚠️ Bỏ qua page {} vì thiếu access_token hoặc id.", pageName);
                errors.add(new ConnectionError(pageName, "Trang không có access_token hoặc id"));
                continue;
            }

            FacebookConnection conn = existingMap.get(pageId);
            boolean isNew = (conn == null);
            boolean wasInactive = false;

            if (isNew) {
                conn = new FacebookConnection();
                conn.setId(UUID.randomUUID());
                conn.setBotId(botId);
                conn.setOwnerId(ownerId);
                conn.setFbUserId(fbUserId);
                conn.setPageId(pageId);
                conn.setFanpageUrl("https://www.facebook.com/" + pageId);
                conn.setCreatedAt(DateUtils.nowUtcLocalDateTime());

                connectedPages.add(pageName);
                log.info("➡️ Tạo mới kết nối cho trang: {} ({})", pageName, pageId);
            } else {
                wasInactive = !conn.isActive();
                if (wasInactive) {
                    reactivatedPages.add(pageName);
                    log.info("♻️ Kích hoạt lại trang: {} ({})", pageName, pageId);
                } else {
                    connectedPages.add(pageName);
                    log.debug("🔄 Trang {} đã active, chỉ cập nhật token.", pageName);
                }
            }

            conn.setPageAccessToken(pageToken);
            conn.setBotName(pageName);
            conn.setEnabled(true);
            conn.setActive(true);
            conn.setUpdatedAt(DateUtils.nowUtcLocalDateTime());
            toSave.add(conn);

            webhookQueue.add(new ConnectionToProcess(conn, isNew || wasInactive, false));
        }

        // 4️⃣ Trang bị gỡ quyền
        List<FacebookConnection> currentFbUserConnections = existingConnections.stream()
                .filter(conn -> fbUserId.equals(conn.getFbUserId()))
                .collect(Collectors.toList());

        for (FacebookConnection conn : currentFbUserConnections) {
            if (!fbPageIds.contains(conn.getPageId()) && conn.isActive()) {
                conn.setActive(false);
                conn.setUpdatedAt(DateUtils.nowUtcLocalDateTime());
                toSave.add(conn);

                inactivePages.add(conn.getBotName());
                webhookQueue.add(new ConnectionToProcess(conn, false, true));
                log.info("❌ Đánh dấu trang {} ({}) là inactive.", conn.getBotName(), conn.getPageId());
            }
        }

        // 5️⃣ Lưu thay đổi
        if (!toSave.isEmpty()) {
            persistenceService.saveConnectionsTransactional(toSave, ownerId);
        }

        // 6️⃣ Xử lý webhook ngoài transaction
        processWebhooks(webhookQueue, errors);

        // ✅ Tạo message kết quả
        String message = String.format(
                "Xử lý xong: %d trang mới, %d trang kích hoạt lại, %d trang vô hiệu hóa.",
                connectedPages.size() - reactivatedPages.size(), reactivatedPages.size(), inactivePages.size()
        );

        if (!errors.isEmpty()) {
            message += " Có lỗi khi đăng ký/hủy webhook.";
        }

        log.info("✅ Auto connect hoàn tất cho ownerId={}", ownerId);
        return new AutoConnectResponse(errors.isEmpty(), message, connectedPages, reactivatedPages, inactivePages, errors);
    }

    /**
     * Đăng ký / hủy đăng ký webhook ngoài transaction DB
     */
    protected void processWebhooks(List<ConnectionToProcess> queue, List<ConnectionError> errors) {
        for (ConnectionToProcess task : queue) {
            FacebookConnection conn = task.connection;

            try {
                if (task.needsWebhookUnsubscription) {
                    facebookApiGraphService.unsubscribePageFromWebhook(conn.getPageId(), conn.getPageAccessToken());
                    log.debug("🪓 Hủy đăng ký webhook thành công cho {}", conn.getPageId());
                }
                if (task.needsWebhookSubscription) {
                    facebookApiGraphService.subscribePageToWebhook(conn.getPageId(), conn.getPageAccessToken());
                    log.debug("📡 Đăng ký webhook thành công cho {}", conn.getPageId());
                }
            } catch (Exception e) {
                log.error("❌ Lỗi webhook cho trang {}: {}", conn.getPageId(), e.getMessage());
                errors.add(new ConnectionError(conn.getBotName(), e.getMessage()));

                // Cập nhật trạng thái inactive nếu lỗi đăng ký webhook
                conn.setActive(false);
                conn.setUpdatedAt(DateUtils.nowUtcLocalDateTime());
                connectionRepository.save(conn);
                log.warn("⚠️ Đã đánh dấu trang {} là inactive do lỗi webhook.", conn.getPageId());
            }
        }
    }
}
