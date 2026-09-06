package com.chatbot.core.payment.invoice.service;

import com.chatbot.core.payment.common.audit.PaymentAuditLog.AuditAction;
import com.chatbot.core.payment.common.audit.PaymentAuditService;
import com.chatbot.core.payment.common.event.PaymentCompletedEvent;
import com.chatbot.core.payment.invoice.model.Invoice;
import com.chatbot.core.payment.invoice.model.Invoice.InvoiceStatus;
import com.chatbot.core.payment.invoice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentAuditService paymentAuditService;

    /**
     * Event listener for PaymentCompletedEvent
     * Automatically generates invoice after payment completion
     */
    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "sharedTransactionManager")
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        log.info("📄 [EVENT] Generating invoice for payment: {}", event.getReferenceCode());

        try {
            Invoice invoice = generateInvoice(
                event.getReferenceCode(),
                event.getUserId(),
                event.getTenantId(),
                event.getAmount(),
                event.getCurrency(),
                event.getTargetPackageId(),
                event.getCompletedAt()
            );

            log.info("✅ Invoice generated: {}", invoice.getInvoiceNumber());

            // Log audit
            paymentAuditService.logPaymentAction(
                event.getReferenceCode(),
                event.getUserId(),
                event.getTenantId(),
                AuditAction.PACKAGE_UPGRADED,
                null,
                null,
                event.getAmount(),
                "Invoice generated: " + invoice.getInvoiceNumber(),
                null
            );

        } catch (Exception e) {
            log.error("❌ Failed to generate invoice for payment: {}", event.getReferenceCode(), e);
        }
    }

    /**
     * Generate invoice for payment
     */
    @Transactional(transactionManager = "sharedTransactionManager")
    public Invoice generateInvoice(String referenceCode, Long userId, Long tenantId,
                                   BigDecimal amount, String currency, String packageId,
                                   LocalDateTime paymentDate) {
        log.info("📄 Generating invoice for reference: {}", referenceCode);

        // Generate invoice number
        String invoiceNumber = generateInvoiceNumber();

        // Placeholder user info - will be fetched from user service
        String userEmail = "user@example.com";
        String userName = "User Name";
        String packageName = packageId != null ? "Package: " + packageId : "Balance Credit";

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .paymentId(null) // Will be set after payment ID is available
                .userId(userId)
                .tenantId(tenantId)
                .userEmail(userEmail)
                .userName(userName)
                .subtotal(amount)
                .discountAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .totalAmount(amount)
                .currency(currency)
                .packageId(packageId)
                .packageName(packageName)
                .status(InvoiceStatus.PAID)
                .paymentMethod("BANK_TRANSFER")
                .paymentReference(referenceCode)
                .paidAt(paymentDate)
                .dueDate(paymentDate.plusDays(30))
                .build();

        return invoiceRepository.save(invoice);
    }

    /**
     * Get invoice by invoice number
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public Invoice getInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceNumber));
    }

    /**
     * Get invoices for user
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public java.util.List<Invoice> getUserInvoices(Long userId) {
        return invoiceRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get invoices for tenant
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public java.util.List<Invoice> getTenantInvoices(Long tenantId) {
        return invoiceRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    /**
     * Generate invoice number
     * Format: INV-YYYYMMDD-XXXXX
     */
    private String generateInvoiceNumber() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // Get count of invoices today
        long count = invoiceRepository.count();
        String sequence = String.format("%05d", count + 1);
        
        return "INV-" + datePrefix + "-" + sequence;
    }

    /**
     * Update invoice status
     */
    @Transactional(transactionManager = "sharedTransactionManager")
    public Invoice updateInvoiceStatus(String invoiceNumber, InvoiceStatus status) {
        log.info("📄 Updating invoice status: {} -> {}", invoiceNumber, status);

        Invoice invoice = getInvoiceByNumber(invoiceNumber);
        invoice.setStatus(status);
        
        if (status == InvoiceStatus.PAID) {
            invoice.setPaidAt(LocalDateTime.now());
        }

        return invoiceRepository.save(invoice);
    }

    /**
     * Delete invoice
     */
    @Transactional(transactionManager = "sharedTransactionManager")
    public void deleteInvoice(String invoiceNumber) {
        log.info("🗑️ Deleting invoice: {}", invoiceNumber);
        invoiceRepository.deleteByInvoiceNumber(invoiceNumber);
    }
}
