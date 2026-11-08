package com.chatbot.core.customer.service;

import com.chatbot.core.customer.model.CustomerNote;
import com.chatbot.core.customer.repository.CustomerNoteRepository;
import com.chatbot.core.customer.repository.CustomerProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerNoteServiceTest {

    @Mock private CustomerNoteRepository noteRepository;
    @Mock private CustomerProfileRepository customerProfileRepository;

    @InjectMocks
    private CustomerNoteService service;

    @Test
    void createNoteSavesWithTenantId() {
        when(customerProfileRepository.existsByIdAndTenantId(1L, 1L)).thenReturn(true);
        when(noteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CustomerNote saved = service.createNote(1L, "VIP customer", "agent-1", "Agent A", 1L);

        assertEquals(1L, saved.getCustomerProfileId());
        assertEquals("VIP customer", saved.getContent());
        assertEquals("agent-1", saved.getAuthorId());
        assertEquals(1L, saved.getTenantId());
    }

    @Test
    void createNoteRejectsMissingProfile() {
        when(customerProfileRepository.existsByIdAndTenantId(99L, 1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
            () -> service.createNote(99L, "note", "a", "b", 1L));
        verify(noteRepository, never()).save(any());
    }

    @Test
    void getNotesDelegatesToRepository() {
        CustomerNote n = new CustomerNote();
        n.setId(1L);
        when(noteRepository.findByTenantIdAndCustomerProfileIdAndIsDeletedFalseOrderByCreatedAtDesc(1L, 5L))
            .thenReturn(List.of(n));

        assertEquals(1, service.getNotesForCustomer(5L, 1L).size());
    }

    @Test
    void deleteNoteSoftDeletes() {
        CustomerNote n = new CustomerNote();
        n.setId(7L);
        n.setIsDeleted(false);
        when(noteRepository.findByIdAndTenantIdAndIsDeletedFalse(7L, 1L)).thenReturn(Optional.of(n));
        when(noteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.deleteNote(7L, 1L);

        assertEquals(Boolean.TRUE, n.getIsDeleted());
    }

    @Test
    void deleteNoteRejectsOtherTenant() {
        when(noteRepository.findByIdAndTenantIdAndIsDeletedFalse(7L, 2L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.deleteNote(7L, 2L));
        verify(noteRepository, never()).save(any());
    }
}
