package sayyeed.dev.atlantbackend.entities.products;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import sayyeed.dev.atlantbackend.enums.ProductCategoryEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column
    private String nameUz;

    @Column
    private String nameRu;

    @Column
    private String nameEn;

    @Column
    private BigDecimal price;

    @Column
    private String brand;

    @Column
    private String articleNumber;

    @Column
    private ProductCategoryEnum category;

    @Column
    private Boolean discount;

    @Column
    private String paymentMethodEn;

    @Column
    private String paymentMethodRu;

    @Column
    private String paymentMethodUz;

    @Column
    private String deliveryEn;

    @Column
    private String deliveryRu;

    @Column
    private String deliveryUz;

    @Column(columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(columnDefinition = "TEXT")
    private String descriptionRu;

    @Column(columnDefinition = "TEXT")
    private String descriptionUz;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<ProductImageEntity> images;

    @Column
    private LocalDateTime createdAt;
}
