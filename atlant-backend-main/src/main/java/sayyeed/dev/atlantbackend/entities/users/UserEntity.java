package sayyeed.dev.atlantbackend.entities.users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column
    private String name;

    @Column
    private String email;

    @Column
    private String password;

    @OneToMany(mappedBy = "user")
    private List<UserRoleEntity> roleList;
}
