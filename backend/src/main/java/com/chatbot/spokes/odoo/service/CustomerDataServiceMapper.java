package com.chatbot.spokes.odoo.service;

import com.chatbot.spokes.odoo.dto.CustomerDataDTO;
import com.chatbot.spokes.odoo.model.FbCustomerStaging;
import com.chatbot.spokes.odoo.model.FbCapturedPhone;
import com.chatbot.spokes.facebook.user.model.FacebookUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Mapper service để gộp data từ 3 bảng vào CustomerDataDTO
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerDataServiceMapper {

    private final ObjectMapper objectMapper;

    /**
     * Map từ FbCustomerStaging và FacebookUser sang CustomerDataDTO
     */
    public CustomerDataDTO mapToCustomerData(
            FbCustomerStaging staging, 
            FacebookUser facebookUser,
            List<FbCapturedPhone> capturedPhones) {

        if (staging == null) {
            return null;
        }

        // Parse phones từ JSON
        Set<String> phones = new HashSet<>();
        try {
            if (staging.getPhones() != null && !staging.getPhones().trim().isEmpty()) {
                phones = objectMapper.readValue(staging.getPhones(), new TypeReference<Set<String>>() {});
            }
        } catch (Exception e) {
            log.warn("Error parsing phones JSON for PSID {}: {}", staging.getPsid(), e.getMessage());
        }

        // Map captured phones
        List<CustomerDataDTO.CapturedPhoneInfo> phoneInfoList = new ArrayList<>();
        if (capturedPhones != null) {
            phoneInfoList = capturedPhones.stream()
                    .map(phone -> CustomerDataDTO.CapturedPhoneInfo.builder()
                            .phoneNumber(phone.getPhoneNumber())
                            .capturedAt(phone.getCreatedAt())
                            .ownerId(phone.getOwnerId())
                            .build())
                    .collect(Collectors.toList());
        }

        return CustomerDataDTO.builder()
                // From staging
                .psid(staging.getPsid())
                .ownerId(staging.getOwnerId())
                .pageId(staging.getPageId())
                .phones(phones)
                .dataJson(staging.getDataJson())
                .status(staging.getStatus())
                .odooId(staging.getOdooId())
                .createdAt(staging.getCreatedAt())
                .updatedAt(staging.getUpdatedAt())

                // From Facebook user
                .facebookName(facebookUser != null ? facebookUser.getName() : null)
                .facebookAvatar(facebookUser != null ? facebookUser.getProfilePic() : null)
                .odooPartnerId(facebookUser != null ? facebookUser.getOdooPartnerId() : null)
                .lastInteraction(facebookUser != null ? facebookUser.getLastInteraction() : null)

                // From captured phones
                .capturedPhones(phoneInfoList)
                .build();
    }

    /**
     * Map danh sách FbCustomerStaging sang CustomerDataDTO
     */
    public List<CustomerDataDTO> mapToCustomerDataList(
            List<FbCustomerStaging> stagingList,
            Map<String, FacebookUser> facebookUserMap,
            Map<String, List<FbCapturedPhone>> capturedPhoneMap) {

        if (stagingList == null) {
            return new ArrayList<>();
        }

        return stagingList.stream()
                .map(staging -> {
                    FacebookUser facebookUser = facebookUserMap != null ? 
                            facebookUserMap.get(staging.getPsid()) : null;
                    List<FbCapturedPhone> capturedPhones = capturedPhoneMap != null ? 
                            capturedPhoneMap.get(staging.getOwnerId()) : null;
                    return mapToCustomerData(staging, facebookUser, capturedPhones);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
