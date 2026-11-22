package sayyeed.dev.atlantbackend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AuthRequestDTO {
    @NotBlank(message = "Name is required")
    private String name;
    @Email(message = "Email is required")
    private String email;
    @NotBlank(message = "Password is required")
    private String password;
}
