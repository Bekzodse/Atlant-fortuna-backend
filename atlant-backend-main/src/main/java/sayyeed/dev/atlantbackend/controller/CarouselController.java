package sayyeed.dev.atlantbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sayyeed.dev.atlantbackend.dto.CarouselResponseDTO;
import sayyeed.dev.atlantbackend.service.CarouselService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/carousel")
public class CarouselController {

    @Autowired
    private CarouselService carouselService;

    // PUBLIC ENDPOINTS

    @GetMapping("/public")
    public ResponseEntity<List<CarouselResponseDTO>> getAllActiveCarousel() {
        List<CarouselResponseDTO> carousels = carouselService.getAllActive();
        return ResponseEntity.ok(carousels);
    }

    // ADMIN ENDPOINTS
    @PostMapping(value = "/admin", consumes = "multipart/form-data")
    public ResponseEntity<String> createCarousel(@RequestParam("file") MultipartFile file) {
        String carouselId = carouselService.create(file);
        return ResponseEntity.ok(carouselId);
    }

    @GetMapping("/admin")
    public ResponseEntity<List<CarouselResponseDTO>> getAllCarousel() {
        List<CarouselResponseDTO> carousels = carouselService.getAll();
        return ResponseEntity.ok(carousels);
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Boolean> deleteCarousel(@PathVariable String id) {
        Boolean result = carouselService.delete(id);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/admin/{id}/toggle")
    public ResponseEntity<Boolean> toggleCarouselActive(@PathVariable String id) {
        Boolean isActive = carouselService.toggleActive(id);
        return ResponseEntity.ok(isActive);
    }
}
