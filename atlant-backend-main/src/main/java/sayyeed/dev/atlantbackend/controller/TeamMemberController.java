package sayyeed.dev.atlantbackend.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sayyeed.dev.atlantbackend.dto.team.TeamMemberRequestDTO;
import sayyeed.dev.atlantbackend.dto.team.TeamMemberResponseDTO;
import sayyeed.dev.atlantbackend.service.TeamService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/team")
@AllArgsConstructor
public class TeamMemberController {

    private final TeamService service;

    @PostMapping("/admin")
    public ResponseEntity<String> addMember(@RequestBody TeamMemberRequestDTO dto) {
        return ResponseEntity.ok(service.addMember(dto));
    }

    @GetMapping("/public")
    public ResponseEntity<List<TeamMemberResponseDTO>> getAll(
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage
    ) {
        String lang = parseLanguage(acceptLanguage);
        return ResponseEntity.ok(service.getAll(lang));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<TeamMemberResponseDTO>> getAllForAdmin() {
        return ResponseEntity.ok(service.getAllForAdmin());
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<String> updateMember(@PathVariable String id,
                                               @RequestBody TeamMemberRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<String> deleteMember(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.ok("Team member deleted successfully");
    }

    private String parseLanguage(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isEmpty()) {
            return "en";
        }

        // Parse "en-US,en;q=0.9,ru;q=0.8" format
        String[] languages = acceptLanguage.split(",");
        for (String lang : languages) {
            String langCode = lang.split(";")[0].trim();
            if (langCode.contains("-")) {
                langCode = langCode.split("-")[0];
            }
            langCode = langCode.toLowerCase();

            // Return first supported language
            if (langCode.equals("en") || langCode.equals("ru") || langCode.equals("uz")) {
                return langCode;
            }
        }

        // Default to English
        return "en";
    }
}
