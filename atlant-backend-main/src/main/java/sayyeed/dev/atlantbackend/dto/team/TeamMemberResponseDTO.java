package sayyeed.dev.atlantbackend.dto.team;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import sayyeed.dev.atlantbackend.dto.AttachDTO;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeamMemberResponseDTO {
    private String id;
    private String name;
    private String nameEn;
    private String nameRu;
    private String nameUz;
    private String role;
    private String roleEn;
    private String roleRu;
    private String roleUz;
    private String phoneNumber;
    private AttachDTO image;
}
