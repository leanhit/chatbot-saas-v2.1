package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.model.SimplePayment;
import com.chatbot.core.simplepayment.model.PaymentStatus;
import com.chatbot.core.simplepayment.repository.SimplePaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SimplePaymentServiceTest {

    @Mock
    private SimplePaymentRepository paymentRepository;

    @InjectMocks
    private SimplePaymentService simplePaymentService;

    private SimplePayment testPayment;

    @BeforeEach
    void setUp() {
        testPayment = new SimplePayment();
        testPayment.setId(100L);
        testPayment.setReferenceCode("REF-123");
        testPayment.setAmount(BigDecimal.valueOf(100000));
        testPayment.setStatus(PaymentStatus.PENDING);
        testPayment.setUserId(1L);
        testPayment.setTenantId(1L);
    }

    @Test
    void getPaymentByReference_Success() {
        when(paymentRepository.findByReferenceCode("REF-123")).thenReturn(Optional.of(testPayment));

        SimplePayment payment = simplePaymentService.getPaymentByReference("REF-123");

        assertNotNull(payment);
        assertEquals("REF-123", payment.getReferenceCode());
    }

    @Test
    void getPaymentByReference_NotFound() {
        when(paymentRepository.findByReferenceCode("REF-999")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            simplePaymentService.getPaymentByReference("REF-999");
        });
    }
}
