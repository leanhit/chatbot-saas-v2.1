package com.chatbot.core.simplepayment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Service
@Slf4j
@lombok.RequiredArgsConstructor
public class QRCodeService {

    private final com.chatbot.core.simplepayment.config.SimplePaymentBankConfig bankConfig;

    /**
     * Generate QR code content for bank transfer
     */
    public String generateQRCode(BigDecimal amount, String referenceCode, String description) {
        log.info("📱 Generating QR code for: {} - {}", amount, referenceCode);

        try {
            // Simple QR content format (VietQR compatible)
            String qrContent = String.format(
                "00020101021238630010A000000712010011970436%s520400005303%s5802VN5904%s6009HOCHIMINH6207%s6304",
                bankConfig.getAccountNumber(),
                formatAmount(amount),
                bankConfig.getAccountName(),
                generateDescription(referenceCode, description)
            );

            // Add CRC (calculated accurately using CRC-16 CCITT False)
            qrContent += calculateCRC16(qrContent);

            log.info("✅ QR code generated successfully");
            return qrContent;

        } catch (Exception e) {
            log.error("❌ Failed to generate QR code: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    /**
     * Generate simple QR text for display
     */
    public String generateQRText(BigDecimal amount, String referenceCode) {
        return String.format(
            "Ngân hàng: %s\n" +
            "Số tài khoản: %s\n" +
            "Chủ tài khoản: %s\n" +
            "Số tiền: %s VNĐ\n" +
            "Nội dung: %s",
            bankConfig.getName(),
            bankConfig.getAccountNumber(),
            bankConfig.getAccountName(),
            formatAmountDisplay(amount),
            referenceCode
        );
    }

    /**
     * Get bank information for display
     */
    public BankInfo getBankInfo() {
        BankInfo info = new BankInfo();
        info.setBankName(bankConfig.getName());
        info.setAccountNumber(bankConfig.getAccountNumber());
        info.setAccountName(bankConfig.getAccountName());
        return info;
    }

    /**
     * Update bank information (admin only)
     */
    public void updateBankInfo(BankInfo bankInfo) {
        log.info("🏦 Updating bank info: {} - {}", bankInfo.getBankName(), bankInfo.getAccountNumber());
        bankConfig.setName(bankInfo.getBankName());
        bankConfig.setAccountNumber(bankInfo.getAccountNumber());
        bankConfig.setAccountName(bankInfo.getAccountName());
        log.info("✅ Bank info updated successfully");
    }

    /**
     * Calculate CRC-16 CCITT False checksum
     */
    private String calculateCRC16(String input) {
        int crc = 0xFFFF;          // initial value
        int polynomial = 0x1021;   // 0001 0000 0010 0001

        for (byte b : input.getBytes(java.nio.charset.StandardCharsets.US_ASCII)) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) {
                    crc ^= polynomial;
                }
            }
        }
        crc &= 0xFFFF;
        return String.format("%04X", crc);
    }

    private String formatAmount(BigDecimal amount) {
        // Format amount for QR code (12 digits, zero-padded)
        long amountInVnd = amount.longValue();
        return String.format("%012d", amountInVnd);
    }

    private String formatAmountDisplay(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }

    private String generateDescription(String referenceCode, String description) {
        if (description != null && !description.trim().isEmpty()) {
            return description.length() > 25 ? description.substring(0, 25) : description;
        }
        return "NAP " + referenceCode;
    }

    public static class BankInfo {
        private String bankName;
        private String accountNumber;
        private String accountName;

        // Getters and setters
        public String getBankName() { return bankName; }
        public void setBankName(String bankName) { this.bankName = bankName; }
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        public String getAccountName() { return accountName; }
        public void setAccountName(String accountName) { this.accountName = accountName; }
    }
}
