package sayyeed.dev.atlantbackend.dto.telegram;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TelegramBotResponseDTO {

    private String id;
    private String label;

    // Masked preview, e.g. "••••3456" - the real token is never returned
    private String maskedToken;

    private boolean active;
    private LocalDateTime createdAt;
}
