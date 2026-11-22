package sayyeed.dev.atlantbackend.dto.contact;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContactResponseDTO {
    private String id;
    private String name;
    private String phone;
    private String message;
    private String locale;
    private String type;
    private LocalDateTime createdAt;
}
