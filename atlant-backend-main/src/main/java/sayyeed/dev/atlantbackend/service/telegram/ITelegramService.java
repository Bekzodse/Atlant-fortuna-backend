package sayyeed.dev.atlantbackend.service.telegram;

public interface ITelegramService {
    void sendMessage(String text);

    // Sends directly to one chat ID and lets the caller see failures - used by the "test send" feature
    void sendTestMessage(String chatId);
}
