package com.chatbot.modules.address.dto;

import com.chatbot.modules.address.model.OwnerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDetailResponseDTO {
    private Long id;
    private OwnerType ownerType;
    private Long ownerId;

    private String houseNumber;
    private String street;
    private String ward;
    private String district;
    private String province;
    private String country;

    private String fullAddress;
}
