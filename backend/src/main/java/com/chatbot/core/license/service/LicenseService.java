package com.chatbot.core.license.service;

import com.chatbot.core.license.dto.CreateLicenseRequest;
import com.chatbot.core.license.dto.DeviceBindingResponse;
import com.chatbot.core.license.dto.LicenseResponse;
import com.chatbot.core.license.dto.UpdateLicenseRequest;
import com.chatbot.core.license.exception.LicenseException;
import com.chatbot.core.license.exception.LicenseNotFoundException;
import com.chatbot.core.license.model.DeviceBinding;
import com.chatbot.core.license.model.License;
import com.chatbot.core.license.repository.DeviceBindingRepository;
import com.chatbot.core.license.repository.LicenseRepository;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LicenseService {

    public static final int MAX_DEVICES_PER_USER = 3;

    private final LicenseRepository licenseRepository;
    private final DeviceBindingRepository deviceBindingRepository;
    private final UserService userService;

    @Cacheable(value = "licenses", key = "#userId")
    public LicenseResponse getLicenseForUser(Long userId) {
        log.debug("Fetching license for user: {}", userId);
        
        Optional<License> licenseOpt = licenseRepository.findActiveLicenseByUserId(userId);
        
        if (licenseOpt.isEmpty()) {
            throw new LicenseNotFoundException("No active license found for user");
        }
        
        License license = licenseOpt.get();
        // Application-level join: fetch user by userId
        User user = userService.getUser(license.getUserId());
        
        // Check if license is still valid
        if (!license.isValid()) {
            if (license.isExpired()) {
                throw new LicenseException(com.chatbot.shared.exceptions.ErrorCode.LICENSE_EXPIRED, "License has expired");
            } else {
                throw new LicenseException(com.chatbot.shared.exceptions.ErrorCode.LICENSE_INACTIVE, "License is inactive");
            }
        }
        
        return LicenseResponse.from(license, user.getEmail());
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "licenses", key = "#request.userId"),
        @CacheEvict(value = "license-features", allEntries = true),
        @CacheEvict(value = "license-modules", allEntries = true),
        @CacheEvict(value = "license-limits", allEntries = true)
    })
    public LicenseResponse createLicense(CreateLicenseRequest request) {
        log.info("Creating license for user: {} with plan: {}", request.getUserId(), request.getPlanName());
        
        User user = userService.getUser(request.getUserId());
        
        // Check if user already has an active license
        if (licenseRepository.hasActiveLicense(request.getUserId())) {
            throw new LicenseException(com.chatbot.shared.exceptions.ErrorCode.CONFLICT, "User already has an active license");
        }
        
        License license = License.builder()
                .userId(user.getId())
                .planName(request.getPlanName())
                .isActive(request.getIsActive())
                .expiresAt(request.getExpiresAt())
                .features(request.getFeatures())
                .modules(request.getModules())
                .limits(request.getLimits())
                .build();
        
        License savedLicense = licenseRepository.save(license);
        log.info("License created successfully with ID: {}", savedLicense.getId());
        
        return LicenseResponse.from(savedLicense, user.getEmail());
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "licenses", allEntries = true),
        @CacheEvict(value = "license-features", allEntries = true),
        @CacheEvict(value = "license-modules", allEntries = true),
        @CacheEvict(value = "license-limits", allEntries = true)
    })
    public LicenseResponse updateLicense(Long licenseId, UpdateLicenseRequest request) {
        log.info("Updating license: {}", licenseId);
        
        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new LicenseNotFoundException("License not found"));
        
        if (request.getPlanName() != null) {
            license.setPlanName(request.getPlanName());
        }
        
        if (request.getIsActive() != null) {
            license.setIsActive(request.getIsActive());
        }
        
        if (request.getExpiresAt() != null) {
            license.setExpiresAt(request.getExpiresAt());
        }
        
        if (request.getFeatures() != null) {
            license.setFeatures(request.getFeatures());
        }
        
        if (request.getModules() != null) {
            license.setModules(request.getModules());
        }
        
        if (request.getLimits() != null) {
            license.setLimits(request.getLimits());
        }
        
        License updatedLicense = licenseRepository.save(license);
        log.info("License updated successfully: {}", licenseId);
        
        User user = userService.getUser(license.getUserId());
        return LicenseResponse.from(updatedLicense, user.getEmail());
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "licenses", allEntries = true),
        @CacheEvict(value = "license-features", allEntries = true),
        @CacheEvict(value = "license-modules", allEntries = true),
        @CacheEvict(value = "license-limits", allEntries = true)
    })
    public void revokeLicense(Long licenseId) {
        log.info("Revoking license: {}", licenseId);
        
        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new LicenseNotFoundException("License not found"));
        
        license.setIsActive(false);
        licenseRepository.save(license);
        
        log.info("License revoked successfully: {}", licenseId);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "licenses", key = "#userId"),
        @CacheEvict(value = "license-features", allEntries = true),
        @CacheEvict(value = "license-modules", allEntries = true),
        @CacheEvict(value = "license-limits", allEntries = true)
    })
    public void revokeLicenseForUser(Long userId) {
        log.info("Revoking all licenses for user: {}", userId);
        
        List<License> licenses = licenseRepository.findByUserIdAndIsActive(userId, true);
        
        for (License license : licenses) {
            license.setIsActive(false);
            licenseRepository.save(license);
        }
        
        log.info("Revoked {} licenses for user: {}", licenses.size(), userId);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveLicense(Long userId) {
        return licenseRepository.hasActiveLicense(userId);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "license-features", key = "#userId + ':' + #feature")
    public boolean hasFeature(Long userId, String feature) {
        Optional<License> licenseOpt = licenseRepository.findActiveLicenseByUserId(userId);
        return licenseOpt.map(license -> license.hasFeature(feature)).orElse(false);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "license-modules", key = "#userId + ':' + #module")
    public boolean hasModule(Long userId, String module) {
        Optional<License> licenseOpt = licenseRepository.findActiveLicenseByUserId(userId);
        return licenseOpt.map(license -> license.hasModule(module)).orElse(false);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "license-limits", key = "#userId + ':' + #limitKey")
    public Integer getLimit(Long userId, String limitKey) {
        Optional<License> licenseOpt = licenseRepository.findActiveLicenseByUserId(userId);
        return licenseOpt.map(license -> license.getLimit(limitKey)).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<License> getExpiredLicenses() {
        return licenseRepository.findExpiredLicenses();
    }

    // --- DEVICE BINDING LOGIC ---

    @Transactional
    @CacheEvict(value = "device-bindings", key = "#userId")
    public DeviceBindingResponse bindDevice(Long userId, String deviceId, String deviceName) {
        log.info("Binding device {} for user {}", deviceId, userId);
        
        Optional<DeviceBinding> existingBindingOpt = deviceBindingRepository.findByUserIdAndDeviceId(userId, deviceId);
        
        if (existingBindingOpt.isPresent()) {
            DeviceBinding binding = existingBindingOpt.get();
            binding.setStatus("ACTIVE");
            binding.setLastSeenAt(Instant.now());
            if (deviceName != null && !deviceName.isBlank()) {
                binding.setDeviceName(deviceName);
            }
            DeviceBinding saved = deviceBindingRepository.save(binding);
            log.info("Updated existing device binding ID: {} for device {}", saved.getId(), deviceId);
            return DeviceBindingResponse.from(saved);
        }
        
        long activeDeviceCount = deviceBindingRepository.countByUserIdAndStatus(userId, "ACTIVE");
        if (activeDeviceCount >= MAX_DEVICES_PER_USER) {
            throw new LicenseException(
                com.chatbot.shared.exceptions.ErrorCode.LIMIT_EXCEEDED,
                "Quá giới hạn số lượng thiết bị kích hoạt (tối đa " + MAX_DEVICES_PER_USER + " thiết bị). Vui lòng hủy liên kết thiết bị cũ trước khi kích hoạt thiết bị mới."
            );
        }
        
        DeviceBinding newBinding = DeviceBinding.builder()
                .userId(userId)
                .deviceId(deviceId)
                .deviceName(deviceName != null && !deviceName.isBlank() ? deviceName : "Local Client (" + deviceId + ")")
                .status("ACTIVE")
                .activatedAt(Instant.now())
                .lastSeenAt(Instant.now())
                .build();
        
        DeviceBinding saved = deviceBindingRepository.save(newBinding);
        log.info("Bound new device ID: {} for user {}", saved.getId(), userId);
        return DeviceBindingResponse.from(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "device-bindings", key = "#userId")
    public List<DeviceBindingResponse> getUserDevices(Long userId) {
        log.debug("Fetching device bindings for user: {}", userId);
        return deviceBindingRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .stream()
                .map(DeviceBindingResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "device-bindings", key = "#userId")
    public void unbindDevice(Long userId, String deviceId) {
        log.info("Unbinding device {} for user {}", deviceId, userId);
        
        Optional<DeviceBinding> bindingOpt = deviceBindingRepository.findByUserIdAndDeviceId(userId, deviceId);
        if (bindingOpt.isEmpty()) {
            throw new LicenseNotFoundException("Thiết bị không tồn tại hoặc không thuộc tài khoản của bạn.");
        }
        
        DeviceBinding binding = bindingOpt.get();
        binding.setStatus("REVOKED");
        deviceBindingRepository.save(binding);
        log.info("Unbound device {} for user {}", deviceId, userId);
    }
}
