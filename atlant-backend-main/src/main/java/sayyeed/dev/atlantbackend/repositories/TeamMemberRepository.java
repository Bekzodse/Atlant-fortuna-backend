package sayyeed.dev.atlantbackend.repositories;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import sayyeed.dev.atlantbackend.entities.TeamMembersEntity;
import sayyeed.dev.atlantbackend.mapper.TeamMemberMapper;

import java.util.List;

public interface TeamMemberRepository extends CrudRepository<TeamMembersEntity, String> {
    @Query("SELECT p.id AS id, " +
            "CASE :lang " +
            "   WHEN 'uz' THEN p.nameUz " +
            "   WHEN 'ru' THEN p.nameRu " +
            "   WHEN 'en' THEN p.nameEn " +
            "   ELSE p.nameEn " +
            "END AS name, " +
            "CASE :lang " +
            "   WHEN 'uz' THEN p.roleUz " +
            "   WHEN 'ru' THEN p.roleRu " +
            "   WHEN 'en' THEN p.roleEn " +
            "   ELSE p.roleEn " +
            "END AS role, " +
            "p.phoneNumber AS phoneNumber, " +
            "p.attachId AS imageId " +
            "FROM TeamMembersEntity p ")
    List<TeamMemberMapper> getAllByLang(@Param("lang") String lang);
}
