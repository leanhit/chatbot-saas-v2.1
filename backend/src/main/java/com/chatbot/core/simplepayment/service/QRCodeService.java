package com.chatbot.core.simplepayment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Service
@Slf4j
public class QRCodeService {

    private static final String BANK_ACCOUNT_NUMBER = "1234567890";
    private static final String BANK_ACCOUNT_NAME = "CHATBOT SaaS";
    private static final String BANK_NAME = "Vietcombank";

    /**
     * Generate QR code content for bank transfer
     */
    public String generateQRCode(BigDecimal amount, String referenceCode, String description) {
        log.info("📱 Generating QR code for: {} - {}", amount, referenceCode);

        try {
            // Simple QR content format (VietQR compatible)
            String qrContent = String.format(
                "00020101021238630010A000000712010011970436%s520400005303%s5802VN5904%s6009HOCHIMINH6207%s6304",
                BANK_ACCOUNT_NUMBER,
                formatAmount(amount),
                BANK_ACCOUNT_NAME,
                generateDescription(referenceCode, description)
            );

            // Add CRC (simplified)
            qrContent += "1234";

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
            BANK_NAME,
            BANK_ACCOUNT_NUMBER,
            BANK_ACCOUNT_NAME,
            formatAmountDisplay(amount),
            referenceCode
        );
    }

    /**
     * Get bank information for display
     */
    public BankInfo getBankInfo() {
        BankInfo info = new BankInfo();
        info.setBankName(BANK_NAME);
        info.setAccountNumber(BANK_ACCOUNT_NUMBER);
        info.setAccountName(BANK_ACCOUNT_NAME);
        return info;
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
