package sayyeed.dev.atlantbackend.dto.team;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class TeamMemberRequestDTO {
    @NotBlank(message = "Name in English is required")
    private String nameEn;
    @NotBlank(message = "Name in Russian is required")
    private String nameRu;
    @NotBlank(message = "Name in Uzbek is required")
    private String nameUz;
    @NotBlank(message = "Role in English is required")
    private String roleEn;
    @NotBlank(message = "Role in Russian is required")
    private String roleRu;
    @NotBlank(message = "Role in Uzbek is required")
    private String roleUz;
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+[1-9]\\d{6,14}$", message = "Phone number must be in E.164 format, e.g. +998901234567")
    private String phoneNumber;
    @NotBlank(message = "Image Id required")
    private String imageId;
}
