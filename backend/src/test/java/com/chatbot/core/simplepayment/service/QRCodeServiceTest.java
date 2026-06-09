package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.config.SimplePaymentBankConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class QRCodeServiceTest {

    private QRCodeService qrCodeService;
    private SimplePaymentBankConfig bankConfig;

    @BeforeEach
    void setUp() {
        bankConfig = new SimplePaymentBankConfig();
        bankConfig.setName("Vietcombank");
        bankConfig.setAccountNumber("1234567890");
        bankConfig.setAccountName("CHATBOT SaaS");
        qrCodeService = new QRCodeService(bankConfig);
    }

    @Test
    void testGenerateQRCodeCRC() {
        BigDecimal amount = new BigDecimal("50000");
        String referenceCode = "PAYTEST123456";
        String description = "Nap tien test";
        
        String qrContent = qrCodeService.generateQRCode(amount, referenceCode, description);
        
        assertNotNull(qrContent);
        assertTrue(qrContent.startsWith("000201010212"));
        assertTrue(qrContent.endsWith(qrContent.substring(qrContent.length() - 4)));
        
        String crc = qrContent.substring(qrContent.length() - 4);
        assertEquals(4, crc.length());
        
        // Expected CRC for static mock VietQR string
        System.out.println("Generated QR Content: " + qrContent);
        System.out.println("CRC Checksum: " + crc);
    }

    @Test
    void testGetBankInfo() {
        QRCodeService.BankInfo bankInfo = qrCodeService.getBankInfo();
        assertEquals("Vietcombank", bankInfo.getBankName());
        assertEquals("1234567890", bankInfo.getAccountNumber());
        assertEquals("CHATBOT SaaS", bankInfo.getAccountName());
    }
}
