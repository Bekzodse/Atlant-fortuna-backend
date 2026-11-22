package sayyeed.dev.atlantbackend.dto.license;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import sayyeed.dev.atlantbackend.dto.AttachDTO;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LicenseResponseDTO {

    private String id;

    // Language-specific title (for public endpoints)
    private String title;

    // All titles (for admin endpoints)
    private String titleEn;
    private String titleRu;
    private String titleUz;

    private AttachDTO image;

    private LocalDateTime createdAt;
}
