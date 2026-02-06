package com.chatbot.modules.address.dto;

import com.chatbot.modules.address.model.OwnerType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequestDTO {

    @NotNull
    private OwnerType ownerType;

    @NotNull
    private Long ownerId;

    @NotBlank
    private String street;

    private String houseNumber;

    @NotBlank
    private String ward;

    private String district;
    private String province;
    private String country;
    private boolean isDefault;
}
