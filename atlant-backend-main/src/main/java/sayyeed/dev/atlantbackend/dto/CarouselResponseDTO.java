package sayyeed.dev.atlantbackend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CarouselResponseDTO {

    private String id;
    private String attachId;
    private String imageUrl;
    private Boolean active;
    private LocalDateTime createdAt;
}
