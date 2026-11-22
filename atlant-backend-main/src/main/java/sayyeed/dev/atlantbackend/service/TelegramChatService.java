package sayyeed.dev.atlantbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sayyeed.dev.atlantbackend.dto.telegram.TelegramChatRequestDTO;
import sayyeed.dev.atlantbackend.dto.telegram.TelegramChatResponseDTO;
import sayyeed.dev.atlantbackend.dto.telegram.TelegramChatUpdateDTO;
import sayyeed.dev.atlantbackend.entities.TelegramChatEntity;
import sayyeed.dev.atlantbackend.exceptions.AppBadException;
import sayyeed.dev.atlantbackend.repositories.TelegramChatRepository;

import java.util.List;

@Service
public class TelegramChatService {

    @Autowired
    private TelegramChatRepository telegramChatRepository;

    public TelegramChatResponseDTO create(TelegramChatRequestDTO requestDTO) {
        TelegramChatEntity chat = new TelegramChatEntity();
        chat.setLabel(requestDTO.getLabel());
        chat.setGroupId(requestDTO.getGroupId());
        chat.setActive(true);

        telegramChatRepository.save(chat);
        return toResponseDTO(chat);
    }

    public List<TelegramChatResponseDTO> getAll() {
        return telegramChatRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<TelegramChatEntity> getAllActive() {
        return telegramChatRepository.findAllByActiveTrue();
    }

    public TelegramChatResponseDTO update(String id, TelegramChatUpdateDTO updateDTO) {
        TelegramChatEntity chat = findOrThrow(id);

        if (updateDTO.getLabel() != null) {
            chat.setLabel(updateDTO.getLabel());
        }
        if (updateDTO.getGroupId() != null) {
            chat.setGroupId(updateDTO.getGroupId());
        }
        if (updateDTO.getActive() != null) {
            chat.setActive(updateDTO.getActive());
        }

        telegramChatRepository.save(chat);
        return toResponseDTO(chat);
    }

    public void delete(String id) {
        TelegramChatEntity chat = findOrThrow(id);
        telegramChatRepository.delete(chat);
    }

    private TelegramChatEntity findOrThrow(String id) {
        return telegramChatRepository.findById(id)
                .orElseThrow(() -> new AppBadException("Telegram chat not found"));
    }

    private TelegramChatResponseDTO toResponseDTO(TelegramChatEntity chat) {
        TelegramChatResponseDTO response = new TelegramChatResponseDTO();
        response.setId(chat.getId());
        response.setLabel(chat.getLabel());
        response.setGroupId(chat.getGroupId());
        response.setActive(chat.getActive());
        response.setCreatedAt(chat.getCreatedAt());
        return response;
    }
}
