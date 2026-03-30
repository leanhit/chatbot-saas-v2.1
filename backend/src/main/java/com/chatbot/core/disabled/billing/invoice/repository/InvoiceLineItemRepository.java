package com.chatbot.core.billing.invoice.repository;

import com.chatbot.core.billing.invoice.model.InvoiceLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceLineItemRepository extends JpaRepository<InvoiceLineItem, Long> {

    List<InvoiceLineItem> findByInvoiceId(Long invoiceId);

    List<InvoiceLineItem> findBySubscriptionId(Long subscriptionId);

    @Query("SELECT ili FROM InvoiceLineItem ili WHERE ili.invoice.id = :invoiceId ORDER BY ili.itemNumber")
    List<InvoiceLineItem> findByInvoiceIdOrderByItemNumber(@Param("invoiceId") Long invoiceId);

    void deleteByInvoiceId(Long invoiceId);
}
