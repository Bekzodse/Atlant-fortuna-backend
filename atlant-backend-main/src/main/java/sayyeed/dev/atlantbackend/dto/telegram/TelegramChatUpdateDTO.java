package sayyeed.dev.atlantbackend.dto.telegram;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TelegramChatUpdateDTO {

    private String label;

    private String groupId;

    private Boolean active;
}
