package com.chatbot.modules.facebook.user.service;

import com.chatbot.modules.facebook.connection.model.FacebookConnection;
import com.chatbot.modules.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.modules.facebook.user.dto.FacebookUserInfo;
import com.chatbot.modules.facebook.user.model.FacebookUser;
import com.chatbot.modules.facebook.user.repository.FacebookUserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacebookUserService {
    private static final String USER_FIELDS = "name,profile_pic";
    
    private final WebClient webClient;
    private final FacebookConnectionRepository connectionRepository;
    private final FacebookUserRepository facebookUserRepository;

    /**
     * Lấy thông tin người dùng Facebook thông qua PSID và pageId
     * @param psid PSID của người dùng
     * @param pageId ID của page mà người dùng đang tương tác
     * @return Thông tin người dùng hoặc null nếu không tìm thấy
     */
    public FacebookUserInfo getUserInfo(String psid, String pageId) {
        try {
            // 1. Lấy thông tin kết nối Facebook từ database
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                log.warn("Không tìm thấy tenant ID trong context");
                return null;
            }
            Optional<FacebookConnection> connectionOpt = connectionRepository.findByTenantIdAndPageId(tenantId, pageId);
            if (connectionOpt.isEmpty()) {
                log.warn("Không tìm thấy kết nối Facebook cho page: {}", pageId);
                return null;
            }
            FacebookConnection connection = connectionOpt.get();
            
            // 2. Gọi API Facebook Graph để lấy thông tin người dùng
            Map<String, Object> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/{psid}")
                    .queryParam("fields", USER_FIELDS)
                    .queryParam("access_token", connection.getPageAccessToken())
                    .build(psid))
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

            // 3. Ánh xạ dữ liệu trả về thành đối tượng FacebookUserInfo
            if (response != null && response.containsKey("name")) {
                return FacebookUserInfo.builder()
                    .psid(psid)
                    .pageId(pageId)
                    .name((String) response.get("name"))
                    .profilePic((String) response.get("profile_pic"))
                    .build();
            }
            
            log.warn("Không tìm thấy thông tin người dùng cho PSID: {} trên page: {}", psid, pageId);
            return null;
            
        } catch (WebClientResponseException e) {
            log.error("Lỗi khi gọi Facebook Graph API: {}", e.getResponseBodyAsString(), e);
            return null;
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin người dùng Facebook", e);
            return null;
        }
    }
    
    /**
     * Lấy thông tin người dùng và bổ sung vào tin nhắn nếu cần
     * @param psid PSID của người dùng
     * @param pageId ID của page
     * @return Thông tin người dùng hoặc null nếu không tìm thấy
     */
    public FacebookUserInfo enrichMessageWithUserInfo(String psid, String pageId) {
        // Có thể thêm logic cache ở đây nếu cần
        return getUserInfo(psid, pageId);
    }

    @Transactional(readOnly = true)
    public Page<FacebookUserInfo> searchUsers(String pageId, String keyword, Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return facebookUserRepository.findByPageIdAndTenantId(pageId, tenantId, pageable)
                .map(this::mapToUserInfo);
        }
        return facebookUserRepository.searchByPageIdAndTenantIdAndNameContaining(pageId, tenantId, keyword, pageable)
            .map(this::mapToUserInfo);
    }

    @Transactional
    public FacebookUserInfo updateOdooPartnerId(String psid, String pageId, Integer odooPartnerId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        
        FacebookUser user = facebookUserRepository.findByPsidAndPageIdAndTenantId(psid, pageId, tenantId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with PSID: " + psid));
        
        user.updateOdooPartnerId(odooPartnerId);
        return mapToUserInfo(facebookUserRepository.save(user));
    }

    @Transactional
    public FacebookUserInfo syncWithFacebook(String psid, String pageId) {
        FacebookUserInfo userInfo = getUserInfo(psid, pageId);
        if (userInfo == null) {
            throw new EntityNotFoundException("Cannot fetch user info from Facebook for PSID: " + psid);
        }

        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        
        FacebookUser user = facebookUserRepository.findByPsidAndPageIdAndTenantId(psid, pageId, tenantId)
            .orElseGet(() -> {
                FacebookUser newUser = new FacebookUser();
                newUser.setPsid(psid);
                newUser.setPage(connectionRepository.findByTenantIdAndPageId(tenantId, pageId)
                    .orElseThrow(() -> new EntityNotFoundException("Page not found: " + pageId)));
                return newUser;
            });

        user.updateFromFacebook(userInfo.getName(), userInfo.getProfilePic());
        return mapToUserInfo(facebookUserRepository.save(user));
    }

    @Transactional(readOnly = true)
    public Page<FacebookUserInfo> findUnmappedUsers(String pageId, Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        
        return facebookUserRepository.findByPageIdAndTenantIdAndOdooPartnerIdIsNull(pageId, tenantId, pageable)
            .map(this::mapToUserInfo);
    }

    @Transactional(readOnly = true)
    public FacebookUserInfo findByOdooPartnerId(Integer odooPartnerId, String pageId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        
        return facebookUserRepository.findByOdooPartnerIdAndPageIdAndTenantId(odooPartnerId, pageId, tenantId)
            .map(this::mapToUserInfo)
            .orElseThrow(() -> new EntityNotFoundException("User not found with Odoo Partner ID: " + odooPartnerId));
    }

    @Transactional
    public void updateSubscription(String psid, String pageId, boolean isSubscribed) {
        // Note: The subscription field was removed, so this method is kept for backward compatibility
        // but doesn't do anything with the subscription status
        log.info("Subscription update requested for user {} on page {}. New status: {}", psid, pageId, isSubscribed);
    }

    private FacebookUserInfo mapToUserInfo(FacebookUser user) {
        return FacebookUserInfo.builder()
            .psid(user.getPsid())
            .pageId(user.getPage().getPageId())
            .name(user.getName())
            .profilePic(user.getProfilePic())
            .odooPartnerId(user.getOdooPartnerId())
            .lastInteraction(user.getLastInteraction())
            .build();
    }
}
