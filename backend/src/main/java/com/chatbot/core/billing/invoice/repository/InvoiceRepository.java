package com.chatbot.core.billing.invoice.repository;

import com.chatbot.core.billing.invoice.model.Invoice;
import com.chatbot.core.billing.invoice.model.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByTenantId(Long tenantId);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByBillingAccountId(Long billingAccountId);

    List<Invoice> findBySubscriptionId(Long subscriptionId);

    List<Invoice> findByStatus(InvoiceStatus status);

    Page<Invoice> findByStatusIn(List<InvoiceStatus> statuses, Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.tenantId = :tenantId ORDER BY i.invoiceDate DESC")
    Page<Invoice> findByTenantIdOrderByInvoiceDateDesc(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.tenantId = :tenantId AND i.status IN :statuses ORDER BY i.invoiceDate DESC")
    Page<Invoice> findByTenantIdAndStatusIn(@Param("tenantId") Long tenantId, 
                                            @Param("statuses") List<InvoiceStatus> statuses, 
                                            Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :now AND i.status NOT IN :paidStatuses")
    List<Invoice> findOverdueInvoices(@Param("now") LocalDateTime now, 
                                     @Param("paidStatuses") List<InvoiceStatus> paidStatuses);

    @Query("SELECT i FROM Invoice i WHERE i.dueDate BETWEEN :start AND :end AND i.status NOT IN :paidStatuses")
    List<Invoice> findInvoicesDueBetween(@Param("start") LocalDateTime start, 
                                         @Param("end") LocalDateTime end,
                                         @Param("paidStatuses") List<InvoiceStatus> paidStatuses);

    @Query("SELECT i FROM Invoice i WHERE i.invoiceDate BETWEEN :start AND :end")
    List<Invoice> findInvoicesByDateRange(@Param("start") LocalDateTime start, 
                                          @Param("end") LocalDateTime end);

    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.status = :status AND i.tenantId = :tenantId")
    BigDecimal getTotalAmountByStatusAndTenant(@Param("status") InvoiceStatus status, 
                                              @Param("tenantId") Long tenantId);

    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.status = :status")
    BigDecimal getTotalAmountByStatus(@Param("status") InvoiceStatus status);

    @Query("SELECT SUM(i.totalAmount - i.paidAmount) FROM Invoice i WHERE i.status IN :statuses AND i.tenantId = :tenantId")
    BigDecimal getOutstandingAmountByTenant(@Param("statuses") List<InvoiceStatus> statuses, 
                                           @Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = :status AND i.tenantId = :tenantId")
    Long countInvoicesByStatusAndTenant(@Param("status") InvoiceStatus status, 
                                       @Param("tenantId") Long tenantId);

    @Query("SELECT i FROM Invoice i WHERE " +
           "(LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.notes) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Invoice> searchInvoices(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.tenantId = :tenantId AND " +
           "(LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.notes) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Invoice> searchInvoicesByTenant(@Param("tenantId") Long tenantId, 
                                        @Param("keyword") String keyword, 
                                        Pageable pageable);

    boolean existsByInvoiceNumber(String invoiceNumber);
}
