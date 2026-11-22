package sayyeed.dev.atlantbackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sayyeed.dev.atlantbackend.dto.contact.ContactRequestDTO;
import sayyeed.dev.atlantbackend.dto.contact.ContactResponseDTO;
import sayyeed.dev.atlantbackend.exceptions.AppBadException;
import sayyeed.dev.atlantbackend.service.ContactService;
import sayyeed.dev.atlantbackend.utils.RateLimiter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contact")
@AllArgsConstructor
public class ContactController {

    private final ContactService contactService;
    private final RateLimiter rateLimiter;

    @PostMapping("/public")
    public ResponseEntity<String> submit(@Valid @RequestBody ContactRequestDTO dto,
                                          @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale,
                                          HttpServletRequest request) {
        String clientIp = extractClientIp(request);
        if (!rateLimiter.allow(clientIp)) {
            throw new AppBadException("Too many requests, please try again later");
        }

        contactService.submit(dto, locale);
        return ResponseEntity.ok("Request submitted successfully");
    }

    @GetMapping("/admin")
    public ResponseEntity<List<ContactResponseDTO>> getAll() {
        return ResponseEntity.ok(contactService.getAll());
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
