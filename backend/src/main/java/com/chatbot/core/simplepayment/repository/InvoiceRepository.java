package com.chatbot.core.simplepayment.repository;

import com.chatbot.core.simplepayment.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    Optional<Invoice> findByPaymentId(Long paymentId);
    
    List<Invoice> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Invoice> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    
    @Query("SELECT i FROM Invoice i WHERE i.userId = :userId AND i.createdAt >= :startDate")
    List<Invoice> findByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Invoice i WHERE i.invoiceNumber = :invoiceNumber")
    boolean existsByInvoiceNumber(@Param("invoiceNumber") String invoiceNumber);
    
    @Query("SELECT i FROM Invoice i WHERE i.status = :status ORDER BY i.createdAt DESC")
    List<Invoice> findByStatus(@Param("status") Invoice.InvoiceStatus status);
}
