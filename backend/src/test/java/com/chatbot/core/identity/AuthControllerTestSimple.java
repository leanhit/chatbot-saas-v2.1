package com.chatbot.core.identity;

import com.chatbot.core.identity.controller.AuthController;
import com.chatbot.core.identity.dto.LoginRequest;
import com.chatbot.core.identity.dto.RegisterRequest;
import com.chatbot.core.identity.dto.UserResponse;
import com.chatbot.core.identity.dto.UserDto;
import com.chatbot.core.identity.service.AuthService;
import com.chatbot.core.identity.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

public class AuthControllerTestSimple {

    private MockMvc mockMvc;
    
    @Mock
    private AuthService authService;

    @Mock
    private JwtService jwtService;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        AuthController authController = new AuthController(authService, jwtService);
        this.mockMvc = standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testLogin_Success() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        UserDto mockUserDto = new UserDto(1L, "test@example.com", "USER");
        UserResponse mockResponse = new UserResponse("mock-jwt-token", "mock-refresh-token", mockUserDto);

        // Mock response
        when(authService.login(any(LoginRequest.class)))
            .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testRegister_Success() throws Exception {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Password123!");
        registerRequest.setConfirmPassword("Password123!");

        UserDto mockUserDto = new UserDto(1L, "test@example.com", "USER");
        UserResponse mockResponse = new UserResponse("mock-jwt-token", "mock-refresh-token", mockUserDto);

        // Mock response
        when(authService.register(any(RegisterRequest.class)))
            .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
