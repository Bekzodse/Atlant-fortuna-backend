package sayyeed.dev.atlantbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sayyeed.dev.atlantbackend.entities.users.UserRoleEntity;
import sayyeed.dev.atlantbackend.enums.UserRoleEnum;
import sayyeed.dev.atlantbackend.repositories.user.UserRoleRepository;

import java.util.List;

@Service
public class UserRoleService {

    @Autowired
    private UserRoleRepository repository;

    public void create(String userId, UserRoleEnum roleEnum) {
        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setUserId(userId);
        userRoleEntity.setRole(roleEnum);
        repository.save(userRoleEntity);
    }

    public List<UserRoleEnum> getRolesById(String userId) {
        return repository.getRolesByUserId(userId);
    }

}
