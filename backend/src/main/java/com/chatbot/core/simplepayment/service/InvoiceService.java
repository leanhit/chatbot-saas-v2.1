package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.model.Invoice;
import com.chatbot.core.simplepayment.model.SimplePayment;
import com.chatbot.core.simplepayment.repository.InvoiceRepository;
import com.chatbot.core.simplepayment.repository.SimplePaymentRepository;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final SimplePaymentRepository paymentRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter INVOICE_NUMBER_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    /**
     * Generate invoice for a completed payment
     */
    @Transactional("sharedTransactionManager")
    public Invoice generateInvoice(String referenceCode) {
        log.info("📄 Generating invoice for payment: {}", referenceCode);

        SimplePayment payment = paymentRepository.findByReferenceCode(referenceCode)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + referenceCode));

        // Check if invoice already exists
        if (invoiceRepository.findByPaymentId(payment.getId()).isPresent()) {
            log.warn("Invoice already exists for payment: {}", referenceCode);
            return invoiceRepository.findByPaymentId(payment.getId()).get();
        }

        // Get user information
        User user = userRepository.findById(payment.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + payment.getUserId()));

        // Generate invoice number
        String invoiceNumber = generateInvoiceNumber();

        // Create invoice
        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .paymentId(payment.getId())
                .userId(payment.getUserId())
                .tenantId(payment.getTenantId())
                .userEmail(user.getEmail())
                .userName(user.getEmail())
                .subtotal(payment.getAmount())
                .discountAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .totalAmount(payment.getAmount())
                .currency(payment.getCurrency())
                .packageId(payment.getTargetPackageId())
                .status(Invoice.InvoiceStatus.PAID)
                .paymentMethod("BANK_TRANSFER")
                .paymentReference(payment.getReferenceCode())
                .paidAt(payment.getCompletedAt())
                .build();

        Invoice saved = invoiceRepository.save(invoice);
        log.info("✅ Invoice generated: {} for payment: {}", invoiceNumber, referenceCode);
        return saved;
    }

    /**
     * Generate unique invoice number
     */
    private String generateInvoiceNumber() {
        YearMonth now = YearMonth.now();
        String prefix = "INV-" + now.format(INVOICE_NUMBER_FORMATTER) + "-";
        
        // Get count of invoices this month
        LocalDateTime startOfMonth = now.atDay(1).atStartOfDay();
        long count = invoiceRepository.count();
        
        // Generate sequential number
        String sequence = String.format("%06d", count + 1);
        return prefix + sequence;
    }

    /**
     * Get invoice by invoice number
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public Invoice getInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElse(null);
    }

    /**
     * Get invoice by payment ID
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public Invoice getInvoiceByPaymentId(Long paymentId) {
        return invoiceRepository.findByPaymentId(paymentId)
                .orElse(null);
    }

    /**
     * Get user invoices
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public List<Invoice> getUserInvoices(Long userId) {
        return invoiceRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get tenant invoices
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public List<Invoice> getTenantInvoices(Long tenantId) {
        return invoiceRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    /**
     * Update invoice status
     */
    @Transactional("sharedTransactionManager")
    public Invoice updateInvoiceStatus(Long invoiceId, Invoice.InvoiceStatus status) {
        log.info("🔄 Updating invoice status: {} to {}", invoiceId, status);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId));

        invoice.setStatus(status);
        
        if (status == Invoice.InvoiceStatus.PAID && invoice.getPaidAt() == null) {
            invoice.setPaidAt(LocalDateTime.now());
        }

        Invoice updated = invoiceRepository.save(invoice);
        log.info("✅ Invoice status updated: {} to {}", invoiceId, status);
        return updated;
    }

    /**
     * Get invoice statistics
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public String getInvoiceStatistics() {
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        
        List<Invoice> monthlyInvoices = invoiceRepository.findByUserIdAndDateRange(0L, startOfMonth);
        
        BigDecimal totalRevenue = monthlyInvoices.stream()
                .filter(i -> i.getStatus() == Invoice.InvoiceStatus.PAID)
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long paidCount = monthlyInvoices.stream()
                .filter(i -> i.getStatus() == Invoice.InvoiceStatus.PAID)
                .count();
        
        return String.format("Monthly Invoice Statistics: Total=%d, Paid=%d, Revenue=%,.0f VND",
                monthlyInvoices.size(), paidCount, totalRevenue);
    }
}
