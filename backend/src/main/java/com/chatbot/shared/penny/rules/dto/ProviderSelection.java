package com.chatbot.shared.penny.rules.dto;

import com.chatbot.shared.penny.routing.ProviderSelector;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Provider Selection DTO - Kết quả chọn provider cho custom logic
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderSelection {
    
    /**
     * Selected provider type
     */
    private ProviderSelector.ProviderType providerType;
    
    /**
     * Provider instance
     */
    private Object provider;
    
    /**
     * Reason for selection
     */
    private String selectionReason;
    
    /**
     * Confidence score
     */
    private Double confidence;
    
    /**
     * Fallback providers
     */
    private List<ProviderSelector.ProviderType> fallbackProviders;
    
    /**
     * Selection timestamp
     */
    private Instant timestamp;
    
    /**
     * Selection metadata
     */
    private Object metadata;
}
