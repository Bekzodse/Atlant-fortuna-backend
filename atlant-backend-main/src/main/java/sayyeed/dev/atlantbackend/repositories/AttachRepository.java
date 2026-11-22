package sayyeed.dev.atlantbackend.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import sayyeed.dev.atlantbackend.entities.AttachEntity;

import java.util.Optional;

public interface AttachRepository extends CrudRepository<AttachEntity, String> {
    @Modifying
    @Transactional
    @Query("update AttachEntity set visible = false where id =?1")
    void setVisibleFalse(String id);

    @Query("from AttachEntity where visible = true and id =?1")
    Optional<AttachEntity> findById(String id);
}
