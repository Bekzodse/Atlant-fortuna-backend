package sayyeed.dev.atlantbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sayyeed.dev.atlantbackend.dto.gallery.GalleryResponseDTO;
import sayyeed.dev.atlantbackend.service.GalleryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gallery")
public class GalleryController {

    @Autowired
    private GalleryService galleryService;

    @PostMapping(value = "/admin/upload", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadPhoto(@RequestParam("file") MultipartFile file) {
        String galleryId = galleryService.upload(file);
        return ResponseEntity.ok(galleryId);
    }

    @GetMapping("/public")
    public ResponseEntity<List<GalleryResponseDTO>> getAllPhotos(
            @RequestParam(required = false) Integer limit
    ) {
        List<GalleryResponseDTO> photos;
        if (limit != null && limit > 0) {
            photos = galleryService.getLastN(limit);
        } else {
            photos = galleryService.getAll();
        }
        return ResponseEntity.ok(photos);
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<String> deletePhoto(@PathVariable String id) {
        galleryService.delete(id);
        return ResponseEntity.ok("Gallery photo deleted successfully");
    }
}
