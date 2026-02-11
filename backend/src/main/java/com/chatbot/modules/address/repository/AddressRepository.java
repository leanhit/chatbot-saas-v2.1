package com.chatbot.modules.address.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chatbot.modules.address.model.Address;
import com.chatbot.modules.address.model.OwnerType;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    // Lấy địa chỉ duy nhất của user (không cần tenant)
    Optional<Address> findByOwnerTypeAndOwnerId(OwnerType ownerType, Long ownerId);

    // Lấy địa chỉ duy nhất của owner (nếu có)
    Optional<Address> findByTenantIdAndOwnerTypeAndOwnerId(Long tenantId, OwnerType ownerType, Long ownerId);

    // Lấy địa chỉ theo ID và tenant
    Optional<Address> findByIdAndTenantId(Long id, Long tenantId);

    // Các phương thức bổ sung nếu cần
    List<Address> findByOwnerIdAndOwnerType(Long ownerId, OwnerType ownerType);

    // Lấy tất cả địa chỉ của danh sách tenant (Thường mỗi tenant có 1 địa chỉ chính)
    List<Address> findByTenantIdIn(List<Long> tenantIds);
}