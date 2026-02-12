package com.chatbot.modules.address.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatbot.modules.address.dto.*;
import com.chatbot.modules.address.mapper.AddressMapper;
import com.chatbot.modules.address.model.Address;
import com.chatbot.modules.address.model.OwnerType;
import com.chatbot.modules.address.repository.AddressRepository;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;
    private final TenantRepository tenantRepository;

    @Transactional
    public AddressResponseDTO createAddress(Long tenantId, AddressRequestDTO dto) {
        // Kiểm tra xem owner đã có địa chỉ chưa
        Optional<Address> existingAddress = addressRepository.findByTenantIdAndOwnerTypeAndOwnerId(tenantId, dto.getOwnerType(), dto.getOwnerId());
        if (existingAddress.isPresent()) {
            throw new RuntimeException("Owner already has an address. Use update instead.");
        }
        
        Address address = addressMapper.toEntity(tenantId, dto);
        return addressMapper.toResponseDTO(addressRepository.save(address));
    }

    // Lấy địa chỉ duy nhất của owner, nếu chưa có thì tạo mới
    @Transactional
    public AddressDetailResponseDTO getOrCreateSingleAddress(Long tenantId, OwnerType type, Long ownerId) {
        Optional<Address> existingAddress = addressRepository.findByTenantIdAndOwnerTypeAndOwnerId(tenantId, type, ownerId);
        
        if (existingAddress.isPresent()) {
            return addressMapper.toDetailDTO(existingAddress.get());
        } else {
            // Tạo địa chỉ trống mặc định
            AddressRequestDTO emptyAddress = new AddressRequestDTO();
            emptyAddress.setOwnerType(type);
            emptyAddress.setOwnerId(ownerId);
            emptyAddress.setHouseNumber("");
            emptyAddress.setStreet("");
            emptyAddress.setWard("");
            emptyAddress.setDistrict("");
            emptyAddress.setProvince("");
            emptyAddress.setCountry("Vietnam");
            
            Address newAddress = addressMapper.toEntity(tenantId, emptyAddress);
            return addressMapper.toDetailDTO(addressRepository.save(newAddress));
        }
    }

    // Lấy địa chỉ duy nhất của user (không cần tenant)
    public AddressDetailResponseDTO getUserAddress(OwnerType type, Long ownerId) {
        Optional<Address> address = addressRepository.findByOwnerTypeAndOwnerId(type, ownerId);
        return address.map(addressMapper::toDetailDTO)
                .orElseThrow(() -> new RuntimeException("Address not found for user"));
    }

    // Lấy hoặc tạo địa chỉ duy nhất cho user (không cần tenant)
    @Transactional
    public AddressDetailResponseDTO getOrCreateUserAddress(OwnerType type, Long ownerId) {
        Optional<Address> existingAddress = addressRepository.findByOwnerTypeAndOwnerId(type, ownerId);
        
        if (existingAddress.isPresent()) {
            return addressMapper.toDetailDTO(existingAddress.get());
        } else {
            // Tạo địa chỉ trống mặc định
            AddressRequestDTO emptyAddress = new AddressRequestDTO();
            emptyAddress.setOwnerType(type);
            emptyAddress.setOwnerId(ownerId);
            emptyAddress.setHouseNumber("");
            emptyAddress.setStreet("");
            emptyAddress.setWard("");
            emptyAddress.setDistrict("");
            emptyAddress.setProvince("");
            emptyAddress.setCountry("Vietnam");
            
            Address newAddress = addressMapper.toEntity(null, emptyAddress); // tenantId = null
            return addressMapper.toDetailDTO(addressRepository.save(newAddress));
        }
    }

    // Lấy địa chỉ duy nhất của owner (nếu có)
    public AddressDetailResponseDTO getSingleAddressByOwner(Long tenantId, OwnerType type, Long ownerId) {
        Optional<Address> address = addressRepository.findByTenantIdAndOwnerTypeAndOwnerId(tenantId, type, ownerId);
        return address.map(addressMapper::toDetailDTO)
                .orElseThrow(() -> new RuntimeException("Address not found for owner"));
    }

    public AddressDetailResponseDTO getAddressDetail(Long tenantId, Long id) {
        Address address = addressRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        return addressMapper.toDetailDTO(address);
    }

    // Helper method to update address fields from DTO
    private void updateAddressFields(Address address, AddressUpdateRequest dto) {
        if (dto.getStreet() != null) {
            address.setStreet(dto.getStreet());
        }
        if (dto.getHouseNumber() != null) {
            address.setHouseNumber(dto.getHouseNumber());
        }
        if (dto.getWard() != null) {
            address.setWard(dto.getWard());
        }
        if (dto.getDistrict() != null) {
            address.setDistrict(dto.getDistrict());
        }
        if (dto.getProvince() != null) {
            address.setProvince(dto.getProvince());
        }
        if (dto.getCountry() != null) {
            address.setCountry(dto.getCountry());
        } else {
            // Nếu country null, giữ nguyên giá trị cũ hoặc set default
            if (address.getCountry() == null) {
                address.setCountry("Vietnam");
            }
        }
    }

    @Transactional
    public AddressDetailResponseDTO updateUserAddress(OwnerType type, Long ownerId, AddressUpdateRequest dto) {
        Address address = addressRepository.findByOwnerTypeAndOwnerId(type, ownerId)
                .orElseThrow(() -> new RuntimeException("Address not found for user"));

        updateAddressFields(address, dto);
        return addressMapper.toDetailDTO(addressRepository.save(address));
    }

    @Transactional
    public AddressDetailResponseDTO updateTenantAddress(String tenantKey, AddressUpdateRequest dto) {
        // Find tenant by tenantKey to get tenantId
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new RuntimeException("Tenant not found with key: " + tenantKey));
        
        Address address = addressRepository.findByTenantIdAndOwnerTypeAndOwnerId(
            tenant.getId(), OwnerType.TENANT, tenant.getId())
                .orElseThrow(() -> new RuntimeException("Tenant address not found"));

        updateAddressFields(address, dto);
        return addressMapper.toDetailDTO(addressRepository.save(address));
    }

    @Transactional
    public AddressResponseDTO updateAddressField(Long tenantId, Long id, String field, String value) {
        Address address = addressRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        switch (field) {
            case "houseNumber":
                address.setHouseNumber(value);
                break;
            case "street":
                address.setStreet(value);
                break;
            case "ward":
                address.setWard(value);
                break;
            case "district":
                address.setDistrict(value);
                break;
            case "province":
                address.setProvince(value);
                break;
            case "country":
                address.setCountry(value != null ? value : "Vietnam");
                break;
            default:
                throw new RuntimeException("Invalid field: " + field);
        }

        return addressMapper.toResponseDTO(addressRepository.save(address));
    }

    @Transactional
    public AddressDetailResponseDTO updateAddressFields(Long tenantId, Long id, AddressUpdateRequest dto) {
        Address address = addressRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        updateAddressFields(address, dto);
        return addressMapper.toDetailDTO(addressRepository.save(address));
    }

    // Helper method to update address fields from AddressRequestDTO
    private void updateAddressFields(Address address, AddressRequestDTO dto) {
        if (dto.getHouseNumber() != null) {
            address.setHouseNumber(dto.getHouseNumber());
        }
        if (dto.getStreet() != null) {
            address.setStreet(dto.getStreet());
        }
        if (dto.getWard() != null) {
            address.setWard(dto.getWard());
        }
        if (dto.getDistrict() != null) {
            address.setDistrict(dto.getDistrict());
        }
        if (dto.getProvince() != null) {
            address.setProvince(dto.getProvince());
        }
        if (dto.getCountry() != null) {
            address.setCountry(dto.getCountry());
        } else {
            // Nếu country null, giữ nguyên giá trị cũ hoặc set default
            if (address.getCountry() == null) {
                address.setCountry("Vietnam");
            }
        }
    }

    @Transactional
    public AddressResponseDTO updateAddress(Long tenantId, Long id, AddressRequestDTO dto) {
        Address address = addressRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        updateAddressFields(address, dto);
        return addressMapper.toResponseDTO(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(Long tenantId, Long id) {
        Address address = addressRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        addressRepository.delete(address);
    }

    @Transactional
    public void createEmptyAddressForUser(Long userId) {
        AddressRequestDTO emptyAddress = new AddressRequestDTO();
        emptyAddress.setOwnerType(OwnerType.USER);
        emptyAddress.setOwnerId(userId);
        emptyAddress.setHouseNumber("");
        emptyAddress.setStreet("");
        emptyAddress.setWard("");
        emptyAddress.setDistrict("");
        emptyAddress.setProvince("");
        emptyAddress.setCountry("Vietnam");

        // Tạo address không cần tenantId cho user
        try {
            Address address = addressMapper.toEntity(null, emptyAddress); // tenantId = null cho user address
            addressRepository.save(address);
            log.info("Đã tạo địa chỉ trống mặc định cho user ID: {}", userId);
        } catch (Exception e) {
            log.error("Lỗi khi tạo địa chỉ mặc định cho user {}: {}", userId, e.getMessage());
        }
    }

    }