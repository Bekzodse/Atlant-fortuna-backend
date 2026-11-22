package sayyeed.dev.atlantbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sayyeed.dev.atlantbackend.dto.reels.ReelResponseDTO;
import sayyeed.dev.atlantbackend.service.ReelService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reels")
public class ReelController {

    private final ReelService reelService;

    public ReelController(ReelService reelService) {
        this.reelService = reelService;
    }

    @PostMapping(value = "/admin/upload", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadVideo(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(reelService.upload(file));
    }

    @GetMapping("/public")
    public ResponseEntity<List<ReelResponseDTO>> getAll(@RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(limit != null && limit > 0
                ? reelService.getLastN(limit)
                : reelService.getAll());
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        reelService.delete(id);
        return ResponseEntity.ok("Reel deleted successfully");
    }
}
