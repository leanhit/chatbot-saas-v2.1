package com.chatbot.modules.address.service;

import com.chatbot.modules.address.dto.*;
import com.chatbot.modules.address.model.OwnerType;

import java.util.List;

public interface AddressService {
    
    AddressResponseDTO createAddress(Long tenantId, AddressRequestDTO dto);
    
    List<AddressResponseDTO> getAddressesByOwner(Long tenantId, OwnerType type, Long id);
    
    AddressDetailResponseDTO getAddressDetail(Long tenantId, Long id);
    
    AddressResponseDTO updateAddress(Long tenantId, Long id, AddressRequestDTO dto);
    
    void deleteAddress(Long tenantId, Long id);
    
    void createEmptyAddressForUser(Long userId);
}
