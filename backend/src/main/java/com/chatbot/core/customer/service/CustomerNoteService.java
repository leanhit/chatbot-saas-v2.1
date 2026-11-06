package com.chatbot.core.customer.service;

import com.chatbot.core.customer.model.CustomerNote;
import com.chatbot.core.customer.repository.CustomerNoteRepository;
import com.chatbot.core.customer.repository.CustomerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Customer notes service (Phase 4). Tenant-scoped CRUD for agent notes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerNoteService {

    private final CustomerNoteRepository noteRepository;
    private final CustomerProfileRepository customerProfileRepository;

    @Transactional(value = "appTransactionManager", rollbackFor = Exception.class)
    public CustomerNote createNote(Long customerProfileId, String content, String authorId, String authorName, Long tenantId) {
        if (!customerProfileRepository.existsByIdAndTenantId(customerProfileId, tenantId)) {
            throw new IllegalArgumentException("Customer profile not found for tenant: " + customerProfileId);
        }
        CustomerNote note = CustomerNote.builder()
            .customerProfileId(customerProfileId)
            .authorId(authorId)
            .authorName(authorName)
            .content(content)
            .build();
        note.setTenantId(tenantId);
        CustomerNote saved = noteRepository.save(note);
        log.info("Created note {} for customer profile {} tenant {}", saved.getId(), customerProfileId, tenantId);
        return saved;
    }

    @Transactional(value = "appTransactionManager", readOnly = true)
    public List<CustomerNote> getNotesForCustomer(Long customerProfileId, Long tenantId) {
        return noteRepository.findByTenantIdAndCustomerProfileIdAndIsDeletedFalseOrderByCreatedAtDesc(
            tenantId, customerProfileId);
    }

    @Transactional(value = "appTransactionManager", rollbackFor = Exception.class)
    public void deleteNote(Long noteId, Long tenantId) {
        CustomerNote note = noteRepository.findByIdAndTenantIdAndIsDeletedFalse(noteId, tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Note not found: " + noteId));
        note.setIsDeleted(true);
        noteRepository.save(note);
        log.info("Soft-deleted note {} for tenant {}", noteId, tenantId);
    }
}
