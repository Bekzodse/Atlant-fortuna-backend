package sayyeed.dev.atlantbackend.dto.telegram;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TelegramBotRequestDTO {

    @NotBlank(message = "Label is required")
    private String label;

    @NotBlank(message = "Bot token is required")
    private String token;
}
