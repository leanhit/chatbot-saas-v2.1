package com.chatbot.spokes.facebook.adapter;

import com.chatbot.shared.messenger.ChannelMessengerService;
import com.chatbot.shared.messenger.ChannelUserInfo;
import com.chatbot.spokes.facebook.connection.exception.ConnectionNotFoundException;
import com.chatbot.spokes.facebook.connection.model.FacebookConnection;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.spokes.facebook.messenger.service.FacebookMessengerService;
import com.chatbot.spokes.facebook.user.dto.FacebookUserInfo;
import com.chatbot.spokes.facebook.user.service.FacebookUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacebookChannelAdapter implements ChannelMessengerService {

    private final FacebookConnectionRepository connectionRepository;
    private final FacebookMessengerService facebookMessengerService;
    private final FacebookUserService facebookUserService;

    @Override
    public boolean sendMessage(UUID connectionId, String recipientExternalId, String content) {
        if (connectionId == null || recipientExternalId == null || content == null) {
            log.warn("⚠️ [FacebookAdapter] Missing parameters for message dispatch: connectionId={}, recipient={}", connectionId, recipientExternalId);
            return false;
        }

        try {
            FacebookConnection connection = connectionRepository.findById(connectionId)
                    .orElseThrow(() -> new ConnectionNotFoundException(connectionId));

            String pageId = connection.getPageId();
            String pageAccessToken = connection.getPageAccessToken();

            log.info("🤖 [FacebookAdapter] Dispatching message via Page ID: {} to PSID: {}", pageId, recipientExternalId);
            facebookMessengerService.sendMessageToUser(pageId, recipientExternalId, content, pageAccessToken);
            log.info("📤 [FacebookAdapter] Successfully sent message to recipient: {}", recipientExternalId);
            return true;
        } catch (Exception e) {
            log.error("❌ [FacebookAdapter] Error sending message via Facebook connection {}: {}", connectionId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public long countActiveConnections(Long tenantId) {
        if (tenantId == null) return 0L;
        return connectionRepository.countByTenantIdAndIsActiveTrue(tenantId);
    }

    @Override
    public String getOwnerIdForConnection(UUID connectionId) {
        return connectionRepository.findById(connectionId)
                .map(FacebookConnection::getOwnerId)
                .orElse(null);
    }

    @Override
    public ChannelUserInfo getUserInfo(UUID connectionId, String externalUserId) {
        try {
            FacebookConnection connection = connectionRepository.findById(connectionId).orElse(null);
            if (connection == null) return null;

            FacebookUserInfo fbInfo = facebookUserService.getUserInfo(externalUserId, connection.getPageId());
            if (fbInfo == null) return null;

            Map<String, Object> attributes = new HashMap<>();
            if (fbInfo.getName() != null) {
                attributes.put("name", fbInfo.getName());
                String[] parts = fbInfo.getName().split(" ", 2);
                if (parts.length > 0) attributes.put("firstName", parts[0]);
                if (parts.length > 1) attributes.put("lastName", parts[1]);
            }
            if (fbInfo.getPsid() != null) attributes.put("psid", fbInfo.getPsid());
            if (fbInfo.getProfilePic() != null) attributes.put("hasProfilePic", true);
            if (fbInfo.getOdooPartnerId() != null) {
                attributes.put("odooPartnerId", fbInfo.getOdooPartnerId());
                attributes.put("isOdooCustomer", true);
            }

            return ChannelUserInfo.builder()
                    .name(fbInfo.getName())
                    .avatarUrl(fbInfo.getProfilePic())
                    .attributes(attributes)
                    .build();
        } catch (Exception e) {
            log.error("❌ [FacebookAdapter] Error getting user info for PSID {}: {}", externalUserId, e.getMessage());
            return null;
        }
    }
}
