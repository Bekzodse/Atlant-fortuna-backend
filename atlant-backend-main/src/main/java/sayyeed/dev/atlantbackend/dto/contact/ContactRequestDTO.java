package sayyeed.dev.atlantbackend.dto.contact;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactRequestDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name is too long")
    private String name;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+[1-9]\\d{6,14}$", message = "Phone number must be in E.164 format, e.g. +998901234567")
    private String phone;

    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message is too long")
    private String message;

    @Pattern(regexp = "^(GENERAL|PURCHASE)$", message = "Type must be GENERAL or PURCHASE")
    private String type = "GENERAL";
}
