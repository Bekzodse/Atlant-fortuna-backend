package sayyeed.dev.atlantbackend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginRequestDTO {
    @NotBlank(message = "Email required")
    private String email;
    @NotBlank(message = "Password required")
    private String password;;
}
