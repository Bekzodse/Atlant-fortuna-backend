package sayyeed.dev.atlantbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sayyeed.dev.atlantbackend.dto.AttachDTO;
import sayyeed.dev.atlantbackend.dto.CarouselResponseDTO;
import sayyeed.dev.atlantbackend.entities.CarouselEntity;
import sayyeed.dev.atlantbackend.exceptions.AppBadException;
import sayyeed.dev.atlantbackend.repositories.CarouselRepository;
import sayyeed.dev.atlantbackend.service.aws.IS3Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarouselService {

    @Autowired
    private CarouselRepository carouselRepository;

    @Autowired
    private IS3Service s3Service;

    // Admin: Create new carousel item
    public String create(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppBadException("File is required");
        }

        // Upload file to S3
        AttachDTO attachDTO = s3Service.uploadFile(file);

        // Create carousel entity
        CarouselEntity carousel = new CarouselEntity();
        carousel.setAttachId(attachDTO.getId());
        carousel.setActive(true);

        carouselRepository.save(carousel);

        return carousel.getId();
    }

    // Admin: Delete carousel item
    public Boolean delete(String id) {
        CarouselEntity carousel = carouselRepository.findById(id)
                .orElseThrow(() -> new AppBadException("Carousel item not found"));

        // Delete from S3 and mark as invisible
        s3Service.delete(carousel.getAttachId());

        // Delete carousel record
        carouselRepository.delete(carousel);

        return true;
    }

    // Admin: Toggle active status
    public Boolean toggleActive(String id) {
        CarouselEntity carousel = carouselRepository.findById(id)
                .orElseThrow(() -> new AppBadException("Carousel item not found"));

        carousel.setActive(!carousel.getActive());
        carouselRepository.save(carousel);

        return carousel.getActive();
    }

    // Public: Get all active carousel items
    public List<CarouselResponseDTO> getAllActive() {
        List<CarouselEntity> carousels = carouselRepository.findAllByActiveTrueOrderByCreatedAtDesc();
        return carousels.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Admin: Get all carousel items
    public List<CarouselResponseDTO> getAll() {
        List<CarouselEntity> carousels = carouselRepository.findAllByOrderByCreatedAtDesc();
        return carousels.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Convert entity to DTO
    private CarouselResponseDTO toDTO(CarouselEntity entity) {
        CarouselResponseDTO dto = new CarouselResponseDTO();
        dto.setId(entity.getId());
        dto.setAttachId(entity.getAttachId());
        dto.setActive(entity.getActive());
        dto.setCreatedAt(entity.getCreatedAt());

        // Get presigned URL for the image
        AttachDTO attachDTO = s3Service.getImageUrlById(entity.getAttachId());
        dto.setImageUrl(attachDTO.getUrl());

        return dto;
    }
}
