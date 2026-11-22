package sayyeed.dev.atlantbackend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sayyeed.dev.atlantbackend.dto.contact.ContactRequestDTO;
import sayyeed.dev.atlantbackend.dto.contact.ContactResponseDTO;
import sayyeed.dev.atlantbackend.entities.ContactRequestEntity;
import sayyeed.dev.atlantbackend.repositories.ContactRequestRepository;
import sayyeed.dev.atlantbackend.service.telegram.ITelegramService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {
    @Mock private ContactRequestRepository contactRequestRepository;
    @Mock private ITelegramService telegramService;
    @InjectMocks private ContactService contactService;

    private ContactRequestDTO validDto() {
        ContactRequestDTO dto = new ContactRequestDTO();
        dto.setName("Aziz");
        dto.setPhone("+998901234567");
        dto.setMessage("Interested in RO-500");
        return dto;
    }

    @Test
    void submit_savesEntityAndSendsTelegramMessage() {
        when(contactRequestRepository.save(any(ContactRequestEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ContactResponseDTO response = contactService.submit(validDto(), "uz");

        verify(contactRequestRepository).save(any(ContactRequestEntity.class));
        verify(telegramService).sendMessage(contains("Aziz"));
        assertEquals("Aziz", response.getName());
    }

    @Test
    void submit_whenTelegramFails_stillSucceedsAndEntityIsSaved() {
        when(contactRequestRepository.save(any(ContactRequestEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("Telegram down")).when(telegramService).sendMessage(any());

        assertDoesNotThrow(() -> contactService.submit(validDto(), "uz"));
        verify(contactRequestRepository).save(any(ContactRequestEntity.class));
    }

    @Test
    void submit_whenRepositorySaveFails_propagatesAndSkipsTelegram() {
        when(contactRequestRepository.save(any(ContactRequestEntity.class)))
                .thenThrow(new RuntimeException("DB down"));

        assertThrows(RuntimeException.class, () -> contactService.submit(validDto(), "uz"));
        verify(telegramService, never()).sendMessage(any());
    }

    @Test
    void getAll_returnsSavedRequestsOrderedNewestFirst() {
        ContactRequestEntity entity = new ContactRequestEntity();
        entity.setName("Aziz");
        when(contactRequestRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(entity));

        List<ContactResponseDTO> result = contactService.getAll();

        assertEquals(1, result.size());
        assertEquals("Aziz", result.get(0).getName());
    }
}
