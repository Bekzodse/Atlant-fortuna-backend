package sayyeed.dev.atlantbackend.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import sayyeed.dev.atlantbackend.entities.GalleryEntity;

import java.util.List;

public interface GalleryRepository extends CrudRepository<GalleryEntity, String> {

    List<GalleryEntity> findAllByOrderByCreatedAtDesc();

    List<GalleryEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
