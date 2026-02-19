package com.chatbot.modules.penny.core.config;

import com.chatbot.modules.facebook.connection.service.FacebookConnectionService;
import com.chatbot.modules.facebook.webhook.service.ChatbotProviderFactory;
import com.chatbot.modules.facebook.webhook.service.ChatbotProviderService;
import com.chatbot.core.message.store.service.ConversationService;
import com.chatbot.core.message.store.service.MessageService;
import com.chatbot.modules.penny.analytics.AnalyticsCollector;
import com.chatbot.modules.penny.context.ContextManager;
import com.chatbot.modules.penny.context.storage.DatabaseContextStorage;
import com.chatbot.modules.penny.context.storage.RedisContextStorage;
import com.chatbot.modules.penny.core.PennyMiddlewareEngine;
import com.chatbot.modules.penny.error.ErrorHandler;
import com.chatbot.modules.penny.rules.BotRuleManager;
import com.chatbot.modules.penny.rules.BotRuleRepository;
import com.chatbot.modules.penny.rules.CustomLogicEngine;
import com.chatbot.modules.penny.rules.ResponseTemplateManager;
import com.chatbot.modules.penny.rules.ResponseTemplateRepository;
import com.chatbot.modules.penny.service.IntentAnalyzer;
import com.chatbot.modules.penny.routing.ProviderSelector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * Penny Middleware Configuration - Spring Boot auto-configuration
 */
@Configuration
@EnableConfigurationProperties(PennyProperties.class)
@EnableScheduling
@Slf4j
public class PennyConfig {
    
    /**
     * Main Penny Middleware Engine
     */
    @Bean
    @ConditionalOnMissingBean
    public PennyMiddlewareEngine pennyMiddlewareEngine(
            // Existing services from current system
            ChatbotProviderFactory existingProviderFactory,
            ConversationService existingConversationService,
            FacebookConnectionService existingFacebookConnectionService,
            MessageService existingMessageService,
            
            // New Penny services
            ContextManager contextManager,
            IntentAnalyzer intentAnalyzer,
            ProviderSelector providerSelector,
            ErrorHandler errorHandler,
            AnalyticsCollector analyticsCollector,
            CustomLogicEngine customLogicEngine) {
        
        log.info("🚀 Initializing Penny Middleware Engine...");
        
        PennyMiddlewareEngine engine = new PennyMiddlewareEngine(
            existingProviderFactory,
            existingConversationService,
            existingFacebookConnectionService,
            existingMessageService,
            contextManager,
            intentAnalyzer,
            providerSelector,
            errorHandler,
            analyticsCollector,
            customLogicEngine
        );
        
        log.info("✅ Penny Middleware Engine initialized successfully");
        return engine;
    }
    
    /**
     * Context Manager
     */
    @Bean
    @ConditionalOnMissingBean
    public ContextManager contextManager(PennyProperties properties,
                                        RedisTemplate<String, Object> redisTemplate) {
        log.info("🔄 Initializing Context Manager...");
        
        RedisContextStorage redisStorage = new RedisContextStorage(
            redisTemplate, 
            new com.fasterxml.jackson.databind.ObjectMapper()
        );
        
        DatabaseContextStorage databaseStorage = new DatabaseContextStorage();
        
        ContextManager contextManager = new ContextManager(redisStorage, databaseStorage);
        
        log.info("✅ Context Manager initialized with storage: {}", properties.getContext().getStorageType());
        return contextManager;
    }
    
    /**
     * Intent Analyzer
     */
    @Bean
    @ConditionalOnMissingBean
    public IntentAnalyzer intentAnalyzer(PennyProperties properties) {
        log.info("🧠 Initializing Intent Analyzer...");
        
        IntentAnalyzer intentAnalyzer = new IntentAnalyzer();
        
        log.info("✅ Intent Analyzer initialized (Vietnamese enabled: {})", 
            properties.getIntent().isVietnameseEnabled());
        return intentAnalyzer;
    }
    
