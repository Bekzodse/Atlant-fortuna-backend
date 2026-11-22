package sayyeed.dev.atlantbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sayyeed.dev.atlantbackend.dto.AttachDTO;
import sayyeed.dev.atlantbackend.dto.products.*;
import sayyeed.dev.atlantbackend.entities.AttachEntity;
import sayyeed.dev.atlantbackend.entities.products.ProductEntity;
import sayyeed.dev.atlantbackend.entities.products.ProductImageEntity;
import sayyeed.dev.atlantbackend.enums.ProductCategoryEnum;
import sayyeed.dev.atlantbackend.exceptions.AppBadException;
import sayyeed.dev.atlantbackend.mapper.ProductPublicMapper;
import sayyeed.dev.atlantbackend.repositories.AttachRepository;
import sayyeed.dev.atlantbackend.repositories.ProductImageRepository;
import sayyeed.dev.atlantbackend.repositories.ProductRepository;
import sayyeed.dev.atlantbackend.service.aws.IS3Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private AttachRepository attachRepository;

    @Autowired
    private IS3Service s3Service;

    public String create(CreateProductRequestDTO dto) {
        // Create product entity
        ProductEntity entity = new ProductEntity();
        entity.setNameUz(dto.getNameUz());
        entity.setNameRu(dto.getNameRu());
        entity.setNameEn(dto.getNameEn());
        entity.setPrice(dto.getPrice());
        entity.setBrand(dto.getBrand());
        entity.setArticleNumber(dto.getArticleNumber());
        entity.setCategory(dto.getCategory());
        entity.setPaymentMethodEn(dto.getPaymentMethodEn());
        entity.setPaymentMethodRu(dto.getPaymentMethodRu());
        entity.setPaymentMethodUz(dto.getPaymentMethodUz());
        entity.setDeliveryEn(dto.getDeliveryEn());
        entity.setDeliveryRu(dto.getDeliveryRu());
        entity.setDeliveryUz(dto.getDeliveryUz());
        entity.setDescriptionEn(dto.getDescriptionEn());
        entity.setDescriptionRu(dto.getDescriptionRu());
        entity.setDescriptionUz(dto.getDescriptionUz());
        entity.setDiscount(dto.getDiscount());
        entity.setCreatedAt(LocalDateTime.now());

        // Save product
        productRepository.save(entity);

        // Link images if provided
        if (dto.getImageIds() != null && !dto.getImageIds().isEmpty()) {
            linkImagesToProduct(entity.getId(), dto.getImageIds());
        }

        return entity.getId();
    }

    public String update(String id, UpdateProductRequestDTO dto) {
        // Find existing product
        Optional<ProductEntity> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            throw new AppBadException("Product not found");
        }

        ProductEntity entity = optionalProduct.get();

        // Update all fields (frontend always sends all data)
        entity.setNameUz(dto.getNameUz());
        entity.setNameRu(dto.getNameRu());
        entity.setNameEn(dto.getNameEn());
        entity.setPrice(dto.getPrice());
        entity.setBrand(dto.getBrand());
        entity.setArticleNumber(dto.getArticleNumber());
        entity.setCategory(dto.getCategory());
        entity.setPaymentMethodEn(dto.getPaymentMethodEn());
        entity.setPaymentMethodRu(dto.getPaymentMethodRu());
        entity.setPaymentMethodUz(dto.getPaymentMethodUz());
        entity.setDeliveryEn(dto.getDeliveryEn());
        entity.setDeliveryRu(dto.getDeliveryRu());
        entity.setDeliveryUz(dto.getDeliveryUz());
        entity.setDescriptionEn(dto.getDescriptionEn());
        entity.setDescriptionRu(dto.getDescriptionRu());
        entity.setDescriptionUz(dto.getDescriptionUz());
        entity.setDiscount(dto.getDiscount());

        // Save updated product
        productRepository.save(entity);

        // Update images if provided
        if (dto.getImageIds() != null) {
            // Delete existing images
            List<ProductImageEntity> existingImages = productImageRepository.findByProductId(id);
            productImageRepository.deleteAll(existingImages);

            // Link new images
            if (!dto.getImageIds().isEmpty()) {
                linkImagesToProduct(id, dto.getImageIds());
            }
        }

        return entity.getId();
    }

    public void delete(String id) {
        Optional<ProductEntity> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            throw new AppBadException("Product not found");
        }

        // Delete product images first
        List<ProductImageEntity> images = productImageRepository.findByProductId(id);
        productImageRepository.deleteAll(images);

        // Delete product
        productRepository.deleteById(id);
    }

    public ProductResponseDTO getById(String id) {
        Optional<ProductEntity> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            throw new AppBadException("Product not found");
        }

        return toResponseDTO(optionalProduct.get());
    }

    public List<ProductResponseDTO> getAll() {
        List<ProductEntity> products = productRepository.findAllOrderByCreatedAtDesc();
        return products.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ProductResponseDTO> getByCategory(ProductCategoryEnum category) {
        List<ProductEntity> products = productRepository.findByCategory(category);
        return products.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ProductResponseDTO> getByBrand(String brand) {
        List<ProductEntity> products = productRepository.findByBrand(brand);
        return products.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Helper method to link images to product
    private void linkImagesToProduct(String productId, List<String> imageIds) {
        int displayOrder = 0;
        for (String imageId : imageIds) {
            // Verify image exists
            Optional<AttachEntity> attachOptional = attachRepository.findById(imageId);
            if (attachOptional.isEmpty()) {
                throw new AppBadException("Image with id " + imageId + " not found");
            }

            ProductImageEntity productImage = new ProductImageEntity();
            productImage.setProductId(productId);
            productImage.setAttachId(imageId);
            productImage.setDisplayOrder(displayOrder++);
            productImageRepository.save(productImage);
        }
    }

    // Helper method to convert entity to DTO with all information for admin
    private ProductResponseDTO toResponseDTO(ProductEntity entity) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(entity.getId());
        dto.setNameUz(entity.getNameUz());
        dto.setNameRu(entity.getNameRu());
        dto.setNameEn(entity.getNameEn());
        dto.setPaymentMethodEn(entity.getPaymentMethodEn());
        dto.setPaymentMethodRu(entity.getPaymentMethodRu());
        dto.setPaymentMethodUz(entity.getPaymentMethodUz());
        dto.setDeliveryEn(entity.getDeliveryEn());
        dto.setDeliveryRu(entity.getDeliveryRu());
        dto.setDeliveryUz(entity.getDeliveryUz());
        dto.setDescriptionEn(entity.getDescriptionEn());
        dto.setDescriptionRu(entity.getDescriptionRu());
        dto.setDescriptionUz(entity.getDescriptionUz());
        dto.setPrice(entity.getPrice());
        dto.setBrand(entity.getBrand());
        dto.setArticleNumber(entity.getArticleNumber());
        dto.setCategory(entity.getCategory());
        dto.setDiscount(entity.getDiscount());
        dto.setCreatedAt(entity.getCreatedAt());

        // Get product images with S3 URLs
        List<ProductImageEntity> productImages = productImageRepository.findByProductIdOrderByDisplayOrder(entity.getId());
        List<ProductImageDTO> imageDTOs = new ArrayList<>();

        for (ProductImageEntity productImage : productImages) {
            ProductImageDTO imageDTO = new ProductImageDTO();
            imageDTO.setId(productImage.getId());
            imageDTO.setAttachId(productImage.getAttachId());
            imageDTO.setDisplayOrder(productImage.getDisplayOrder());

            // Get S3 presigned URL
            AttachDTO attachDTO = s3Service.getImageUrlById(productImage.getAttachId());
            imageDTO.setPath(attachDTO.getUrl());

            imageDTOs.add(imageDTO);
        }

        dto.setImages(imageDTOs);
        return dto;
    }

    // Language-aware public methods
    public ProductResponseDTO getByIdAndLang(String id, String lang) {
        Optional<ProductPublicMapper> optionalProduct = productRepository.getByIdAndLang(id, lang);
        if (optionalProduct.isEmpty()) {
            throw new AppBadException("Product not found");
        }
        return toPublicResponseDTO(optionalProduct.get(), id);
    }

    public List<ProductResponseDTO> getAllByLang(String lang) {
        List<ProductPublicMapper> mappers = productRepository.getAllByLang(lang);
        return mappers.stream()
                .map(mapper -> toPublicResponseDTO(mapper, mapper.getId()))
                .collect(Collectors.toList());
    }

    public List<ProductResponseDTO> getByCategoryAndLang(ProductCategoryEnum category, String lang) {
        List<ProductPublicMapper> mappers = productRepository.getByCategoryAndLang(category, lang);
        return mappers.stream()
                .map(mapper -> toPublicResponseDTO(mapper, mapper.getId()))
                .collect(Collectors.toList());
    }

    public List<ProductResponseDTO> getByBrandAndLang(String brand, String lang) {
        List<ProductPublicMapper> mappers = productRepository.getByBrandAndLang(brand, lang);
        return mappers.stream()
                .map(mapper -> toPublicResponseDTO(mapper, mapper.getId()))
                .collect(Collectors.toList());
    }

    // Helper method to convert ProductPublicMapper to ProductResponseDTO with images
    private ProductResponseDTO toPublicResponseDTO(ProductPublicMapper mapper, String productId) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(mapper.getId());
        dto.setName(mapper.getName());
        dto.setPrice(mapper.getPrice());
        dto.setBrand(mapper.getBrand());
        dto.setArticleNumber(mapper.getArticleNumber());
        dto.setCategory(mapper.getCategory());
        dto.setDiscount(mapper.getDiscount());
        dto.setPaymentMethod(mapper.getPaymentMethod());
        dto.setDelivery(mapper.getDelivery());
        dto.setDescription(mapper.getDescription());
        dto.setCreatedAt(mapper.getCreatedAt());

        // Get product images with S3 URLs
        List<ProductImageEntity> productImages = productImageRepository.findByProductIdOrderByDisplayOrder(productId);
        List<ProductImageDTO> imageDTOs = new ArrayList<>();

        for (ProductImageEntity productImage : productImages) {
            ProductImageDTO imageDTO = new ProductImageDTO();
            imageDTO.setId(productImage.getId());
            imageDTO.setAttachId(productImage.getAttachId());
            imageDTO.setDisplayOrder(productImage.getDisplayOrder());

            // Get S3 presigned URL
            AttachDTO attachDTO = s3Service.getImageUrlById(productImage.getAttachId());
            imageDTO.setPath(attachDTO.getUrl());

            imageDTOs.add(imageDTO);
        }

        dto.setImages(imageDTOs);
        return dto;
    }
}
