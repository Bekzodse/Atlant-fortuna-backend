package sayyeed.dev.atlantbackend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "team_members")
@Getter
@Setter
public class TeamMembersEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column
    private String nameEn;

    @Column
    private String nameRu;

    @Column
    private String nameUz;

    @Column
    private String roleEn;

    @Column
    private String roleRu;

    @Column
    private String roleUz;

    @Column
    private String phoneNumber;

    @Column(name = "attach_id", nullable = false)
    private String attachId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attach_id", insertable = false, updatable = false)
    private AttachEntity attachment;

}
