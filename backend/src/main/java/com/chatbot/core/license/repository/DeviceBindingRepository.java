package com.chatbot.core.license.repository;

import com.chatbot.core.license.model.DeviceBinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceBindingRepository extends JpaRepository<DeviceBinding, Long> {

    List<DeviceBinding> findByUserId(Long userId);

    List<DeviceBinding> findByUserIdAndStatus(Long userId, String status);

    Optional<DeviceBinding> findByUserIdAndDeviceId(Long userId, String deviceId);

    long countByUserIdAndStatus(Long userId, String status);
}
