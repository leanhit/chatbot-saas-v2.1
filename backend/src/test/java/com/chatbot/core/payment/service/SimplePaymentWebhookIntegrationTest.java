package com.chatbot.core.payment.service;

import com.chatbot.core.payment.common.audit.PaymentAuditService;
import com.chatbot.core.payment.common.metrics.PaymentMetricsService;
import com.chatbot.core.payment.gateway.model.Webhook;
import com.chatbot.core.payment.gateway.model.Webhook.WebhookEventType;
import com.chatbot.core.payment.gateway.repository.WebhookDeadLetterRepository;
import com.chatbot.core.payment.gateway.repository.WebhookRepository;
import com.chatbot.core.payment.gateway.service.WebhookService;
import com.chatbot.core.payment.gateway.service.WebhookSignatureService;
import com.chatbot.core.payment.transaction.model.PaymentStatus;
import com.chatbot.core.payment.transaction.model.SimplePayment;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimplePaymentWebhookIntegrationTest {

    @Mock
    private WebhookRepository webhookRepository;

    @Mock
    private WebhookDeadLetterRepository webhookDeadLetterRepository;

    @Mock
    private WebhookSignatureService webhookSignatureService;

    @Mock
    private PaymentAuditService paymentAuditService;

    @Mock
    private PaymentMetricsService paymentMetricsService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private WebClient webClient;

    private WebhookService webhookService;

    private Webhook testWebhook;
    private SimplePayment testPayment;

    @BeforeEach
    void setUp() {
        webhookService = new WebhookService(
                webhookRepository,
                webhookDeadLetterRepository,
                webhookSignatureService,
                paymentAuditService,
                paymentMetricsService,
                objectMapper,
                webClient
        );

        testWebhook = Webhook.builder()
                .id(1L)
                .name("Test Webhook")
                .url("https://example.com/webhook")
                .secret("test-secret")
                .isActive(true)
                .build();

        testPayment = SimplePayment.builder()
                .id(1L)
                .referenceCode("REF-" + UUID.randomUUID())
                .amount(new BigDecimal("100000"))
                .currency("VND")
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        reset(webhookRepository, objectMapper);
    }

    @Test
    void testCreateWebhook_SavesToDatabase() {
        when(webhookRepository.existsByUrl(anyString())).thenReturn(false);
        when(webhookRepository.save(any(Webhook.class))).thenReturn(testWebhook);

        Webhook result = webhookService.createWebhook(testWebhook);

        assertNotNull(result);
        verify(webhookRepository, times(1)).existsByUrl(anyString());
        verify(webhookRepository, times(1)).save(any(Webhook.class));
    }

    @Test
    void testCreateWebhook_UrlAlreadyExists() {
        when(webhookRepository.existsByUrl(anyString())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            webhookService.createWebhook(testWebhook);
        });

        verify(webhookRepository, times(1)).existsByUrl(anyString());
        verify(webhookRepository, never()).save(any(Webhook.class));
    }
}
