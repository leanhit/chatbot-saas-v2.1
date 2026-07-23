package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.model.PaymentStatus;
import com.chatbot.core.simplepayment.exception.PaymentNotFoundException;
import com.chatbot.core.simplepayment.model.SimplePayment;
import com.chatbot.core.simplepayment.repository.SimplePaymentRepository;
import com.chatbot.core.identity.exception.UserNotFoundException;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEmailNotificationService {

    private final UserRepository userRepository;
    private final SimplePaymentRepository paymentRepository;

    private static final String FROM_EMAIL = "noreply@chatbot-saas.com";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Send payment success email
     */
    @Async
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public void sendPaymentSuccessEmail(String referenceCode) {
        log.info("📧 Sending payment success email for: {}", referenceCode);

        try {
            SimplePayment payment = paymentRepository.findByReferenceCode(referenceCode)
                    .orElseThrow(() -> new PaymentNotFoundException(referenceCode));

            User user = userRepository.findById(payment.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + payment.getUserId()));

            String subject = "Thanh toán thành công - " + referenceCode;
            String content = buildPaymentSuccessEmail(payment, user);

            sendEmail(user.getEmail(), subject, content);
            log.info("✅ Payment success email sent to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("❌ Failed to send payment success email for {}: {}", referenceCode, e.getMessage(), e);
        }
    }

    /**
     * Send payment failure email
     */
    @Async
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public void sendPaymentFailureEmail(String referenceCode) {
        log.info("📧 Sending payment failure email for: {}", referenceCode);

        try {
            SimplePayment payment = paymentRepository.findByReferenceCode(referenceCode)
                    .orElseThrow(() -> new PaymentNotFoundException(referenceCode));

            User user = userRepository.findById(payment.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + payment.getUserId()));

            String subject = "Thanh toán thất bại - " + referenceCode;
            String content = buildPaymentFailureEmail(payment, user);

            sendEmail(user.getEmail(), subject, content);
            log.info("✅ Payment failure email sent to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("❌ Failed to send payment failure email for {}: {}", referenceCode, e.getMessage(), e);
        }
    }

    /**
     * Send payment cancelled email
     */
    @Async
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public void sendPaymentCancelledEmail(String referenceCode) {
        log.info("📧 Sending payment cancelled email for: {}", referenceCode);

        try {
            SimplePayment payment = paymentRepository.findByReferenceCode(referenceCode)
                    .orElseThrow(() -> new PaymentNotFoundException(referenceCode));

            User user = userRepository.findById(payment.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + payment.getUserId()));

            String subject = "Thanh toán đã hủy - " + referenceCode;
            String content = buildPaymentCancelledEmail(payment, user);

            sendEmail(user.getEmail(), subject, content);
            log.info("✅ Payment cancelled email sent to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("❌ Failed to send payment cancelled email for {}: {}", referenceCode, e.getMessage(), e);
        }
    }

    /**
     * Send payment expired email
     */
    @Async
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public void sendPaymentExpiredEmail(String referenceCode) {
        log.info("📧 Sending payment expired email for: {}", referenceCode);

        try {
            SimplePayment payment = paymentRepository.findByReferenceCode(referenceCode)
                    .orElseThrow(() -> new PaymentNotFoundException(referenceCode));

            User user = userRepository.findById(payment.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + payment.getUserId()));

            String subject = "Thanh toán đã hết hạn - " + referenceCode;
            String content = buildPaymentExpiredEmail(payment, user);

            sendEmail(user.getEmail(), subject, content);
            log.info("✅ Payment expired email sent to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("❌ Failed to send payment expired email for {}: {}", referenceCode, e.getMessage(), e);
        }
    }

    /**
     * Send payment refund email
     */
    @Async
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public void sendPaymentRefundEmail(String referenceCode) {
        log.info("📧 Sending payment refund email for: {}", referenceCode);

        try {
            SimplePayment payment = paymentRepository.findByReferenceCode(referenceCode)
                    .orElseThrow(() -> new PaymentNotFoundException(referenceCode));

            User user = userRepository.findById(payment.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + payment.getUserId()));

            String subject = "Hoàn tiền - " + referenceCode;
            String content = buildPaymentRefundEmail(payment, user);

            sendEmail(user.getEmail(), subject, content);
            log.info("✅ Payment refund email sent to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("❌ Failed to send payment refund email for {}: {}", referenceCode, e.getMessage(), e);
        }
    }

    /**
     * Send package upgrade email
     */
    @Async
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public void sendPackageUpgradeEmail(String referenceCode, String packageId) {
        log.info("📧 Sending package upgrade email for: {}, package: {}", referenceCode, packageId);

        try {
            SimplePayment payment = paymentRepository.findByReferenceCode(referenceCode)
                    .orElseThrow(() -> new PaymentNotFoundException(referenceCode));

            User user = userRepository.findById(payment.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + payment.getUserId()));

            String subject = "Nâng cấp gói thành công - " + packageId;
            String content = buildPackageUpgradeEmail(payment, user, packageId);

            sendEmail(user.getEmail(), subject, content);
            log.info("✅ Package upgrade email sent to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("❌ Failed to send package upgrade email for {}: {}", referenceCode, e.getMessage(), e);
        }
    }

    private String buildPaymentSuccessEmail(SimplePayment payment, User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("Kính gửi ").append(user.getEmail()).append(",\n\n");
        sb.append("Chúng tôi xin thông báo thanh toán của bạn đã thành công!\n\n");
        sb.append("Thông tin thanh toán:\n");
        sb.append("- Mã tham chiếu: ").append(payment.getReferenceCode()).append("\n");
        sb.append("- Số tiền: ").append(formatAmount(payment.getAmount())).append(" ").append(payment.getCurrency()).append("\n");
        sb.append("- Thời gian: ").append(payment.getCompletedAt() != null ? 
            payment.getCompletedAt().format(DATE_FORMATTER) : "N/A").append("\n");
        
        if (payment.getTargetPackageId() != null) {
            sb.append("- Gói nâng cấp: ").append(payment.getTargetPackageId()).append("\n");
        }
        
        sb.append("\nCảm ơn bạn đã sử dụng dịch vụ!\n");
        sb.append("Trân trọng,\n");
        sb.append("Chatbot SaaS Team");
        
        return sb.toString();
    }

    private String buildPaymentFailureEmail(SimplePayment payment, User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("Kính gửi ").append(user.getEmail()).append(",\n\n");
        sb.append("Chúng tôi xin thông báo thanh toán của bạn đã thất bại.\n\n");
        sb.append("Thông tin thanh toán:\n");
        sb.append("- Mã tham chiếu: ").append(payment.getReferenceCode()).append("\n");
        sb.append("- Số tiền: ").append(formatAmount(payment.getAmount())).append(" ").append(payment.getCurrency()).append("\n");
        sb.append("- Thời gian: ").append(payment.getUpdatedAt() != null ? 
            payment.getUpdatedAt().format(DATE_FORMATTER) : "N/A").append("\n");
        
        sb.append("\nVui lòng thử lại hoặc liên hệ hỗ trợ nếu cần thiết.\n");
        sb.append("Trân trọng,\n");
        sb.append("Chatbot SaaS Team");
        
        return sb.toString();
    }

    private String buildPaymentCancelledEmail(SimplePayment payment, User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("Kính gửi ").append(user.getEmail()).append(",\n\n");
        sb.append("Thanh toán của bạn đã được hủy.\n\n");
        sb.append("Thông tin thanh toán:\n");
        sb.append("- Mã tham chiếu: ").append(payment.getReferenceCode()).append("\n");
        sb.append("- Số tiền: ").append(formatAmount(payment.getAmount())).append(" ").append(payment.getCurrency()).append("\n");
        
        sb.append("\nNếu bạn muốn tiếp tục thanh toán, vui lòng tạo yêu cầu mới.\n");
        sb.append("Trân trọng,\n");
        sb.append("Chatbot SaaS Team");
        
        return sb.toString();
    }

    private String buildPaymentExpiredEmail(SimplePayment payment, User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("Kính gửi ").append(user.getEmail()).append(",\n\n");
        sb.append("Thanh toán của bạn đã hết hạn.\n\n");
        sb.append("Thông tin thanh toán:\n");
        sb.append("- Mã tham chiếu: ").append(payment.getReferenceCode()).append("\n");
        sb.append("- Số tiền: ").append(formatAmount(payment.getAmount())).append(" ").append(payment.getCurrency()).append("\n");
        sb.append("- Thời gian hết hạn: ").append(payment.getExpiresAt() != null ? 
            payment.getExpiresAt().format(DATE_FORMATTER) : "N/A").append("\n");
        
        sb.append("\nVui lòng tạo yêu cầu thanh toán mới nếu bạn vẫn muốn tiếp tục.\n");
        sb.append("Trân trọng,\n");
        sb.append("Chatbot SaaS Team");
        
        return sb.toString();
    }

    private String buildPaymentRefundEmail(SimplePayment payment, User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("Kính gửi ").append(user.getEmail()).append(",\n\n");
        sb.append("Chúng tôi đã hoàn tiền cho thanh toán của bạn.\n\n");
        sb.append("Thông tin hoàn tiền:\n");
        sb.append("- Mã tham chiếu: ").append(payment.getReferenceCode()).append("\n");
        sb.append("- Số tiền hoàn: ").append(formatAmount(payment.getAmount())).append(" ").append(payment.getCurrency()).append("\n");
        sb.append("- Lý do: ").append(extractRefundReason(payment.getDescription())).append("\n");
        
        sb.append("\nSố tiền sẽ được hoàn vào tài khoản của bạn trong vài ngày làm việc.\n");
        sb.append("Trân trọng,\n");
        sb.append("Chatbot SaaS Team");
        
        return sb.toString();
    }

    private String buildPackageUpgradeEmail(SimplePayment payment, User user, String packageId) {
        StringBuilder sb = new StringBuilder();
        sb.append("Kính gửi ").append(user.getEmail()).append(",\n\n");
        sb.append("Chúc mừng! Bạn đã nâng cấp thành công lên gói ").append(packageId).append(".\n\n");
        sb.append("Thông tin nâng cấp:\n");
        sb.append("- Mã tham chiếu: ").append(payment.getReferenceCode()).append("\n");
        sb.append("- Số tiền: ").append(formatAmount(payment.getAmount())).append(" ").append(payment.getCurrency()).append("\n");
        sb.append("- Thời gian: ").append(payment.getCompletedAt() != null ? 
            payment.getCompletedAt().format(DATE_FORMATTER) : "N/A").append("\n");
        
        sb.append("\nBạn có thể bắt đầu sử dụng các tính năng của gói mới ngay lập tức.\n");
        sb.append("Trân trọng,\n");
        sb.append("Chatbot SaaS Team");
        
        return sb.toString();
    }

    private String extractRefundReason(String description) {
        if (description == null) {
            return "Không có thông tin";
        }
        if (description.contains("[REFUNDED:")) {
            int start = description.indexOf("[REFUNDED:") + 10;
            int end = description.indexOf("]", start);
            if (end > start) {
                return description.substring(start, end);
            }
        }
        return "Không có thông tin";
    }

    private String formatAmount(BigDecimal amount) {
        return String.format("%,.0f", amount);
    }

    private void sendEmail(String to, String subject, String content) {
        // Email sending disabled - mail dependency not available
        // Log email content instead
        log.info("📧 [EMAIL MOCK] To: {}, Subject: {}, Content: {}", to, subject, content);
    }
}
