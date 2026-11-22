package sayyeed.dev.atlantbackend.dto.products;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import sayyeed.dev.atlantbackend.enums.ProductCategoryEnum;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class UpdateProductRequestDTO {

    @NotBlank(message = "Name in Uzbek is required")
    private String nameUz;

    @NotBlank(message = "Name in Russian is required")
    private String nameRu;

    @NotBlank(message = "Name in English is required")
    private String nameEn;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Article number is required")
    private String articleNumber;

    @NotNull(message = "Category is required")
    private ProductCategoryEnum category;

    @NotBlank(message = "Payment method in English is required")
    private String paymentMethodEn;

    @NotBlank(message = "Payment method in Russian is required")
    private String paymentMethodRu;

    @NotBlank(message = "Payment method in Uzbek is required")
    private String paymentMethodUz;

    @NotBlank(message = "Delivery info in English is required")
    private String deliveryEn;

    @NotBlank(message = "Delivery info in Russian is required")
    private String deliveryRu;

    @NotBlank(message = "Delivery info in Uzbek is required")
    private String deliveryUz;

    @NotBlank(message = "Description in English is required")
    private String descriptionEn;

    @NotBlank(message = "Description in Russian is required")
    private String descriptionRu;

    @NotBlank(message = "Description in Uzbek is required")
    private String descriptionUz;

    @NotNull(message = "Discount flag is required")
    private Boolean discount;

    private List<String> imageIds;
}
