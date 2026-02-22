package com.chatbot.shared.address.mapper;

import org.springframework.stereotype.Component;

import com.chatbot.shared.address.dto.*;
import com.chatbot.shared.address.model.Address;

@Component
public class AddressMapper {

    public Address toEntity(Long tenantId, AddressRequestDTO dto) {
        return Address.builder()
                .tenantId(tenantId)
                .ownerType(dto.getOwnerType())
                .ownerId(dto.getOwnerId())
                .houseNumber(dto.getHouseNumber())
                .street(dto.getStreet())
                .ward(dto.getWard())
                .district(dto.getDistrict())
                .province(dto.getProvince())
                .country(dto.getCountry() != null ? dto.getCountry() : "Vietnam")
                .build();
    }

    public AddressResponseDTO toResponseDTO(Address entity) {
        AddressResponseDTO dto = new AddressResponseDTO();
        dto.setId(entity.getId());
        dto.setFullAddress(buildFullAddress(entity));
        return dto;
    }

    public AddressDetailResponseDTO toDetailDTO(Address entity) {
        if (entity == null) return null;
        AddressDetailResponseDTO dto = new AddressDetailResponseDTO();
        dto.setId(entity.getId());
        dto.setOwnerType(entity.getOwnerType());
        dto.setOwnerId(entity.getOwnerId());
        dto.setHouseNumber(entity.getHouseNumber());
        dto.setStreet(entity.getStreet());
        dto.setWard(entity.getWard());
        dto.setDistrict(entity.getDistrict());
        dto.setProvince(entity.getProvince());
        dto.setCountry(entity.getCountry());
        dto.setFullAddress(buildFullAddress(entity));
        return dto;
    }

    private String buildFullAddress(Address e) {
        // Sử dụng StringJoiner để tự động xử lý dấu phẩy giữa các thành phần
        java.util.StringJoiner joiner = new java.util.StringJoiner(", ");
        
        String houseAndStreet = (e.getHouseNumber() != null ? e.getHouseNumber() : "") + 
                                " " + 
                                (e.getStreet() != null ? e.getStreet() : "");
        
        if (!houseAndStreet.trim().isEmpty()) joiner.add(houseAndStreet.trim());
        if (e.getWard() != null && !e.getWard().isEmpty()) joiner.add(e.getWard());
        if (e.getDistrict() != null && !e.getDistrict().isEmpty()) joiner.add(e.getDistrict());
        if (e.getProvince() != null && !e.getProvince().isEmpty()) joiner.add(e.getProvince());
        
        return joiner.toString();
    }
    

}
