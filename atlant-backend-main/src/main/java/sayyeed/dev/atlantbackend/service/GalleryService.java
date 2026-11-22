package sayyeed.dev.atlantbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sayyeed.dev.atlantbackend.dto.AttachDTO;
import sayyeed.dev.atlantbackend.dto.gallery.GalleryResponseDTO;
import sayyeed.dev.atlantbackend.entities.AttachEntity;
import sayyeed.dev.atlantbackend.entities.GalleryEntity;
import sayyeed.dev.atlantbackend.exceptions.AppBadException;
import sayyeed.dev.atlantbackend.repositories.AttachRepository;
import sayyeed.dev.atlantbackend.repositories.GalleryRepository;
import sayyeed.dev.atlantbackend.service.aws.IS3Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GalleryService {

    @Autowired
    private GalleryRepository galleryRepository;

    @Autowired
    private AttachRepository attachRepository;

    @Autowired
    private IS3Service s3Service;

    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppBadException("File is required");
        }

        // Upload file to S3
        AttachDTO attachDTO = s3Service.uploadFile(file);

        // Create gallery entity
        GalleryEntity gallery = new GalleryEntity();
        gallery.setAttachId(attachDTO.getId());

        galleryRepository.save(gallery);

        return gallery.getId();
    }

    public List<GalleryResponseDTO> getAll() {
        List<GalleryEntity> galleries = galleryRepository.findAllByOrderByCreatedAtDesc();
        return galleries.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<GalleryResponseDTO> getLastN(int n) {
        Pageable pageable = PageRequest.of(0, n);
        List<GalleryEntity> galleries = galleryRepository.findAllByOrderByCreatedAtDesc(pageable);
        return galleries.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public void delete(String id) {
        GalleryEntity gallery = galleryRepository.findById(id)
                .orElseThrow(() -> new AppBadException("Gallery photo not found"));

        // Delete from S3
        s3Service.delete(gallery.getAttachId());

        // Delete gallery record
        galleryRepository.delete(gallery);
    }

    private GalleryResponseDTO toResponseDTO(GalleryEntity gallery) {
        GalleryResponseDTO dto = new GalleryResponseDTO();
        dto.setId(gallery.getId());
        dto.setCreatedAt(gallery.getCreatedAt());

        // Fetch complete attachment details
        Optional<AttachEntity> attachOptional = attachRepository.findById(gallery.getAttachId());
        if (attachOptional.isEmpty()) {
            throw new AppBadException("File not found");
        }
        dto.setAttach(s3Service.getImageUrlById(attachOptional.get().getId()));
        return dto;
    }
}
