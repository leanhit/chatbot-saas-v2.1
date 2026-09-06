package com.chatbot.core.payment.service;

import com.chatbot.core.payment.gateway.service.QRCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class QRCodeServiceTest {

    private QRCodeService qrCodeService;

    @BeforeEach
    void setUp() {
        qrCodeService = new QRCodeService();
    }

    @Test
    void testGenerateQRCodeCRC() {
        BigDecimal amount = new BigDecimal("50000");
        String referenceCode = "PAYTEST123456";
        String description = "Nap tien test";
        
        String qrContent = qrCodeService.generateQRCode(amount, referenceCode, description);
        
        assertNotNull(qrContent);
        assertTrue(qrContent.startsWith("000201010212"));
        
        String crc = qrContent.substring(qrContent.length() - 4);
        assertEquals(4, crc.length());
    }

    @Test
    void testGetBankInfo() {
        QRCodeService.BankInfo bankInfo = qrCodeService.getBankInfo();
        assertEquals("Vietcombank", bankInfo.getBankName());
        assertEquals("1234567890", bankInfo.getAccountNumber());
        assertEquals("CHATBOT SaaS", bankInfo.getAccountName());
    }
}
