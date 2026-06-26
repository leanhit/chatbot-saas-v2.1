package com.chatbot.spokes.facebook.webhook.service;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory để chọn chatbot provider dựa trên loại provider
 */
@Service
@Slf4j
public class ChatbotProviderFactory {

    private final Map<String, ChatbotProviderService> providerMap;

    @Autowired
    public ChatbotProviderFactory(List<ChatbotProviderService> providers) {
        // Tạo map của các providers theo loại
        this.providerMap = providers.stream()
                .collect(Collectors.toMap(
                        ChatbotProviderService::getProviderType,
                        Function.identity()
                ));
        
        log.info("🔧 Đã khởi tạo ChatbotProviderFactory với các providers: " + providerMap.keySet());
    }

    /**
     * Lấy provider dựa trên loại
     * @param providerType loại provider (BOTPRESS, PENNYBOT)
     * @return ChatbotProviderService
     * @throws IllegalArgumentException nếu không tìm thấy provider
     */
    public ChatbotProviderService getProvider(String providerType) {
        ChatbotProviderService provider = providerMap.get(providerType.toUpperCase());
        if (provider == null) {
            throw new IllegalArgumentException("Không tìm thấy chatbot provider: " + providerType + 
                    ". Các providers có sẵn: " + providerMap.keySet());
        }
        return provider;
    }

    /**
     * Lấy tất cả các providers có sẵn
     * @return Set của các loại provider
     */
    public java.util.Set<String> getAvailableProviders() {
        return providerMap.keySet();
    }

    /**
     * Kiểm tra provider có tồn tại không
     * @param providerType loại provider
     * @return true nếu tồn tại
     */
    public boolean hasProvider(String providerType) {
        return providerMap.containsKey(providerType.toUpperCase());
    }
}
