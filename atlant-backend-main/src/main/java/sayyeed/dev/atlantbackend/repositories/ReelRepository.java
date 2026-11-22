package sayyeed.dev.atlantbackend.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import sayyeed.dev.atlantbackend.entities.ReelEntity;

import java.util.List;

public interface ReelRepository extends CrudRepository<ReelEntity, String> {
    List<ReelEntity> findAllByOrderByCreatedAtDesc();
    List<ReelEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
