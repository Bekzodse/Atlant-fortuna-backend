package sayyeed.dev.atlantbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import sayyeed.dev.atlantbackend.dto.auth.AuthRequestDTO;
import sayyeed.dev.atlantbackend.dto.auth.LoginRequestDTO;
import sayyeed.dev.atlantbackend.dto.auth.LoginResponseDTO;
import sayyeed.dev.atlantbackend.entities.users.UserEntity;
import sayyeed.dev.atlantbackend.enums.UserRoleEnum;
import sayyeed.dev.atlantbackend.exceptions.AppBadException;
import sayyeed.dev.atlantbackend.repositories.user.UserRepository;
import sayyeed.dev.atlantbackend.utils.JwtUtil;

import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRoleService roleService;

    @Autowired
    private UserRoleService userRoleService;

    public String register(AuthRequestDTO dto) {
        Optional<UserEntity> userEntityOptional = repository.getUserByEmail(dto.getEmail());
        if (userEntityOptional.isPresent()) {
            throw new AppBadException("User already exist");
        }
        UserEntity entity = new UserEntity();
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));
        repository.save(entity);

        roleService.create(entity.getId(), UserRoleEnum.ROLE_USER);

        return "Success";
    }

    public LoginResponseDTO login(LoginRequestDTO dto) {
        Optional<UserEntity> userEntityOptional = repository.getUserByEmail(dto.getEmail());
        if (userEntityOptional.isEmpty()) {
            throw new AppBadException("Email or Password wrong");
        }
        UserEntity entity = userEntityOptional.get();
        boolean flag = bCryptPasswordEncoder.matches(dto.getPassword(), entity.getPassword());
        List<UserRoleEnum> userRoleEnums = userRoleService.getRolesById(entity.getId());
        if (!flag) {
            throw new AppBadException("Email or password wrong");
        }

        LoginResponseDTO responseDTO = new LoginResponseDTO();
        responseDTO.setToken(JwtUtil.encode(entity.getEmail(), roleService.getRolesById(entity.getId())));
        return responseDTO;
    }
}
