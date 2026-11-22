package sayyeed.dev.atlantbackend.repositories.user;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import sayyeed.dev.atlantbackend.entities.users.UserEntity;

import java.util.Optional;

public interface UserRepository extends CrudRepository<UserEntity, String> {
    @Query("from UserEntity where email = ?1")
    Optional<UserEntity> getUserByEmail(String email);
}
