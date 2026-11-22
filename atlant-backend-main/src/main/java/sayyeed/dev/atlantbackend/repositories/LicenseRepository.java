package sayyeed.dev.atlantbackend.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import sayyeed.dev.atlantbackend.entities.LicenseEntity;
import sayyeed.dev.atlantbackend.mapper.LicenseMapper;

import java.util.List;

public interface LicenseRepository extends CrudRepository<LicenseEntity, String> {

    List<LicenseEntity> findAllByOrderByCreatedAtDesc();

    List<LicenseEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT l.id AS id, l.attachId AS attachId, " +
            "CASE :lang " +
            "   WHEN 'uz' THEN l.titleUz " +
            "   WHEN 'ru' THEN l.titleRu " +
            "   WHEN 'en' THEN l.titleEn " +
            "   ELSE l.titleEn " +
            "END AS title " +
            "FROM LicenseEntity l " +
            "ORDER BY l.createdAt DESC")
    List<LicenseMapper> getAllByLang(@Param("lang") String lang);

    @Query("SELECT l.id AS id, l.attachId AS attachId, " +
            "CASE :lang " +
            "   WHEN 'uz' THEN l.titleUz " +
            "   WHEN 'ru' THEN l.titleRu " +
            "   WHEN 'en' THEN l.titleEn " +
            "   ELSE l.titleEn " +
            "END AS title " +
            "FROM LicenseEntity l " +
            "ORDER BY l.createdAt DESC")
    List<LicenseMapper> getAllByLang(@Param("lang") String lang, Pageable pageable);
}
