package com.chatbot.core.presence.service;

import com.chatbot.core.tenant.membership.model.TenantMember;
import com.chatbot.core.tenant.membership.model.TenantRole;
import com.chatbot.core.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PresenceService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final TenantMemberRepository tenantMemberRepository;
    private final UserRepository userRepository;

    private static final String ONLINE_MEMBERS_KEY_PREFIX = "tenant:";
    private static final String ONLINE_MEMBERS_KEY_SUFFIX = ":online_members";
    private static final long ONLINE_TTL_HOURS = 24;

    private String getOnlineMembersKey(Long tenantId) {
        return ONLINE_MEMBERS_KEY_PREFIX + tenantId + ONLINE_MEMBERS_KEY_SUFFIX;
    }

    // Redis Hash key: tenant:{tenantId}:user_session → field: userId → JSON
    private String getMemberDataKey(Long tenantId) {
        return ONLINE_MEMBERS_KEY_PREFIX + tenantId + ":member_data";
    }

    /**
     * Thêm thành viên vào danh sách online trong Redis
     */
    public void addOnlineMember(Long tenantId, Long userId, String email, String fullName) {
        try {
            // 1. Add userId to the online Set
            redisTemplate.opsForSet().add(getOnlineMembersKey(tenantId), userId.toString());
            redisTemplate.expire(getOnlineMembersKey(tenantId), ONLINE_TTL_HOURS, TimeUnit.HOURS);

            // 2. Store full member data in a Hash (O(1) lookup and deletion)
            Map<String, Object> memberData = Map.of(
                "userId", userId,
                "email", email,
                "fullName", fullName,
                "timestamp", System.currentTimeMillis()
            );
            String memberJson = objectMapper.writeValueAsString(memberData);
            redisTemplate.opsForHash().put(getMemberDataKey(tenantId), userId.toString(), memberJson);
            redisTemplate.expire(getMemberDataKey(tenantId), ONLINE_TTL_HOURS, TimeUnit.HOURS);

            log.info("✅ [Presence] User {} (ID: {}) added to online list for tenant {}", email, userId, tenantId);
        } catch (Exception e) {
            log.error("❌ [Presence] Error adding online member: {}", e.getMessage(), e);
        }
    }

    /**
     * Xóa thành viên khỏi danh sách online trong Redis
     */
    public void removeOnlineMember(Long tenantId, Long userId) {
        try {
            // O(1) removal from Set and Hash
            redisTemplate.opsForSet().remove(getOnlineMembersKey(tenantId), userId.toString());
            redisTemplate.opsForHash().delete(getMemberDataKey(tenantId), userId.toString());
            log.info("❌ [Presence] User ID {} removed from online list for tenant {}", userId, tenantId);
        } catch (Exception e) {
            log.error("❌ [Presence] Error removing online member: {}", e.getMessage(), e);
        }
    }

    /**
     * Lấy danh sách thành viên online với đầy đủ thông tin (email, fullName, avatar, role)
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getOnlineMembers(Long tenantId) {
        try {
            // 1. Get online userId set
            Set<String> onlineUserIds = redisTemplate.opsForSet().members(getOnlineMembersKey(tenantId));
            if (onlineUserIds == null || onlineUserIds.isEmpty()) {
                return Collections.emptyList();
            }

            // 2. Bulk-fetch member data from Hash (single Redis call)
            List<Object> memberJsons = redisTemplate.opsForHash()
                .multiGet(getMemberDataKey(tenantId), Collections.unmodifiableCollection(onlineUserIds));

            List<Map<String, Object>> onlineMembers = new ArrayList<>();
            for (Object obj : memberJsons) {
                if (obj == null) continue;
                try {
                    Map<String, Object> memberData = objectMapper.readValue(obj.toString(), Map.class);
                    Long userId = ((Number) memberData.get("userId")).longValue();

                    Optional<User> userOpt = userRepository.findById(userId);
                    if (userOpt.isEmpty()) continue;

                    User user = userOpt.get();
                    Optional<TenantMember> memberOpt = tenantMemberRepository
                        .findByTenantIdAndUserIdAndStatus(tenantId, userId,
                            com.chatbot.core.tenant.membership.model.MembershipStatus.ACTIVE);

                    String role = memberOpt.map(m -> m.getRole() != null ? m.getRole().name() : "MEMBER")
                                          .orElse("MEMBER");

                    Map<String, Object> enriched = new HashMap<>();
                    enriched.put("userId", userId);
                    enriched.put("email", user.getEmail());
                    enriched.put("fullName", memberData.get("fullName"));
                    enriched.put("avatar", user.getProfile() != null ? user.getProfile().getAvatar() : null);
                    enriched.put("role", role);
                    enriched.put("timestamp", memberData.get("timestamp"));
                    onlineMembers.add(enriched);

                } catch (Exception e) {
                    log.warn("⚠️ [Presence] Error parsing online member: {}", e.getMessage());
                }
            }

            log.info("📋 [Presence] Found {} online members for tenant {}", onlineMembers.size(), tenantId);
            return onlineMembers;

        } catch (Exception e) {
            log.error("❌ [Presence] Error getting online members: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Broadcast thông báo member online tới tất cả WebSocket connections
     * (Được gọi từ PresenceWebSocketHandler)
     */
    public String createMemberOnlineMessage(Long tenantId, Long userId, String email, String fullName) {
        try {
            Map<String, Object> message = Map.of(
                "type", "MEMBER_ONLINE",
                "tenantId", tenantId,
                "data", Map.of(
                    "userId", userId,
                    "email", email,
                    "fullName", fullName,
                    "timestamp", System.currentTimeMillis()
                )
            );
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("❌ [Presence] Error creating online message: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Broadcast thông báo member offline tới tất cả WebSocket connections
     * (Được gọi từ PresenceWebSocketHandler)
     */
    public String createMemberOfflineMessage(Long tenantId, Long userId) {
        try {
            Map<String, Object> message = Map.of(
                "type", "MEMBER_OFFLINE",
                "tenantId", tenantId,
                "data", Map.of(
                    "userId", userId,
                    "timestamp", System.currentTimeMillis()
                )
            );
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("❌ [Presence] Error creating offline message: {}", e.getMessage());
            return null;
        }
    }
}
