package com.chatbot.core.customer.repository;

import com.chatbot.core.customer.model.CustomerNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerNoteRepository extends JpaRepository<CustomerNote, Long> {

    List<CustomerNote> findByTenantIdAndCustomerProfileIdAndIsDeletedFalseOrderByCreatedAtDesc(
        Long tenantId, Long customerProfileId);

    Optional<CustomerNote> findByIdAndTenantIdAndIsDeletedFalse(Long id, Long tenantId);
}
