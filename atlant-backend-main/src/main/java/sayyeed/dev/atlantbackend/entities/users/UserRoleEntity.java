package sayyeed.dev.atlantbackend.entities.users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import sayyeed.dev.atlantbackend.enums.UserRoleEnum;

@Entity
@Table(name = "user_role")
@Getter
@Setter
public class UserRoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id")
    private String userId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column
    private UserRoleEnum role;
}
