package sayyeed.dev.atlantbackend.repositories.user;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import sayyeed.dev.atlantbackend.entities.users.UserRoleEntity;
import sayyeed.dev.atlantbackend.enums.UserRoleEnum;

import java.util.List;

public interface UserRoleRepository extends CrudRepository<UserRoleEntity, String> {

    @Query("select role from UserRoleEntity where userId =?1")
    List<UserRoleEnum> getRolesByUserId(String userId);

}
