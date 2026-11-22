package sayyeed.dev.atlantbackend.service.telegram.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import sayyeed.dev.atlantbackend.entities.TelegramChatEntity;
import sayyeed.dev.atlantbackend.exceptions.AppBadException;
import sayyeed.dev.atlantbackend.service.TelegramChatService;
import sayyeed.dev.atlantbackend.service.TelegramBotService;
import sayyeed.dev.atlantbackend.service.telegram.ITelegramService;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TelegramServiceImpl implements ITelegramService {

    private static final Logger log = LoggerFactory.getLogger(TelegramServiceImpl.class);

    private final TelegramBotService telegramBotService;
    private final TelegramChatService telegramChatService;

    // Fallback used only when no chats have ever been configured in the dashboard yet
    @Value("${telegram.chat.id:}")
    private String envChatId;

    private final RestClient restClient = buildRestClient();

    private static RestClient buildRestClient() {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()
        );
        requestFactory.setReadTimeout(Duration.ofSeconds(15));
        return RestClient.builder().requestFactory(requestFactory).build();
    }

    // The JVM's first HTTPS call (DNS resolution + TLS handshake + classloading) is
    // noticeably slower than subsequent ones. Without this, a real contact-form
    // submission arriving right after a fresh deploy can trip the timeouts above
    // before the connection pool ever warms up.
    @EventListener(ApplicationReadyEvent.class)
    public void warmUpHttpClient() {
        String botToken = telegramBotService.resolveBotToken();
        if (botToken == null || botToken.isBlank()) {
            return;
        }
        try {
            restClient.get()
                    .uri("https://api.telegram.org/bot" + botToken + "/getMe")
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Telegram warm-up call failed; first real send may be slow", e);
        }
    }

    @Override
    public void sendMessage(String text) {
        String botToken = telegramBotService.resolveBotToken();
        if (botToken == null || botToken.isBlank()) {
            log.warn("Telegram bot token is not configured; skipping notification");
            return;
        }

        List<TelegramChatEntity> chats = telegramChatService.getAllActive();
        List<String> chatIds = chats.isEmpty()
                ? (envChatId == null || envChatId.isBlank() ? List.of() : List.of(envChatId))
                : chats.stream().map(TelegramChatEntity::getGroupId).toList();

        for (String chatId : chatIds) {
            try {
                send(botToken, chatId, text);
            } catch (Exception e) {
                log.error("Failed to send Telegram message to chat {}", chatId, e);
            }
        }
    }

    @Override
    public void sendTestMessage(String chatId) {
        String botToken = telegramBotService.resolveBotToken();
        if (botToken == null || botToken.isBlank()) {
            throw new AppBadException("Telegram bot token is not configured");
        }
        send(botToken, chatId, "✅ Test message from Atlant admin dashboard");
    }

    private void send(String botToken, String chatId, String text) {
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        // The JDK HttpClient's pooled connection can go stale after the host machine
        // sleeps/wakes (common on dev laptops); the first request on it then hangs
        // until timeout. One retry opens a fresh connection and reliably recovers.
        try {
            doSend(url, chatId, text);
        } catch (Exception e) {
            log.warn("Telegram send failed, retrying once for chat {}", chatId, e);
            doSend(url, chatId, text);
        }
    }

    private void doSend(String url, String chatId, String text) {
        restClient
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("chat_id", chatId, "text", text))
                .retrieve()
                .toBodilessEntity();
    }
}
