package sayyeed.dev.atlantbackend.service.aws;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import sayyeed.dev.atlantbackend.dto.AttachDTO;

import java.util.Map;

public interface IS3Service {
    AttachDTO uploadFile(MultipartFile multipartFile);
    Map<String, String> openFile(String id);
    ResponseEntity<Resource> download(String filename);
    Boolean delete(String fileId);
    AttachDTO getImageUrlById(String filename);
}
