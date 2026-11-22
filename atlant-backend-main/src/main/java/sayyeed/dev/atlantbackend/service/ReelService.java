package sayyeed.dev.atlantbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sayyeed.dev.atlantbackend.dto.AttachDTO;
import sayyeed.dev.atlantbackend.dto.reels.ReelResponseDTO;
import sayyeed.dev.atlantbackend.entities.AttachEntity;
import sayyeed.dev.atlantbackend.entities.ReelEntity;
import sayyeed.dev.atlantbackend.exceptions.AppBadException;
import sayyeed.dev.atlantbackend.repositories.AttachRepository;
import sayyeed.dev.atlantbackend.repositories.ReelRepository;
import sayyeed.dev.atlantbackend.service.aws.IS3Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReelService {

    private final ReelRepository reelRepository;
    private final AttachRepository attachRepository;
    private final IS3Service s3Service;

    @Autowired
    public ReelService(ReelRepository reelRepository, AttachRepository attachRepository, IS3Service s3Service) {
        this.reelRepository = reelRepository;
        this.attachRepository = attachRepository;
        this.s3Service = s3Service;
    }

    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppBadException("Video file is required");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("video/")) {
            throw new AppBadException("Only video files are allowed");
        }

        AttachDTO attachDTO = s3Service.uploadFile(file);
        ReelEntity reel = new ReelEntity();
        reel.setAttachId(attachDTO.getId());
        reelRepository.save(reel);
        return reel.getId();
    }

    public List<ReelResponseDTO> getAll() {
        return reelRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ReelResponseDTO> getLastN(int n) {
        return reelRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, n)).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public void delete(String id) {
        ReelEntity reel = reelRepository.findById(id)
                .orElseThrow(() -> new AppBadException("Reel not found"));
        s3Service.delete(reel.getAttachId());
        reelRepository.delete(reel);
    }

    private ReelResponseDTO toResponseDTO(ReelEntity reel) {
        AttachEntity attachment = attachRepository.findById(reel.getAttachId())
                .orElseThrow(() -> new AppBadException("Video file not found"));
        ReelResponseDTO dto = new ReelResponseDTO();
        dto.setId(reel.getId());
        dto.setCreatedAt(reel.getCreatedAt());
        dto.setAttach(s3Service.getImageUrlById(attachment.getId()));
        return dto;
    }
}
