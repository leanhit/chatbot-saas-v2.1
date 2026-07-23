package com.chatbot.core.tenant.integration;

import com.chatbot.core.tenant.controller.TenantController;
import com.chatbot.core.tenant.dto.CreateTenantRequest;
import com.chatbot.core.tenant.dto.TenantResponse;
import com.chatbot.core.tenant.dto.TenantBasicInfoRequest;
import com.chatbot.core.tenant.dto.TenantContactInfoRequest;
import com.chatbot.core.tenant.service.TenantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TenantController.class)
class TenantIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TenantService tenantService;

    @Test
    void createTenant_Success() throws Exception {
        // Arrange
        CreateTenantRequest request = new CreateTenantRequest();
        request.setName("Test Tenant");

        TenantResponse mockResponse = TenantResponse.builder()
                .id(1L)
                .name("Test Tenant")
                .tenantKey("test-tenant-key")
                .build();

        when(tenantService.createTenant(any(CreateTenantRequest.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Tenant"))
                .andExpect(jsonPath("$.tenantKey").value("test-tenant-key"));
    }

    @Test
    void createTenant_MissingName() throws Exception {
        // Arrange
        CreateTenantRequest request = new CreateTenantRequest();
        // Missing name

        // Act & Assert
        mockMvc.perform(post("/api/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTenant_Success() throws Exception {
        // Arrange
        TenantResponse mockResponse = TenantResponse.builder()
                .id(1L)
                .name("Test Tenant")
                .tenantKey("test-tenant-key")
                .build();

        when(tenantService.getTenantForCurrentUser(anyLong())).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/tenants/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Tenant"));
    }

    @Test
    void getTenant_NotFound() throws Exception {
        // Arrange
        when(tenantService.getTenantForCurrentUser(anyLong()))
            .thenThrow(new com.chatbot.core.tenant.exception.TenantNotFoundException("Tenant not found"));

        // Act & Assert
        mockMvc.perform(get("/api/tenants/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBasicInfo_Success() throws Exception {
        // Arrange
        TenantBasicInfoRequest request = new TenantBasicInfoRequest();
        request.setName("Updated Name");

        TenantResponse mockResponse = TenantResponse.builder()
                .id(1L)
                .name("Updated Name")
                .tenantKey("test-tenant-key")
                .build();

        when(tenantService.updateBasicInfo(anyString(), any(TenantBasicInfoRequest.class)))
            .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(put("/api/tenants/test-tenant-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void updateBasicInfo_EmptyTenantKey() throws Exception {
        // Arrange
        TenantBasicInfoRequest request = new TenantBasicInfoRequest();
        request.setName("Updated Name");

        when(tenantService.updateBasicInfo(anyString(), any(TenantBasicInfoRequest.class)))
            .thenThrow(new com.chatbot.core.tenant.exception.InvalidTenantKeyException("Tenant key cannot be empty"));

        // Act & Assert
        mockMvc.perform(put("/api/tenants/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateContactInfo_Success() throws Exception {
        // Arrange
        TenantContactInfoRequest request = new TenantContactInfoRequest();
        request.setEmail("contact@example.com");
        request.setPhone("1234567890");
        request.setWebsite("https://example.com");

        TenantResponse mockResponse = TenantResponse.builder()
                .id(1L)
                .name("Test Tenant")
                .tenantKey("test-tenant-key")
                .build();

        when(tenantService.updateContactInfo(anyString(), any(TenantContactInfoRequest.class)))
            .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(put("/api/tenants/test-tenant-key/contact")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void suspendTenant_Success() throws Exception {
        // Arrange
        doNothing().when(tenantService).suspendTenant(anyLong());

        // Act & Assert
        mockMvc.perform(post("/api/tenants/1/suspend"))
                .andExpect(status().isOk());
    }

    @Test
    void activateTenant_Success() throws Exception {
        // Arrange
        doNothing().when(tenantService).activateTenant(anyLong());

        // Act & Assert
        mockMvc.perform(post("/api/tenants/1/activate"))
                .andExpect(status().isOk());
    }

    @Test
    void deactivateTenant_Success() throws Exception {
        // Arrange
        doNothing().when(tenantService).deactivateTenant(anyLong());

        // Act & Assert
        mockMvc.perform(post("/api/tenants/1/deactivate"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteTenant_Success() throws Exception {
        // Arrange
        doNothing().when(tenantService).deleteTenant(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/tenants/1"))
                .andExpect(status().isOk());
    }

    @Test
    void switchTenant_Success() throws Exception {
        // Arrange
        TenantResponse mockResponse = TenantResponse.builder()
                .id(1L)
                .name("Test Tenant")
                .tenantKey("test-tenant-key")
                .build();

        when(tenantService.switchTenant(anyLong())).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/tenants/1/switch"))
                .andExpect(status().isOk());
    }

    @Test
    void switchTenantByKey_Success() throws Exception {
        // Arrange
        TenantResponse mockResponse = TenantResponse.builder()
                .id(1L)
                .name("Test Tenant")
                .tenantKey("test-tenant-key")
                .build();

        when(tenantService.switchTenantByKey(anyString())).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/tenants/switch/test-tenant-key"))
                .andExpect(status().isOk());
    }
}
