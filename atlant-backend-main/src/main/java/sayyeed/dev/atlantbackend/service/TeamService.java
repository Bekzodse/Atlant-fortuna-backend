package sayyeed.dev.atlantbackend.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import sayyeed.dev.atlantbackend.dto.team.TeamMemberRequestDTO;
import sayyeed.dev.atlantbackend.dto.team.TeamMemberResponseDTO;
import sayyeed.dev.atlantbackend.entities.AttachEntity;
import sayyeed.dev.atlantbackend.entities.TeamMembersEntity;
import sayyeed.dev.atlantbackend.exceptions.AppBadException;
import sayyeed.dev.atlantbackend.mapper.TeamMemberMapper;
import sayyeed.dev.atlantbackend.repositories.AttachRepository;
import sayyeed.dev.atlantbackend.repositories.TeamMemberRepository;
import sayyeed.dev.atlantbackend.service.aws.IS3Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TeamService {

    private final TeamMemberRepository repository;
    private final AttachRepository attachRepository;
    private final IS3Service attachService;

    public String addMember(TeamMemberRequestDTO dto) {

        Optional<AttachEntity> attachOptional = attachRepository.findById(dto.getImageId());
        if (attachOptional.isEmpty()) {
            throw new AppBadException("Image not found");
        }

        TeamMembersEntity entity = new TeamMembersEntity();
        entity.setNameEn(dto.getNameEn());
        entity.setNameRu(dto.getNameRu());
        entity.setNameUz(dto.getNameUz());
        entity.setRoleEn(dto.getRoleEn());
        entity.setRoleRu(dto.getRoleRu());
        entity.setRoleUz(dto.getRoleUz());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setAttachId(dto.getImageId());
        repository.save(entity);

        return entity.getId();
    }

    public List<TeamMemberResponseDTO> getAll(String lang) {
        List<TeamMemberMapper> mappers = repository.getAllByLang(lang);
        return mappers.stream()
                .map(mapper -> toDTO(mapper, mapper.getId()))
                .collect(Collectors.toList());
    }

    public List<TeamMemberResponseDTO> getAllForAdmin() {
        Iterable<TeamMembersEntity> entities =  repository.findAll();

        List<TeamMemberResponseDTO> responseList = new LinkedList<>();
        entities.forEach(entity -> responseList.add(toAdminDTO(entity)));
        return responseList;
    }

    public String update(String id, TeamMemberRequestDTO dto) {
        Optional<TeamMembersEntity> optionalMember = repository.findById(id);
        if (optionalMember.isEmpty()) {
            throw new AppBadException("Team member not found");
        }

        Optional<AttachEntity> attachOptional = attachRepository.findById(dto.getImageId());
        if (attachOptional.isEmpty()) {
            throw new AppBadException("Image not found");
        }

        TeamMembersEntity entity = optionalMember.get();
        entity.setNameEn(dto.getNameEn());
        entity.setNameRu(dto.getNameRu());
        entity.setNameUz(dto.getNameUz());
        entity.setRoleEn(dto.getRoleEn());
        entity.setRoleRu(dto.getRoleRu());
        entity.setRoleUz(dto.getRoleUz());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setAttachId(dto.getImageId());
        repository.save(entity);

        return entity.getId();
    }

    public void delete(String id) {
        Optional<TeamMembersEntity> optionalMember = repository.findById(id);
        if (optionalMember.isEmpty()) {
            throw new AppBadException("Team member not found");
        }

        repository.deleteById(id);
    }

    private TeamMemberResponseDTO toDTO(TeamMemberMapper mapper, String id) {
        TeamMemberResponseDTO responseDTO = new TeamMemberResponseDTO();
        responseDTO.setId(id);
        responseDTO.setName(mapper.getName());
        responseDTO.setRole(mapper.getRole());
        responseDTO.setPhoneNumber(mapper.getPhoneNumber());
        responseDTO.setImage(attachService.getImageUrlById(mapper.getImageId()));
        return responseDTO;
    }

    private TeamMemberResponseDTO toAdminDTO(TeamMembersEntity entity) {
        TeamMemberResponseDTO responseDTO = new TeamMemberResponseDTO();
        responseDTO.setId(entity.getId());
        responseDTO.setNameEn(entity.getNameEn());
        responseDTO.setNameRu(entity.getNameRu());
        responseDTO.setNameUz(entity.getNameUz());
        responseDTO.setRoleEn(entity.getRoleEn());
        responseDTO.setRoleRu(entity.getRoleRu());
        responseDTO.setRoleUz(entity.getRoleUz());
        responseDTO.setPhoneNumber(entity.getPhoneNumber());
        responseDTO.setImage(attachService.getImageUrlById(entity.getAttachId()));
        return responseDTO;
    }
}
