package sayyeed.dev.atlantbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sayyeed.dev.atlantbackend.exceptions.AppBadException;
import sayyeed.dev.atlantbackend.dto.telegram.TelegramChatRequestDTO;
import sayyeed.dev.atlantbackend.dto.telegram.TelegramChatResponseDTO;
import sayyeed.dev.atlantbackend.dto.telegram.TelegramChatUpdateDTO;
import sayyeed.dev.atlantbackend.dto.telegram.TelegramBotRequestDTO;
import sayyeed.dev.atlantbackend.dto.telegram.TelegramBotResponseDTO;
import sayyeed.dev.atlantbackend.service.TelegramChatService;
import sayyeed.dev.atlantbackend.service.TelegramBotService;
import sayyeed.dev.atlantbackend.service.telegram.ITelegramService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/telegram")
@RequiredArgsConstructor
public class TelegramController {

    private final TelegramBotService telegramBotService;
    private final TelegramChatService telegramChatService;
    private final ITelegramService telegramService;

    @GetMapping("/admin/bots")
    public ResponseEntity<List<TelegramBotResponseDTO>> getBots() {
        return ResponseEntity.ok(telegramBotService.getAll());
    }

    @PostMapping("/admin/bots")
    public ResponseEntity<TelegramBotResponseDTO> createBot(@Valid @RequestBody TelegramBotRequestDTO requestDTO) {
        return ResponseEntity.ok(telegramBotService.create(requestDTO));
    }

    @PatchMapping("/admin/bots/{id}/activate")
    public ResponseEntity<TelegramBotResponseDTO> activateBot(@PathVariable String id) {
        return ResponseEntity.ok(telegramBotService.activate(id));
    }

    @DeleteMapping("/admin/bots/{id}")
    public ResponseEntity<String> deleteBot(@PathVariable String id) {
        telegramBotService.delete(id);
        return ResponseEntity.ok("Telegram bot deleted successfully");
    }

    @GetMapping("/admin/chats")
    public ResponseEntity<List<TelegramChatResponseDTO>> getChats() {
        return ResponseEntity.ok(telegramChatService.getAll());
    }

    @PostMapping("/admin/chats")
    public ResponseEntity<TelegramChatResponseDTO> createChat(@Valid @RequestBody TelegramChatRequestDTO requestDTO) {
        return ResponseEntity.ok(telegramChatService.create(requestDTO));
    }

    @PatchMapping("/admin/chats/{id}")
    public ResponseEntity<TelegramChatResponseDTO> updateChat(@PathVariable String id, @RequestBody TelegramChatUpdateDTO updateDTO) {
        return ResponseEntity.ok(telegramChatService.update(id, updateDTO));
    }

    @DeleteMapping("/admin/chats/{id}")
    public ResponseEntity<String> deleteChat(@PathVariable String id) {
        telegramChatService.delete(id);
        return ResponseEntity.ok("Telegram chat deleted successfully");
    }

    @PostMapping("/admin/chats/{id}/test")
    public ResponseEntity<String> testChat(@PathVariable String id) {
        TelegramChatResponseDTO chat = telegramChatService.getAll().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new AppBadException("Telegram chat not found"));

        telegramService.sendTestMessage(chat.getGroupId());
        return ResponseEntity.ok("Test message sent");
    }
}
