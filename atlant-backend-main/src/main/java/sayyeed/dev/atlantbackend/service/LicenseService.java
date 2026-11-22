package sayyeed.dev.atlantbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sayyeed.dev.atlantbackend.dto.AttachDTO;
import sayyeed.dev.atlantbackend.dto.license.LicenseRequestDTO;
import sayyeed.dev.atlantbackend.dto.license.LicenseResponseDTO;
import sayyeed.dev.atlantbackend.entities.AttachEntity;
import sayyeed.dev.atlantbackend.entities.LicenseEntity;
import sayyeed.dev.atlantbackend.exceptions.AppBadException;
import sayyeed.dev.atlantbackend.mapper.LicenseMapper;
import sayyeed.dev.atlantbackend.repositories.AttachRepository;
import sayyeed.dev.atlantbackend.repositories.LicenseRepository;
import sayyeed.dev.atlantbackend.service.aws.IS3Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LicenseService {

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private AttachRepository attachRepository;

    @Autowired
    private IS3Service s3Service;

    public String create(LicenseRequestDTO requestDTO) {
        // Validate that the image exists
        validateImageExists(requestDTO.getImageId());

        // Create license entity
        LicenseEntity license = new LicenseEntity();
        license.setTitleEn(requestDTO.getTitleEn());
        license.setTitleRu(requestDTO.getTitleRu());
        license.setTitleUz(requestDTO.getTitleUz());
        license.setAttachId(requestDTO.getImageId());

        licenseRepository.save(license);

        return license.getId();
    }

    public List<LicenseResponseDTO> getAll(String lang) {
        List<LicenseMapper> licenses = licenseRepository.getAllByLang(lang);
        return licenses.stream()
                .map(license -> {
                    AttachDTO attachDTO = s3Service.getImageUrlById(license.getAttachId());
                    return toResponseDTO(license, attachDTO, lang);
                })
                .collect(Collectors.toList());
    }

    public List<LicenseResponseDTO> getLastN(int n, String lang) {
        Pageable pageable = PageRequest.of(0, n);
        List<LicenseMapper> licenses = licenseRepository.getAllByLang(lang, pageable);
        return licenses.stream()
                .map(license -> {
                    AttachDTO attachDTO = s3Service.getImageUrlById(license.getAttachId());
                    return toResponseDTO(license, attachDTO, lang);
                })
                .collect(Collectors.toList());
    }

    public LicenseResponseDTO getById(String id, String lang) {
        LicenseEntity license = licenseRepository.findById(id)
                .orElseThrow(() -> new AppBadException("License not found"));

        AttachDTO attachDTO = s3Service.getImageUrlById(license.getAttachId());

        LicenseResponseDTO responseDTO = new LicenseResponseDTO();
        responseDTO.setId(license.getId());

        // Set language-specific title
        switch (lang) {
            case "uz" -> responseDTO.setTitle(license.getTitleUz());
            case "ru" -> responseDTO.setTitle(license.getTitleRu());
            default -> responseDTO.setTitle(license.getTitleEn());
        }

        responseDTO.setImage(attachDTO);
        responseDTO.setCreatedAt(license.getCreatedAt());

        return responseDTO;
    }

    public void delete(String id) {
        LicenseEntity license = licenseRepository.findById(id)
                .orElseThrow(() -> new AppBadException("License not found"));

        // Delete from S3
        s3Service.delete(license.getAttachId());

        // Delete license record
        licenseRepository.delete(license);
    }

    private void validateImageExists(String imageId) {
        AttachEntity attachment = attachRepository.findById(imageId)
                .orElseThrow(() -> new AppBadException("Image with ID " + imageId + " not found"));

        if (attachment.getVisible() != null && !attachment.getVisible()) {
            throw new AppBadException("Image is not visible");
        }
    }

    private LicenseResponseDTO toResponseDTO(LicenseMapper license, AttachDTO attachDTO, String lang) {
        LicenseResponseDTO responseDTO = new LicenseResponseDTO();
        responseDTO.setId(license.getId());
        responseDTO.setTitle(license.getTitle());
        responseDTO.setImage(attachDTO);
        return responseDTO;
    }
}
