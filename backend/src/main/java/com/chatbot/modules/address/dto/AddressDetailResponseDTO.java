package com.chatbot.modules.address.dto;

import com.chatbot.modules.address.model.OwnerType;
import lombok.Data;

@Data
public class AddressDetailResponseDTO {
    private Long id;
    private Long tenantId;
    private OwnerType ownerType;
    private Long ownerId;

    private String houseNumber;
    private String street;
    private String ward;
    private String district;
    private String province;
    private String country;

    private String fullAddress;
    private boolean isDefault;
}