    /**
     * Provider Selector
     */
    @Bean
    @ConditionalOnMissingBean
    public ProviderSelector providerSelector(PennyProperties properties,
                                          ChatbotProviderFactory providerFactory) {
        log.info("🎯 Initializing Provider Selector...");
        
        ProviderSelector providerSelector = new ProviderSelector();
        
        log.info("✅ Provider Selector initialized with strategy: {}", 
            properties.getProvider().getSelectionStrategy());
        return providerSelector;
    }
    
    /**
     * Error Handler
     */
    @Bean
    @ConditionalOnMissingBean
    public ErrorHandler errorHandler(PennyProperties properties) {
        log.info("⚠️ Initializing Error Handler...");
        
        ErrorHandler errorHandler = new ErrorHandler(properties);
        
        log.info("✅ Error Handler initialized (Circuit breaker: {}, Fallback: {})", 
            properties.getError().isCircuitbreakerEnabled(),
            properties.getError().isFallbackEnabled());
        return errorHandler;
    }
    
    /**
     * Bot Rule Manager
     */
    @Bean
    @ConditionalOnMissingBean
    public BotRuleManager botRuleManager(BotRuleRepository botRuleRepository,
                                        ResponseTemplateRepository responseTemplateRepository,
                                        com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        log.info("� Initializing Bot Rule Manager...");
        
        BotRuleManager botRuleManager = new BotRuleManager(botRuleRepository, responseTemplateRepository, objectMapper);
        
        log.info("✅ Bot Rule Manager initialized successfully");
        return botRuleManager;
    }
    
    /**
     * Response Template Manager
     */
    @Bean
    @ConditionalOnMissingBean
    public ResponseTemplateManager responseTemplateManager(ResponseTemplateRepository responseTemplateRepository,
                                                        com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        log.info("📋 Initializing Response Template Manager...");
        
        ResponseTemplateManager responseTemplateManager = new ResponseTemplateManager(responseTemplateRepository, objectMapper);
        
        log.info("✅ Response Template Manager initialized successfully");
        return responseTemplateManager;
    }
    
    /**
     * Custom Logic Engine
     */
    @Bean
    @ConditionalOnMissingBean
    public CustomLogicEngine customLogicEngine(BotRuleManager botRuleManager,
                                              ResponseTemplateManager responseTemplateManager) {
        log.info("🧠 Initializing Custom Logic Engine...");
        
        CustomLogicEngine customLogicEngine = new CustomLogicEngine(botRuleManager, responseTemplateManager);
        
        log.info("✅ Custom Logic Engine initialized successfully");
        return customLogicEngine;
    }
    
    /**
     * Penny Properties
     */
    @Bean
    @ConditionalOnMissingBean
    @Primary
    public PennyProperties pennyProperties() {
        return new PennyProperties();
    }
    
    /**
     * Provider Health Monitor (optional)
     */
    @Bean
    @ConditionalOnProperty(prefix = "penny.provider", name = "health.monitor.enabled", havingValue = "true")
    public ProviderHealthMonitor providerHealthMonitor(List<ChatbotProviderService> providers,
                                                     ProviderSelector providerSelector) {
        log.info("🏥 Initializing Provider Health Monitor...");
        
        ProviderHealthMonitor monitor = new ProviderHealthMonitor(providers, providerSelector);
        
        log.info("✅ Provider Health Monitor initialized for {} providers", providers.size());
        return monitor;
    }
    
        
    /**
     * Provider Health Monitor Service
     */
    public static class ProviderHealthMonitor {
        private final List<ChatbotProviderService> providers;
        private final ProviderSelector providerSelector;
        
        public ProviderHealthMonitor(List<ChatbotProviderService> providers,
                                   ProviderSelector providerSelector) {
            this.providers = providers;
            this.providerSelector = providerSelector;
        }
        
        /**
         * Check health of all providers
         */
        public void checkAllProviders() {
            for (ChatbotProviderService provider : providers) {
                try {
                    boolean isHealthy = provider.healthCheck("test-bot-id");
                    String providerType = provider.getProviderType();
                    
                    providerSelector.updateProviderHealth(
                        ProviderSelector.ProviderType.valueOf(providerType),
                        isHealthy,
                        isHealthy ? "Health check passed" : "Health check failed"
                    );
                    
                } catch (Exception e) {
                    log.error("❌ Health check failed for provider: {}", e.getMessage(), e);
                }
            }
        }
    }
}
