package com.chatbot.modules.address.dto;

import com.chatbot.modules.address.model.OwnerType;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddressRequestDTO {

    @NotNull
    private OwnerType ownerType;

    @NotNull
    private Long ownerId;

    private String street;

    private String houseNumber;

    private String ward;

    private String district;
    private String province;
    private String country;
}
