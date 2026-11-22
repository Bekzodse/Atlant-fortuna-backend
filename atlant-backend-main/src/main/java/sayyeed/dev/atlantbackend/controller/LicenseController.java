package sayyeed.dev.atlantbackend.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sayyeed.dev.atlantbackend.dto.license.LicenseRequestDTO;
import sayyeed.dev.atlantbackend.dto.license.LicenseResponseDTO;
import sayyeed.dev.atlantbackend.service.LicenseService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/licenses")
public class LicenseController {

    @Autowired
    private LicenseService licenseService;

    @PostMapping("/admin")
    public ResponseEntity<String> createLicense(@Valid @RequestBody LicenseRequestDTO requestDTO) {
        String licenseId = licenseService.create(requestDTO);
        return ResponseEntity.ok(licenseId);
    }

    @GetMapping("/public")
    public ResponseEntity<List<LicenseResponseDTO>> getAllLicenses(
            @RequestParam(required = false) Integer limit,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage
    ) {
        String language = parseLanguage(acceptLanguage);
        List<LicenseResponseDTO> licenses;

        if (limit != null && limit > 0) {
            licenses = licenseService.getLastN(limit, language);
        } else {
            licenses = licenseService.getAll(language);
        }

        return ResponseEntity.ok(licenses);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<LicenseResponseDTO> getLicenseById(
            @PathVariable String id,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage
    ) {
        String language = parseLanguage(acceptLanguage);
        LicenseResponseDTO license = licenseService.getById(id, language);
        return ResponseEntity.ok(license);
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<String> deleteLicense(@PathVariable String id) {
        licenseService.delete(id);
        return ResponseEntity.ok("License deleted successfully");
    }

    private String parseLanguage(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isEmpty()) {
            return "en";
        }

        String[] languages = acceptLanguage.split(",");
        for (String lang : languages) {
            String langCode = lang.split(";")[0].trim();

            if (langCode.contains("-")) {
                langCode = langCode.split("-")[0];
            }

            langCode = langCode.toLowerCase();

            if (langCode.equals("en") || langCode.equals("ru") || langCode.equals("uz")) {
                return langCode;
            }
        }

        return "en";
    }
}
