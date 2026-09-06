package com.chatbot.core.payment.controller;

import com.chatbot.core.payment.transaction.controller.PublicSimplePaymentController;
import com.chatbot.core.payment.transaction.dto.DepositRequest;
import com.chatbot.core.payment.transaction.dto.DepositResponse;
import com.chatbot.core.payment.transaction.service.SimplePaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PublicSimplePaymentControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SimplePaymentService simplePaymentService;

    @InjectMocks
    private PublicSimplePaymentController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        // Mock SecurityContext manually since we are using standalone setup
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("testuser@example.com", "password", Collections.emptyList())
        );
    }

    @Test
    void testCreateDepositEndpoint() throws Exception {
        DepositRequest request = new DepositRequest();
        request.setAmount(new BigDecimal("50000"));
        request.setCurrency("VND");

        mockMvc.perform(post("/api/public/payment/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isBadRequest());
    }
}
