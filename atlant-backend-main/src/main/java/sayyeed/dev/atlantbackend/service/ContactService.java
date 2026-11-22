package sayyeed.dev.atlantbackend.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sayyeed.dev.atlantbackend.dto.contact.ContactRequestDTO;
import sayyeed.dev.atlantbackend.dto.contact.ContactResponseDTO;
import sayyeed.dev.atlantbackend.entities.ContactRequestEntity;
import sayyeed.dev.atlantbackend.repositories.ContactRequestRepository;
import sayyeed.dev.atlantbackend.service.telegram.ITelegramService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactService {

    private static final Logger log = LoggerFactory.getLogger(ContactService.class);

    private final ContactRequestRepository contactRequestRepository;
    private final ITelegramService telegramService;

    public ContactResponseDTO submit(ContactRequestDTO dto, String locale) {
        ContactRequestEntity entity = new ContactRequestEntity();
        entity.setName(dto.getName());
        entity.setPhone(dto.getPhone());
        entity.setMessage(dto.getMessage());
        entity.setLocale(locale);
        entity.setType(dto.getType() == null || dto.getType().isBlank() ? "GENERAL" : dto.getType());

        ContactRequestEntity saved = contactRequestRepository.save(entity);

        try {
            telegramService.sendMessage(formatTelegramMessage(dto, locale));
        } catch (Exception e) {
            log.error("Failed to send Telegram notification for contact request {}", saved.getId(), e);
        }

        return toResponseDTO(saved);
    }

    public List<ContactResponseDTO> getAll() {
        return contactRequestRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private String formatTelegramMessage(ContactRequestDTO dto, String locale) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String title = "PURCHASE".equals(dto.getType())
                ? "🛒 Хочу купить"
                : "🆕 Новая заявка с сайта";
        return """
                %s

                👤 Имя: %s
                📞 Телефон: %s
                💬 Сообщение: %s
                🌐 Страница: %s
                🕒 Время: %s""".formatted(title, dto.getName(), dto.getPhone(), dto.getMessage(), locale, timestamp);
    }

    private ContactResponseDTO toResponseDTO(ContactRequestEntity entity) {
        ContactResponseDTO response = new ContactResponseDTO();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setPhone(entity.getPhone());
        response.setMessage(entity.getMessage());
        response.setLocale(entity.getLocale());
        response.setType(entity.getType());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }
}
