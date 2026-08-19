package com.chatbot.core.simplepayment.controller;

import com.chatbot.core.simplepayment.dto.DepositRequest;
import com.chatbot.core.simplepayment.dto.DepositResponse;
import com.chatbot.core.simplepayment.service.BankApiService;
import com.chatbot.core.simplepayment.service.QRCodeService;
import com.chatbot.core.simplepayment.service.SimplePaymentService;
import com.chatbot.core.simplepayment.validation.PaymentValidationService;
import com.chatbot.core.tenant.membership.model.TenantMember;
import com.chatbot.core.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.core.simplepayment.service.PaymentNotificationService;
import com.chatbot.core.tenant.service.TenantPackageService;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.UserRepository;
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
import java.util.Optional;

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
    @Mock
    private QRCodeService qrCodeService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BankApiService bankApiService;
    @Mock
    private TenantPackageService tenantPackageService;
    @Mock
    private TenantMemberRepository tenantMemberRepository;
    @Mock
    private PaymentValidationService paymentValidationService;
    @Mock
    private PaymentNotificationService paymentNotificationService;

    @InjectMocks
    private PublicSimplePaymentController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        // Setup mock user
        User mockUser = new User();
        mockUser.setId(1001L);
        mockUser.setEmail("testuser@example.com");
        Mockito.when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(mockUser));
        
        com.chatbot.core.tenant.model.Tenant mockTenant = new com.chatbot.core.tenant.model.Tenant();
        mockTenant.setId(3003L);
        
        TenantMember mockMembership = new TenantMember();
        mockMembership.setTenant(mockTenant);
        
        Mockito.when(tenantMemberRepository.findActiveTenantsOfUser(1001L))
               .thenReturn(Collections.singletonList(mockMembership));

        // Setup mock payment response
        DepositResponse mockResponse = new DepositResponse();
        mockResponse.setId(2002L);
        mockResponse.setReferenceCode("PAY-TEST-123");
        mockResponse.setAmount(new BigDecimal("50000"));
        mockResponse.setStatus("PENDING");
        mockResponse.setQrContent("00020101021238630010A00000071201001197043612345678905204000053037045405500005802VN5904CHATBOT SaaS6009HOCHIMINH6207PAY-TEST6304E8A3");

        Mockito.when(simplePaymentService.createDeposit(any(DepositRequest.class), eq(1001L), any()))
               .thenReturn(mockResponse);

        // Mock SecurityContext manually since we are using standalone setup
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("testuser@example.com", "password", Collections.emptyList())
        );
    }

    @Test
    void testCreateDepositSuccess() throws Exception {
        DepositRequest request = new DepositRequest();
        request.setAmount(new BigDecimal("50000"));
        request.setCurrency("VND");

        mockMvc.perform(post("/api/public/simple-payment/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(2002))
               .andExpect(jsonPath("$.referenceCode").value("PAY-TEST-123"))
               .andExpect(jsonPath("$.amount").value(50000))
               .andExpect(jsonPath("$.status").value("PENDING"))
               .andExpect(jsonPath("$.qrContent").exists());
               
        // Verify that the simplePaymentService was called correctly
        Mockito.verify(simplePaymentService).createDeposit(any(DepositRequest.class), eq(1001L), any());
    }
}
