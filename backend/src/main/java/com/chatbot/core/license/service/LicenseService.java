package com.chatbot.core.license.service;

import com.chatbot.core.license.dto.CreateLicenseRequest;
import com.chatbot.core.license.dto.LicenseResponse;
import com.chatbot.core.license.dto.UpdateLicenseRequest;
import com.chatbot.core.license.exception.LicenseException;
import com.chatbot.core.license.exception.LicenseNotFoundException;
import com.chatbot.core.license.model.License;
import com.chatbot.core.license.repository.LicenseRepository;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LicenseService {

    private final LicenseRepository licenseRepository;
    private final UserService userService;

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

    public LicenseResponse createLicense(CreateLicenseRequest request) {
        log.info("Creating license for user: {} with plan: {}", request.getUserId(), request.getPlanName());
        
        User user = userService.getUser(request.getUserId());
        
        // Check if user already has an active license
        if (licenseRepository.hasActiveLicense(request.getUserId())) {
            throw new LicenseException(com.chatbot.shared.exceptions.ErrorCode.CONFLICT, "User already has an active license");
        }
        
        License license = License.builder()
                .userId(user.getId()) // Application-level join: store userId instead of User object
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
        
        // Application-level join: fetch user by userId
        User user = userService.getUser(license.getUserId());
        return LicenseResponse.from(updatedLicense, user.getEmail());
    }

    public void revokeLicense(Long licenseId) {
        log.info("Revoking license: {}", licenseId);
        
        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new LicenseNotFoundException("License not found"));
        
        license.setIsActive(false);
        licenseRepository.save(license);
        
        log.info("License revoked successfully: {}", licenseId);
    }

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
    public boolean hasFeature(Long userId, String feature) {
        Optional<License> licenseOpt = licenseRepository.findActiveLicenseByUserId(userId);
        return licenseOpt.map(license -> license.hasFeature(feature)).orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean hasModule(Long userId, String module) {
        Optional<License> licenseOpt = licenseRepository.findActiveLicenseByUserId(userId);
        return licenseOpt.map(license -> license.hasModule(module)).orElse(false);
    }

    @Transactional(readOnly = true)
    public Integer getLimit(Long userId, String limitKey) {
        Optional<License> licenseOpt = licenseRepository.findActiveLicenseByUserId(userId);
        return licenseOpt.map(license -> license.getLimit(limitKey)).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<License> getExpiredLicenses() {
        return licenseRepository.findExpiredLicenses();
    }
}
