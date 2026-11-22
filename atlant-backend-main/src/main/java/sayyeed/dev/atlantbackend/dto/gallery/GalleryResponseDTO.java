package sayyeed.dev.atlantbackend.dto.gallery;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import sayyeed.dev.atlantbackend.dto.AttachDTO;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GalleryResponseDTO {
    private String id;
    private AttachDTO attach;
    private LocalDateTime createdAt;
}
