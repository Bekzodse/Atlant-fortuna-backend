package sayyeed.dev.atlantbackend.repositories;

import org.springframework.data.repository.CrudRepository;
import sayyeed.dev.atlantbackend.entities.ContactRequestEntity;

import java.util.List;

public interface ContactRequestRepository extends CrudRepository<ContactRequestEntity, String> {
    List<ContactRequestEntity> findAllByOrderByCreatedAtDesc();
}
