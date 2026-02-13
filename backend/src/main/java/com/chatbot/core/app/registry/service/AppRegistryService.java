package com.chatbot.core.app.registry.service;

import com.chatbot.core.app.registry.dto.AppResponse;
import com.chatbot.core.app.registry.dto.RegisterAppRequest;
import com.chatbot.core.app.registry.model.AppRegistry;
import com.chatbot.core.app.registry.model.AppStatus;
import com.chatbot.core.app.registry.repository.AppRegistryRepository;
import com.chatbot.shared.exceptions.ResourceNotFoundException;
import com.chatbot.shared.exceptions.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppRegistryService {
    
    @Autowired
    private AppRegistryRepository appRegistryRepository;
    
    public AppResponse registerApp(RegisterAppRequest request, Long createdBy) {
        // Check if app name already exists
        if (appRegistryRepository.existsByName(request.getName())) {
            throw new ValidationException("App with name '" + request.getName() + "' already exists");
        }
        
        AppRegistry app = new AppRegistry();
        app.setName(request.getName());
        app.setDisplayName(request.getDisplayName());
        app.setDescription(request.getDescription());
        app.setAppType(request.getAppType());
        app.setVersion(request.getVersion());
        app.setApiEndpoint(request.getApiEndpoint());
        app.setWebhookUrl(request.getWebhookUrl());
        app.setConfigSchema(request.getConfigSchema());
        app.setDefaultConfig(request.getDefaultConfig());
        app.setIsPublic(request.getIsPublic());
        app.setCreatedBy(createdBy);
        
        AppRegistry savedApp = appRegistryRepository.save(app);
        return convertToResponse(savedApp);
    }
    
    @Transactional(readOnly = true)
    public AppResponse getAppById(Long id) {
        AppRegistry app = appRegistryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("App not found with id: " + id));
        return convertToResponse(app);
    }
    
    @Transactional(readOnly = true)
    public AppResponse getAppByName(String name) {
        AppRegistry app = appRegistryRepository.findByName(name)
            .orElseThrow(() -> new ResourceNotFoundException("App not found with name: " + name));
        return convertToResponse(app);
    }
    
    @Transactional(readOnly = true)
    public Page<AppResponse> searchApps(String name, String appType, String status, Boolean isActive, Pageable pageable) {
        return appRegistryRepository.searchApps(name, 
            appType != null ? com.chatbot.core.app.registry.model.AppType.valueOf(appType.toUpperCase()) : null,
            status != null ? AppStatus.valueOf(status.toUpperCase()) : null,
            isActive, pageable)
            .map(this::convertToResponse);
    }
    
    @Transactional(readOnly = true)
    public List<AppResponse> getAppsByType(String appType) {
        List<AppRegistry> apps = appRegistryRepository.findByAppType(
            com.chatbot.core.app.registry.model.AppType.valueOf(appType.toUpperCase()));
        return apps.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AppResponse> getActiveApps() {
        List<AppRegistry> apps = appRegistryRepository.findByIsActive(true);
        return apps.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AppResponse> getPublicApps() {
        List<AppRegistry> apps = appRegistryRepository.findByIsPublic(true);
        return apps.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    public AppResponse updateApp(Long id, RegisterAppRequest request, Long updatedBy) {
        AppRegistry existingApp = appRegistryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("App not found with id: " + id));
        
        // Check if name is being changed and if new name already exists
        if (!existingApp.getName().equals(request.getName()) && 
            appRegistryRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new ValidationException("App with name '" + request.getName() + "' already exists");
        }
        
        existingApp.setName(request.getName());
        existingApp.setDisplayName(request.getDisplayName());
        existingApp.setDescription(request.getDescription());
        existingApp.setAppType(request.getAppType());
        existingApp.setVersion(request.getVersion());
        existingApp.setApiEndpoint(request.getApiEndpoint());
        existingApp.setWebhookUrl(request.getWebhookUrl());
        existingApp.setConfigSchema(request.getConfigSchema());
        existingApp.setDefaultConfig(request.getDefaultConfig());
        existingApp.setIsPublic(request.getIsPublic());
        existingApp.setUpdatedBy(updatedBy);
        
        AppRegistry updatedApp = appRegistryRepository.save(existingApp);
        return convertToResponse(updatedApp);
    }
    
    public AppResponse updateAppStatus(Long id, AppStatus status, Long updatedBy) {
        AppRegistry app = appRegistryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("App not found with id: " + id));
        
        app.setStatus(status);
        app.setUpdatedBy(updatedBy);
        
        AppRegistry updatedApp = appRegistryRepository.save(app);
        return convertToResponse(updatedApp);
    }
    
    public void deleteApp(Long id) {
        if (!appRegistryRepository.existsById(id)) {
            throw new ResourceNotFoundException("App not found with id: " + id);
        }
        appRegistryRepository.deleteById(id);
    }
    
    public AppResponse toggleAppActivation(Long id, Boolean isActive, Long updatedBy) {
        AppRegistry app = appRegistryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("App not found with id: " + id));
        
        app.setIsActive(isActive);
        app.setUpdatedBy(updatedBy);
        
        AppRegistry updatedApp = appRegistryRepository.save(app);
        return convertToResponse(updatedApp);
    }
    
    private AppResponse convertToResponse(AppRegistry app) {
        AppResponse response = new AppResponse();
        response.setId(app.getId());
        response.setName(app.getName());
        response.setDisplayName(app.getDisplayName());
        response.setDescription(app.getDescription());
        response.setAppType(app.getAppType());
        response.setStatus(app.getStatus());
        response.setVersion(app.getVersion());
        response.setApiEndpoint(app.getApiEndpoint());
        response.setWebhookUrl(app.getWebhookUrl());
        response.setIsActive(app.getIsActive());
        response.setIsPublic(app.getIsPublic());
        response.setCreatedAt(app.getCreatedAt());
        response.setUpdatedAt(app.getUpdatedAt());
        response.setCreatedBy(app.getCreatedBy());
        response.setUpdatedBy(app.getUpdatedBy());
        
        // Convert configurations
        List<AppResponse.AppConfigurationDto> configDtos = app.getConfigurations().stream()
            .map(config -> {
                AppResponse.AppConfigurationDto dto = new AppResponse.AppConfigurationDto();
                dto.setId(config.getId());
                dto.setConfigKey(config.getConfigKey());
                dto.setConfigValue(config.getIsEncrypted() ? "***ENCRYPTED***" : config.getConfigValue());
                dto.setConfigType(config.getConfigType());
                dto.setIsRequired(config.getIsRequired());
                dto.setIsEncrypted(config.getIsEncrypted());
                dto.setDescription(config.getDescription());
                return dto;
            })
            .collect(Collectors.toList());
        response.setConfigurations(configDtos);
        
        return response;
    }
}
