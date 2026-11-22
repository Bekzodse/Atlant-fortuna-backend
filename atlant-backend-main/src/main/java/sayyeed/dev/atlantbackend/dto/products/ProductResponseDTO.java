package sayyeed.dev.atlantbackend.dto.products;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import sayyeed.dev.atlantbackend.enums.PaymentMethodEnum;
import sayyeed.dev.atlantbackend.enums.ProductCategoryEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponseDTO {

    private String id;

    private String nameUz;

    private String nameRu;

    private String nameEn;

    // Language-aware fields (for public API)
    private String name;

    private String paymentMethod;
    private String paymentMethodEn;
    private String paymentMethodRu;
    private String paymentMethodUz;

    private String delivery;
    private String deliveryEn;
    private String deliveryRu;
    private String deliveryUz;

    private String description;
    private String descriptionEn;
    private String descriptionRu;
    private String descriptionUz;

    private BigDecimal price;

    private String brand;

    private String articleNumber;

    private ProductCategoryEnum category;

    private Boolean discount;

    private List<PaymentMethodEnum> paymentMethods;

    private List<ProductImageDTO> images;

    private LocalDateTime createdAt;
}
