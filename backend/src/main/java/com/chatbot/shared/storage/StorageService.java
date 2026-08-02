package com.chatbot.shared.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Vendor-agnostic file storage service interface for Core modules.
 */
public interface StorageService {
    
    /**
     * Upload user avatar image
     */
    String uploadUserAvatar(Long userId, String userEmail, MultipartFile file);

    /**
     * Upload tenant logo image
     */
    String uploadTenantLogo(Long tenantId, String userEmail, MultipartFile file);
}
