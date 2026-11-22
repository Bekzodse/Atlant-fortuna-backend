package sayyeed.dev.atlantbackend.dto.license;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LicenseRequestDTO {

    @NotBlank(message = "Title in English is required")
    private String titleEn;

    @NotBlank(message = "Title in Russian is required")
    private String titleRu;

    @NotBlank(message = "Title in Uzbek is required")
    private String titleUz;

    @NotBlank(message = "Image ID is required")
    private String imageId;
}
