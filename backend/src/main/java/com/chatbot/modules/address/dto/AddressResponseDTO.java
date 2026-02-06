package com.chatbot.modules.address.dto;

import lombok.Data;

@Data
public class AddressResponseDTO {
    private Long id;
    private String fullAddress;
    private boolean isDefault;
}
