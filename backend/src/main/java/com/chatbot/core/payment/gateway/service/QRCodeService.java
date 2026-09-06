package com.chatbot.core.payment.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class QRCodeService {

    // private final SimplePaymentBankConfig bankConfig;

    /**
     * Generate QR code content for bank transfer
     * This implements the VietQR standard
     */
    public String generateQRCode(BigDecimal amount, String referenceCode, String description) {
        log.info("📱 Generating QR code for amount: {}, reference: {}", amount, referenceCode);

        // VietQR format: 000201|010212|5303704|5802VN|5910CHATBOT SaaS|6007VIETCOMBANK|62071234567890|6304{CRC}
        
        StringBuilder qrBuilder = new StringBuilder();
        qrBuilder.append("000201"); // Payload Format Indicator
        qrBuilder.append("010212"); // Merchant Account Information
        qrBuilder.append("5303704"); // Transaction Currency (VND)
        qrBuilder.append("5407").append(formatAmount(amount)); // Transaction Amount
        qrBuilder.append("5802VN"); // Country Code
        qrBuilder.append("5910CHATBOT SaaS"); // Merchant Name
        qrBuilder.append("6007VIETCOMBANK"); // Merchant City
        qrBuilder.append("62071234567890"); // Account Number
        qrBuilder.append("6304"); // CRC placeholder

        // Calculate and append CRC
        String crc = calculateCRC(qrBuilder.toString());
        qrBuilder.append(crc);

        String qrContent = qrBuilder.toString();
        log.info("✅ QR code generated: {}", qrContent);

        return qrContent;
    }

    /**
     * Get bank information for display
     */
    public BankInfo getBankInfo() {
        return BankInfo.builder()
                .bankName("Vietcombank")
                .accountNumber("1234567890")
                .accountName("CHATBOT SaaS")
                .branch("Ho Chi Minh City")
                .build();
    }

    /**
     * Format amount for QR code (7 digits, no decimal)
     */
    private String formatAmount(BigDecimal amount) {
        // Convert to integer (no decimal) and pad to 7 digits
        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();
        return String.format("%07d", amountInCents);
    }

    /**
     * Calculate CRC-16 for QR code
     */
    private String calculateCRC(String data) {
        // Simplified CRC calculation for demo
        // In production, use proper CRC-16-CCITT implementation
        int crc = 0xFFFF;
        for (int i = 0; i < data.length(); i++) {
            crc ^= (data.charAt(i) << 8);
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc = crc << 1;
                }
            }
        }
        return String.format("%04X", crc & 0xFFFF);
    }

    /**
     * Generate QR code image (base64 encoded)
     * This would use a QR code library in production
     */
    public String generateQRImage(String qrContent) {
        log.info("📱 Generating QR image for content: {}", qrContent);
        
        // Placeholder - in production, use ZXing or similar library
        // For now, return a placeholder base64 string
        String placeholder = "QR_CODE_IMAGE_PLACEHOLDER_" + qrContent.hashCode();
        return Base64.getEncoder().encodeToString(placeholder.getBytes(StandardCharsets.UTF_8));
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BankInfo {
        private String bankName;
        private String accountNumber;
        private String accountName;
        private String branch;
    }
}
