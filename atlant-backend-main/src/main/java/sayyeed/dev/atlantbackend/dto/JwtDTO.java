package sayyeed.dev.atlantbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sayyeed.dev.atlantbackend.enums.UserRoleEnum;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JwtDTO {
    private String email;
    private String code;
    private List<UserRoleEnum> roles;
}
