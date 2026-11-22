package sayyeed.dev.atlantbackend.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sayyeed.dev.atlantbackend.dto.products.*;
import sayyeed.dev.atlantbackend.enums.ProductCategoryEnum;
import sayyeed.dev.atlantbackend.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    @Autowired
    private ProductService service;

    // get all products by lang
    // Public endpoints - accessible to everyone with language support
    @GetMapping("/public")
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts(
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage) {
        String lang = parseLanguage(acceptLanguage);
        return ResponseEntity.ok(service.getAllByLang(lang));
    }

    // get product details by id
    @GetMapping("/public/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(
            @PathVariable String id,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage) {
        String lang = parseLanguage(acceptLanguage);
        return ResponseEntity.ok(service.getByIdAndLang(id, lang));
    }

    // get all products by category
    @GetMapping("/public/category/{category}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByCategory(
            @PathVariable ProductCategoryEnum category,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage) {
        String lang = parseLanguage(acceptLanguage);
        return ResponseEntity.ok(service.getByCategoryAndLang(category, lang));
    }

    @GetMapping("/public/brand/{brand}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByBrand(
            @PathVariable String brand,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage) {
        String lang = parseLanguage(acceptLanguage);
        return ResponseEntity.ok(service.getByBrandAndLang(brand, lang));
    }

    // Admin endpoints - require ADMIN role
    @GetMapping("/admin")
    public ResponseEntity<List<ProductResponseDTO>> getAllForAdmin() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<ProductResponseDTO> getProductByIdForAdmin(@PathVariable String id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping("/admin")
    public ResponseEntity<String> createProduct(@Valid @RequestBody CreateProductRequestDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<String> updateProduct(@PathVariable String id,
                                                @Valid @RequestBody UpdateProductRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.ok("Product deleted successfully");
    }

    // Helper method to parse Accept-Language header
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
