package sayyeed.dev.atlantbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sayyeed.dev.atlantbackend.dto.AttachDTO;
import sayyeed.dev.atlantbackend.service.aws.IS3Service;
import java.util.Map;

@RestController
@RequestMapping("/attach/")
public class AttachController {

    @Autowired
    private IS3Service service;

    @PostMapping(value = "/admin/upload", consumes = "multipart/form-data")
    public ResponseEntity<AttachDTO> upload(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.uploadFile(file));
    }

    @GetMapping("/open/{fileId}")
    public ResponseEntity<Map<String, String>> open(@PathVariable String fileId) {
        return ResponseEntity.ok(service.openFile(fileId));
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> download(@PathVariable String fileId) {
        return service.download(fileId);
    }

    @DeleteMapping("/admin/delete/{fileId}")
    public ResponseEntity<Boolean> delete(@PathVariable String fileId) {
        return ResponseEntity.ok(service.delete(fileId));
    }
}
