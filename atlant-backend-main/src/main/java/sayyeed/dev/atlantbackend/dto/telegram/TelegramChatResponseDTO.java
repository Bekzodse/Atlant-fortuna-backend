package sayyeed.dev.atlantbackend.dto.telegram;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TelegramChatResponseDTO {

    private String id;
    private String label;
    private String groupId;
    private boolean active;
    private LocalDateTime createdAt;
}
