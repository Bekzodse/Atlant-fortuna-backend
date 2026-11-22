package sayyeed.dev.atlantbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sayyeed.dev.atlantbackend.dto.telegram.TelegramBotRequestDTO;
import sayyeed.dev.atlantbackend.dto.telegram.TelegramBotResponseDTO;
import sayyeed.dev.atlantbackend.entities.TelegramBotEntity;
import sayyeed.dev.atlantbackend.exceptions.AppBadException;
import sayyeed.dev.atlantbackend.repositories.TelegramBotRepository;

import java.util.Comparator;
import java.util.List;

@Service
public class TelegramBotService {

    @Autowired
    private TelegramBotRepository telegramBotRepository;

    // Fallback used only until an admin adds the first bot in the dashboard
    @Value("${telegram.bot.token:}")
    private String envBotToken;

    public TelegramBotResponseDTO create(TelegramBotRequestDTO requestDTO) {
        TelegramBotEntity bot = new TelegramBotEntity();
        bot.setLabel(requestDTO.getLabel());
        bot.setToken(requestDTO.getToken());
        // First bot ever added becomes active automatically; otherwise admin picks via "activate"
        bot.setActive(!telegramBotRepository.findAllByOrderByCreatedAtDesc().iterator().hasNext());

        telegramBotRepository.save(bot);
        return toResponseDTO(bot);
    }

    public List<TelegramBotResponseDTO> getAll() {
        return telegramBotRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // @Transactional makes the deactivate-others + activate-target pair atomic, so a
    // concurrent "set active" on another bot can't race in and leave two bots active.
    @Transactional
    public TelegramBotResponseDTO activate(String id) {
        TelegramBotEntity toActivate = findOrThrow(id);

        telegramBotRepository.findAllByActiveTrue().forEach(bot -> {
            if (!bot.getId().equals(id)) {
                bot.setActive(false);
                telegramBotRepository.save(bot);
            }
        });

        toActivate.setActive(true);
        telegramBotRepository.save(toActivate);
        return toResponseDTO(toActivate);
    }

    @Transactional
    public void delete(String id) {
        TelegramBotEntity bot = findOrThrow(id);
        boolean wasActive = bot.getActive();
        telegramBotRepository.delete(bot);

        if (wasActive) {
            // Promote the most recently added remaining bot so sending doesn't silently stop
            telegramBotRepository.findAllByOrderByCreatedAtDesc().stream()
                    .findFirst()
                    .ifPresent(next -> {
                        next.setActive(true);
                        telegramBotRepository.save(next);
                    });
        }
    }

    // Resolves the token currently used to send messages. Tolerates more than one bot
    // somehow being marked active (e.g. a direct DB edit) by deterministically picking
    // the most recently created one instead of throwing.
    public String resolveBotToken() {
        List<TelegramBotEntity> activeBots = telegramBotRepository.findAllByActiveTrue();
        return activeBots.stream()
                .max(Comparator.comparing(TelegramBotEntity::getCreatedAt))
                .map(TelegramBotEntity::getToken)
                .orElse(envBotToken);
    }

    private TelegramBotEntity findOrThrow(String id) {
        return telegramBotRepository.findById(id)
                .orElseThrow(() -> new AppBadException("Telegram bot not found"));
    }

    private TelegramBotResponseDTO toResponseDTO(TelegramBotEntity bot) {
        TelegramBotResponseDTO response = new TelegramBotResponseDTO();
        response.setId(bot.getId());
        response.setLabel(bot.getLabel());
        response.setMaskedToken(mask(bot.getToken()));
        response.setActive(bot.getActive());
        response.setCreatedAt(bot.getCreatedAt());
        return response;
    }

    private String mask(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String tail = token.length() > 4 ? token.substring(token.length() - 4) : token;
        return "••••" + tail;
    }
}
