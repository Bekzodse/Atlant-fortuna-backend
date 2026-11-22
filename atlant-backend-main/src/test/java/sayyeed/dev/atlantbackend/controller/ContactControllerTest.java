package sayyeed.dev.atlantbackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import sayyeed.dev.atlantbackend.dto.contact.ContactRequestDTO;
import sayyeed.dev.atlantbackend.exceptions.AppBadException;
import sayyeed.dev.atlantbackend.service.ContactService;
import sayyeed.dev.atlantbackend.utils.RateLimiter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactControllerTest {
    @Mock private ContactService contactService;
    @Mock private RateLimiter rateLimiter;
    @Mock private HttpServletRequest request;
    @InjectMocks private ContactController contactController;

    private ContactRequestDTO validDto() {
        ContactRequestDTO dto = new ContactRequestDTO();
        dto.setName("Aziz");
        dto.setPhone("+998901234567");
        dto.setMessage("Hi");
        return dto;
    }

    @Test
    void submit_whenRateLimited_throwsAppBadExceptionAndSkipsService() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("9.9.9.9");
        when(rateLimiter.allow("9.9.9.9")).thenReturn(false);

        assertThrows(AppBadException.class,
                () -> contactController.submit(validDto(), "ru", request));
        verify(contactService, never()).submit(any(), any());
    }

    @Test
    void submit_whenAllowed_callsServiceAndReturnsOk() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("9.9.9.9");
        when(rateLimiter.allow("9.9.9.9")).thenReturn(true);

        ContactRequestDTO dto = validDto();
        ResponseEntity<String> response = contactController.submit(dto, "ru", request);

        verify(contactService).submit(eq(dto), eq("ru"));
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void submit_prefersXForwardedForOverRemoteAddr() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("1.1.1.1, 2.2.2.2");
        when(rateLimiter.allow("1.1.1.1")).thenReturn(true);

        contactController.submit(validDto(), "ru", request);

        verify(rateLimiter).allow("1.1.1.1");
    }
}
