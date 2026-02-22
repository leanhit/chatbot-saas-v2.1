package com.chatbot.shared.address.dto;

import lombok.Data;

@Data
public class AddressUpdateRequest {
    private String street;
    
    private String houseNumber;
    
    private String ward;
    
    private String district;
    private String province;
    private String country;
    
}
