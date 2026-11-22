package sayyeed.dev.atlantbackend.service.aws.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import sayyeed.dev.atlantbackend.dto.AttachDTO;
import sayyeed.dev.atlantbackend.entities.AttachEntity;
import sayyeed.dev.atlantbackend.exceptions.AppBadException;
import sayyeed.dev.atlantbackend.repositories.AttachRepository;
import sayyeed.dev.atlantbackend.service.aws.IS3Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.*;

@Service
public class S3ServiceImpl implements IS3Service {

    private final RestClient.Builder builder;
    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AttachRepository attachRepository;

    @Autowired
    public S3ServiceImpl(S3Client s3Client, S3Presigner s3Presigner, AttachRepository attachRepository, RestClient.Builder builder) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.attachRepository = attachRepository;
        this.builder = builder;
    }

    // UPLOAD
    @Override
    public AttachDTO uploadFile(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            throw new AppBadException("File not found");
        }

        try {
            // creating file path
            String pathFolder = getYmDString();
            String key = UUID.randomUUID().toString();
            String extension = getExtension(Objects.requireNonNull(multipartFile.getOriginalFilename()));
            String fullKey = pathFolder + "/" + key + "." + extension;

            // upload to s3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fullKey)
                    .contentType(multipartFile.getContentType())
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));

            // save to db
            AttachEntity entity = new AttachEntity();
            entity.setId(key + "." + extension);
            entity.setPath(pathFolder);
            entity.setSize(multipartFile.getSize());
            entity.setOrigenName(multipartFile.getOriginalFilename());
            entity.setExtension(extension);
            entity.setVisible(true);
            attachRepository.save(entity);

            return toDTO(entity);

        } catch (IOException e) {
            throw new AppBadException("Upload failed");
        }
    }

    // OPEN FILE
    @Override
    public Map<String, String> openFile(String id) {
        Map<String, String> response = new HashMap<>();
        response.put("url", openURL(id));
        return response;
    }

    // DOWNLOAD
    @Override
    public ResponseEntity<Resource> download(String filename) {
        // get entity to retrieve the path
        AttachEntity entity = attachRepository.findById(filename)
                .orElseThrow(() -> new AppBadException("Attachment not found"));

        // Construct the full path (pathFolder + id)
        String fullPath = entity.getPath() + "/" + filename;

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fullPath)
                .responseContentDisposition("attachment; filename=\"" + entity.getOrigenName() + "\"")
                .responseContentType("application/octet-stream")
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(getObjectRequest)
                .build();

        String presignedUrl = s3Presigner.presignGetObject(presignRequest).url().toString();


        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(presignedUrl))
                .build();
    }

    //Delete
    @Override
    public Boolean delete(String fileId) {
        try {
            AttachEntity entity = attachRepository.findById(fileId)
                    .orElseThrow(() -> new AppBadException("Attachment not found"));

            String fullPath = entity.getPath() + "/" + fileId;

            // delete the obj from s3 bucket
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fullPath)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

            attachRepository.setVisibleFalse(entity.getId());

            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new AppBadException("Something went wrong");
        }
    }

    @Override
    public AttachDTO getImageUrlById(String filename) {
        AttachDTO dto = new AttachDTO();
        dto.setId(filename);
        dto.setUrl(openURL(filename));
        return dto;
    }

    private AttachEntity findById(String filename) {
        return attachRepository.findById(filename)
                .orElseThrow(() -> new AppBadException("Attachment not found"));
    }

    private String getYmDString() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        return year + "/" + String.format("%02d", month) + "/" + String.format("%02d", day);
    }

    private AttachDTO toDTO(AttachEntity entity) {
        AttachDTO attachDTO = new AttachDTO();
        attachDTO.setId(entity.getId());
        attachDTO.setOriginName(entity.getOrigenName());
        attachDTO.setSize(entity.getSize());
        attachDTO.setExtension(entity.getExtension());
        attachDTO.setCreatedData(entity.getCreatedDate());
        attachDTO.setUrl(openURL(entity.getId()));
        return attachDTO;
    }

    private String getExtension(String fileName) { // something.jpg
        int lastIndex = fileName.lastIndexOf(".");
        return fileName.substring(lastIndex + 1);
    }

    private String openURL(String id) {
        // Get entity to retrieve the path
        AttachEntity entity = findById(id);

        // Construct the full path (pathFolder + id)
        String fullPath = entity.getPath() + "/" + id;

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fullPath)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(60))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }
}
